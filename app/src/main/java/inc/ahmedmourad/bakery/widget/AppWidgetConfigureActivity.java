package inc.ahmedmourad.bakery.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import inc.ahmedmourad.bakery.R;
import inc.ahmedmourad.bakery.adapters.ConfigureRecyclerAdapter;
import inc.ahmedmourad.bakery.model.room.database.BakeryDatabase;
import inc.ahmedmourad.bakery.model.room.entities.RecipeEntity;
import inc.ahmedmourad.bakery.utils.ErrorUtils;
import inc.ahmedmourad.bakery.utils.NetworkUtils;
import inc.ahmedmourad.bakery.utils.PreferencesUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * The configuration screen for the {@link AppWidget AppWidget} AppWidget.
 */
public class AppWidgetConfigureActivity extends AppCompatActivity implements ConfigureRecyclerAdapter.OnConfigureRecipeSelected {

	private static final String PREF_PREFIX_KEY = "appwidget_";

	private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	private RecyclerView recyclerView;
	private ConfigureRecyclerAdapter recyclerAdapter;

	private CompositeDisposable disposables = new CompositeDisposable();

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		// If this activity was started with an intent without an app widget ID, finish with an error.
		if (!fetchWidgetId(getIntent()))
			finish();
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		// Set the result to CANCELED.  This will cause the widget host to cancel
		// out of the widget placement if the user presses the back button.
		setResult(RESULT_CANCELED);

		setContentView(R.layout.app_widget_configure);

		recyclerView = findViewById(R.id.configure_recycler_view);

		// If this activity was started with an intent without an app widget ID, finish with an error.
		if (!fetchWidgetId(getIntent())) {
			finish();
			return;
		}

		BakeryDatabase db = BakeryDatabase.getInstance(this);

		disposables.add(db.recipesDao()
				.getCount()
				.subscribeOn(Schedulers.io())
				.observeOn(Schedulers.io())
				.map(count -> count < 4)
				.subscribe(needsSync -> {
					if (needsSync)
						disposables.add(NetworkUtils.fetchRecipes(getApplicationContext(), db));
				}, throwable -> disposables.add(NetworkUtils.fetchRecipes(getApplicationContext(), db)))
		);

		initializeRecyclerView();

		disposables.add(db.recipesDao()
				.getRecipes()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(recyclerAdapter::updateRecipes,
						throwable -> ErrorUtils.general(this, throwable))
		);
	}

	@SuppressWarnings("all")
	private boolean fetchWidgetId(final Intent intent) {

		Bundle extras = intent.getExtras();

		if (extras != null)
			widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

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
	public void onConfigureRecipeSelected(RecipeEntity recipe) {

		saveSelectedRecipe(this, widgetId, recipe.id);

		// It is the responsibility of the configuration activity to update the app widget
		Disposable d = AppWidget.updateAppWidget(this, AppWidgetManager.getInstance(this), widgetId);

		if (d != null)
			disposables.add(d);

		// Make sure we pass back the original appWidgetId
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		setResult(RESULT_OK, resultValue);
		finish();
	}

	// Write the prefix to the SharedPreferences object for this widget
	static void saveSelectedRecipe(Context context, int appWidgetId, int recipeId) {
		PreferencesUtils.edit(context, e -> e.putInt(PREF_PREFIX_KEY + appWidgetId, recipeId));
	}

	// Read the prefix from the SharedPreferences object for this widget.
	// If there is no preference saved, get the default from a resource
	static int loadSelectedRecipe(Context context, int appWidgetId) {
		return PreferencesUtils.defaultPrefs(context).getInt(PREF_PREFIX_KEY + appWidgetId, -1);
	}

	static void unselectRecipe(Context context, int appWidgetId) {
		PreferencesUtils.edit(context, e -> e.remove(PREF_PREFIX_KEY + appWidgetId));
	}

	@Override
	protected void onDestroy() {
		disposables.clear();
		super.onDestroy();
	}
}

