package inc.ahmedmourad.bakery.external.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import inc.ahmedmourad.bakery.R;
import inc.ahmedmourad.bakery.adapters.ConfigureRecyclerAdapter;
import inc.ahmedmourad.bakery.bus.RxBus;
import inc.ahmedmourad.bakery.model.room.database.BakeryDatabase;
import inc.ahmedmourad.bakery.model.room.entities.RecipeEntity;
import inc.ahmedmourad.bakery.utils.ErrorUtils;
import inc.ahmedmourad.bakery.utils.NetworkUtils;
import inc.ahmedmourad.bakery.utils.WidgetUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * The configuration screen for the {@link AppWidget} AppWidget.
 */
public class AppWidgetConfigureActivity extends AppCompatActivity implements ConfigureRecyclerAdapter.OnConfigureRecipeSelected {

	private static final String CODE_ERROR = "awce";

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.configure_recycler_view)
	RecyclerView recyclerView;

	private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	private ConfigureRecyclerAdapter recyclerAdapter;

	private final CompositeDisposable disposables = new CompositeDisposable();
	private CompositeDisposable syncDisposables;

	private Disposable errorDisposable;

	private Intent resultValue;

	private Unbinder unbinder;

	@Override
	protected void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);

		// If this activity was started with an intent without an app widget ID, finish with an error.
		if (!fetchWidgetId(getIntent()))
			finish();
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the result to CANCELED.  This will cause the widget host to cancel
		// out of the widget placement if the user presses the back button.
		setResult(RESULT_CANCELED);

		setContentView(R.layout.app_widget_configure);

		unbinder = ButterKnife.bind(this);

		// If this activity was started with an intent without an app widget ID, finish with an error.
		if (!fetchWidgetId(getIntent())) {
			finish();
			return;
		}

		final BakeryDatabase db = BakeryDatabase.getInstance(this);

		syncDisposables = NetworkUtils.syncIfNeeded(getApplicationContext(), db, CODE_ERROR);

		initializeRecyclerView();

		disposables.add(db.recipesDao()
				.getRecipes()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(recyclerAdapter::updateRecipes,
						throwable -> ErrorUtils.general(this, throwable))
		);
	}

	/**
	 * extracts the widget id from the intent and stores it to the global variable widgetId
	 *
	 * @param intent the intent
	 * @return whether an id was found or not
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean fetchWidgetId(final Intent intent) {

		Bundle extras = intent.getExtras();

		if (extras != null)
			widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

		resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		setResult(RESULT_CANCELED, resultValue);

		return widgetId != AppWidgetManager.INVALID_APPWIDGET_ID;
	}

	private void initializeRecyclerView() {
		recyclerAdapter = new ConfigureRecyclerAdapter(this);
		recyclerView.setAdapter(recyclerAdapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
		recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
		recyclerView.setVerticalScrollBarEnabled(true);
	}

	@Override
	public void onConfigureRecipeSelected(final RecipeEntity recipe) {

		WidgetUtils.selectRecipe(this, widgetId, recipe.id);

		// It is the responsibility of the configuration activity to update the app widget
		final Disposable d = AppWidget.updateAppWidget(this, AppWidgetManager.getInstance(this), widgetId);

		if (d != null)
			disposables.add(d);

		// Make sure we pass back the original appWidgetId
		resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		setResult(RESULT_OK, resultValue);
		finish();
	}

	@Override
	protected void onStart() {
		super.onStart();
		errorDisposable = RxBus.getInstance()
				.getNetworkErrorRelay()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(code -> {
					if (code.equals(CODE_ERROR) && recyclerAdapter.getItemCount() == 0) {
						setResult(RESULT_CANCELED, resultValue);
						finish();
					}
				}, throwable -> ErrorUtils.general(this, throwable));
	}

	@Override
	protected void onStop() {
		errorDisposable.dispose();
		super.onStop();
	}

	@Override
	protected void onDestroy() {

		if (syncDisposables != null)
			syncDisposables.clear();

		disposables.clear();

		unbinder.unbind();

		super.onDestroy();
	}
}

