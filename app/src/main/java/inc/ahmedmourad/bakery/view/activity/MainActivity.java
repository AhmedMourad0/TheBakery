package inc.ahmedmourad.bakery.view.activity;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eralp.circleprogressview.CircleProgressView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import inc.ahmedmourad.bakery.R;
import inc.ahmedmourad.bakery.bus.RxBus;
import inc.ahmedmourad.bakery.model.room.database.BakeryDatabase;
import inc.ahmedmourad.bakery.other.BundledFragment;
import inc.ahmedmourad.bakery.utils.ErrorUtils;
import inc.ahmedmourad.bakery.utils.NetworkUtils;
import inc.ahmedmourad.bakery.utils.OrientationUtils;
import inc.ahmedmourad.bakery.utils.PreferencesUtils;
import inc.ahmedmourad.bakery.utils.WidgetUtils;
import inc.ahmedmourad.bakery.view.fragments.IngredientsFragment;
import inc.ahmedmourad.bakery.view.fragments.PlayerFragment;
import inc.ahmedmourad.bakery.view.fragments.RecipesFragment;
import inc.ahmedmourad.bakery.view.fragments.StepsFragment;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

	public static final String TAG_RECIPES = "recipes";
	private static final String TAG_INGREDIENTS = "ingredients";
	public static final String TAG_STEPS = "steps";
	public static final String TAG_PLAYER = "player";

	public static final int FRAGMENT_RECIPES = 0;
	public static final int FRAGMENT_INGREDIENTS = 1;
	public static final int FRAGMENT_STEPS = 2;
	public static final int FRAGMENT_PLAYER = 3;

	private static final String STATE_SELECTED_RECIPE_ID = "main_sri";
	private static final String STATE_CURRENT_FRAGMENT_ID = "main_cfi";
	private static final String STATE_SELECTED_STEP_POSITION = "main_ssp";

	private static final int DURATION_ANIMATION = 100;

	@BindView(R.id.main_toolbar)
	Toolbar toolbar;

	@BindView(R.id.main_title)
	TextView titleTextView;

	@BindView(R.id.main_progressbar)
	CircleProgressView progressBar;

	@BindView(R.id.main_switch)
	SwitchCompat switchCompat;

	@BindView(R.id.main_back)
	ImageView backButton;

	@BindView(R.id.main_add_to_widget)
	ImageView addToWidgetButton;

	@BindView(R.id.main_fab)
	FloatingActionButton fab;

	@BindView(R.id.main_appbar)
	AppBarLayout appbar;

	@BindView(R.id.main_master_container)
	FrameLayout masterContainer;

	@BindView(R.id.main_detail_container)
	FrameLayout detailContainer;

	@BindView(R.id.main_root_container)
	LinearLayout rootContainer;

	private FragmentManager fragmentManager;

	private int selectedRecipeId = -1;
	private int selectedStepPosition = -1;
	private int currentFragmentId = FRAGMENT_RECIPES;
	private BundledFragment recipesFragment, ingredientsFragment, stepsFragment, playerFragment;

	private Unbinder unbinder;

	private CompositeDisposable busDisposables = new CompositeDisposable();

	private Disposable selectionUpdatingDisposable, syncDisposable, progressCalculationDisposable, dataFetchingDisposable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		unbinder = ButterKnife.bind(this);

		setSupportActionBar(toolbar);

		final BakeryDatabase db = BakeryDatabase.getInstance(this);

		syncDisposable = db.recipesDao()
				.getCount()
				.subscribeOn(Schedulers.io())
				.observeOn(Schedulers.io())
				.map(count -> count < 4)
				.subscribe(needsSync -> {
					if (needsSync)
						dataFetchingDisposable = NetworkUtils.fetchRecipes(getApplicationContext(), db);
				}, throwable -> dataFetchingDisposable = NetworkUtils.fetchRecipes(getApplicationContext(), db));

		fragmentManager = getSupportFragmentManager();

		attachBusSubscribers();

		initializeOrRestoreInstanceFragments(savedInstanceState);

		fab.setOnClickListener(v -> {

			if (getResources().getBoolean(R.bool.useMasterDetailFlow)) {

				selectedStepPosition = 0;

				stepsFragment = StepsFragment.newInstance(selectedRecipeId);
				playerFragment = PlayerFragment.newInstance(selectedRecipeId, selectedStepPosition);

				moveToMasterDetailFlow();

			} else {
				displayFragment(StepsFragment.newInstance(selectedRecipeId), TAG_STEPS, true);
			}
		});

		Completable selectionUpdatingCompletable = Completable.fromAction(() -> db.ingredientsDao().updateSelection(selectedRecipeId, Float.compare(progressBar.getProgress(), 0f) == 0))
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread());

		progressBar.setOnClickListener(v -> showIngredientsSelectionDialog(selectionUpdatingCompletable));

		switchCompat.setChecked(PreferencesUtils.defaultPrefs(this).getBoolean(PreferencesUtils.KEY_USE_AUTOPLAY, true));

		switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> PreferencesUtils.edit(this, e -> e.putBoolean(PreferencesUtils.KEY_USE_AUTOPLAY, isChecked)));

		backButton.setOnClickListener(v -> onBackPressed());

		addToWidgetButton.setOnClickListener(v -> WidgetUtils.startWidgetChooser(this, selectedRecipeId));
	}

	private void initializeOrRestoreInstanceFragments(@Nullable Bundle savedInstanceState) {

		currentFragmentId = FRAGMENT_RECIPES;

		if (savedInstanceState == null) {

			displayFragment(RecipesFragment.newInstance(), TAG_RECIPES, true);

		} else {

			selectedRecipeId = savedInstanceState.getInt(STATE_SELECTED_RECIPE_ID, -1);

			if (selectedRecipeId == -1) {

				restoreFragment(savedInstanceState, TAG_RECIPES, true);

			} else {

				currentFragmentId = savedInstanceState.getInt(STATE_CURRENT_FRAGMENT_ID, FRAGMENT_RECIPES);
				selectedStepPosition = savedInstanceState.getInt(STATE_SELECTED_STEP_POSITION, -1);

				if (currentFragmentId >= FRAGMENT_RECIPES)
					restoreFragment(savedInstanceState, TAG_RECIPES, true);

				if (currentFragmentId >= FRAGMENT_INGREDIENTS)
					restoreFragment(savedInstanceState, TAG_INGREDIENTS, true);

				if (getResources().getBoolean(R.bool.useMasterDetailFlow) && currentFragmentId >= FRAGMENT_STEPS) {

					if (selectedStepPosition == -1)
						selectedStepPosition = 0;

					if (currentFragmentId >= FRAGMENT_STEPS)
						stepsFragment = restoreFragment(savedInstanceState, TAG_STEPS, false);

					if (currentFragmentId >= FRAGMENT_PLAYER) {
						if (selectedStepPosition == -1)
							stepsFragment = restoreFragment(savedInstanceState, TAG_STEPS, false);
						else
							playerFragment = restoreFragment(savedInstanceState, TAG_PLAYER, false);
					}

					if (selectedRecipeId != -1) {

						if (stepsFragment == null)
							stepsFragment = StepsFragment.newInstance(selectedRecipeId);

						if (playerFragment == null)
							playerFragment = PlayerFragment.newInstance(selectedRecipeId, selectedStepPosition);

						moveToMasterDetailFlow();
					}

				} else {

					if (currentFragmentId >= FRAGMENT_STEPS)
						restoreFragment(savedInstanceState, TAG_STEPS, true);

					if (currentFragmentId >= FRAGMENT_PLAYER) {
						if (selectedStepPosition == -1)
							restoreFragment(savedInstanceState, TAG_STEPS, true);
						else
							restoreFragment(savedInstanceState, TAG_PLAYER, true);
					}
				}
			}
		}

		OrientationUtils.isTransactionDone = true;
	}

	private void moveToMasterDetailFlow() {

		detailContainer.setVisibility(View.VISIBLE);

		currentFragmentId = FRAGMENT_PLAYER;

		if (!isFinishing())
			fragmentManager.beginTransaction()
					.replace(R.id.main_master_container, stepsFragment, TAG_STEPS)
					.addToBackStack(TAG_STEPS)
					.replace(R.id.main_detail_container, playerFragment, TAG_PLAYER)
					.addToBackStack(TAG_PLAYER)
					.commit();
	}

	private BundledFragment restoreFragment(final Bundle savedInstanceState, final String tag, boolean attach) {

		BundledFragment fragment;

		switch (tag) {

			case TAG_RECIPES:
				fragment = RecipesFragment.newInstance();
				break;

			case TAG_INGREDIENTS:
				fragment = IngredientsFragment.newInstance(selectedRecipeId);
				break;

			case TAG_STEPS:
				fragment = StepsFragment.newInstance(selectedRecipeId);
				break;

			case TAG_PLAYER:
				fragment = PlayerFragment.newInstance(selectedRecipeId, selectedStepPosition);
				break;

			default:
				currentFragmentId = FRAGMENT_RECIPES;
				fragment = RecipesFragment.newInstance();
		}

		fragment.restoreState(savedInstanceState.getBundle(tag));

		if (attach)
			displayFragment(fragment, tag, false);

		return fragment;
	}

	private void displayFragment(final BundledFragment fragment, final String tag, boolean updateCurrentFragmentId) {

		detailContainer.setVisibility(View.GONE);

		switch (tag) {

			case TAG_RECIPES:

				if (updateCurrentFragmentId)
					currentFragmentId = FRAGMENT_RECIPES;

				recipesFragment = fragment;
				break;

			case TAG_INGREDIENTS:

				if (updateCurrentFragmentId)
					currentFragmentId = FRAGMENT_INGREDIENTS;

				ingredientsFragment = fragment;
				break;

			case TAG_STEPS:

				if (updateCurrentFragmentId)
					currentFragmentId = FRAGMENT_STEPS;

				stepsFragment = fragment;
				break;

			case TAG_PLAYER:

				if (updateCurrentFragmentId)
					currentFragmentId = FRAGMENT_PLAYER;

				playerFragment = fragment;
				break;
		}

		if (!isFinishing())
			fragmentManager.beginTransaction()
					.replace(R.id.main_master_container, fragment, tag)
					.addToBackStack(tag)
					.commit();
	}

	private void showIngredientsSelectionDialog(Completable selectionUpdatingCompletable) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

		int titleId, messageId, positiveTextId;

		boolean isZero = Float.compare(progressBar.getProgress(), 0f) == 0;

		if (isZero) {
			titleId = R.string.select_all;
			messageId = R.string.select_all_ingredients;
			positiveTextId = R.string.select;
		} else {
			titleId = R.string.unselect_all;
			messageId = R.string.unselect_all_ingredients;
			positiveTextId = R.string.unselect;
		}

		final AlertDialog dialog = builder.setTitle(titleId)
				.setMessage(messageId)
				.setPositiveButton(positiveTextId, (d, which) -> {

					if (selectionUpdatingDisposable != null)
						selectionUpdatingDisposable.dispose();

					selectionUpdatingDisposable = selectionUpdatingCompletable.subscribe(() -> {

						RxBus.getInstance().setAllIngredientsSelected(isZero);
						progressBar.setProgressWithAnimation(isZero ? 100f : 0f, DURATION_ANIMATION);

					}, throwable -> ErrorUtils.general(this, throwable));

				}).create();

		final Resources resources = getResources();

		dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.colorSecondary)));

		dialog.show();
	}

	@Override
	protected void onStart() {
		super.onStart();
		attachBusSubscribers();
	}

	private void attachBusSubscribers() {

		if (busDisposables.size() == 13)
			return;

		busDisposables.clear();

		busDisposables.add(RxBus.getInstance()
				.getRecipeSelectionRelay()
				.subscribe(id -> displayFragment(IngredientsFragment.newInstance(id), TAG_INGREDIENTS, true),
						throwable -> ErrorUtils.critical(this, throwable))
		);

		// You may think it's an overkill to stream the whole list to calculate progress
		// instead of just sending an int to add to existing progress,
		// but doing that caused unexpected problems when the user clicked multiple
		// items at the same time which was only fixed using this approach
		busDisposables.add(RxBus.getInstance()
				.getIngredientsProgressRelay()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(ingredients -> {

					if (progressCalculationDisposable != null)
						progressCalculationDisposable.dispose();

					if (ingredients.isEmpty())
						progressBar.setProgress(0f);
					else
						progressCalculationDisposable = Observable.fromIterable(ingredients)
								.filter(ingredient -> ingredient.isSelected)
								.count()
								.map(selectedCount -> 100f / ingredients.size() * selectedCount)
								.subscribe(progress -> progressBar.setProgressWithAnimation(progress, DURATION_ANIMATION),
										throwable -> ErrorUtils.general(this, throwable));

				}, throwable -> ErrorUtils.general(this, throwable))
		);

		busDisposables.add(RxBus.getInstance()
				.getProgressVisibilityRelay()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(visibility -> progressBar.setVisibility(visibility),
						throwable -> ErrorUtils.general(this, throwable))
		);

		busDisposables.add(RxBus.getInstance()
				.getSwitchVisibilityRelay()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(visibility -> switchCompat.setVisibility(visibility),
						throwable -> ErrorUtils.general(this, throwable))
		);

		busDisposables.add(RxBus.getInstance()
				.getBackButtonVisibilityRelay()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(visibility -> backButton.setVisibility(visibility),
						throwable -> ErrorUtils.general(this, throwable))
		);

		busDisposables.add(RxBus.getInstance()
				.getAddToWidgetButtonVisibilityRelay()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(visibility -> addToWidgetButton.setVisibility(visibility),
						throwable -> ErrorUtils.general(this, throwable))
		);

		busDisposables.add(RxBus.getInstance()
				.getFabVisibilityRelay()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(visible -> {

					if (visible)
						fab.show();
					else
						fab.hide();

				}, throwable -> ErrorUtils.general(this, throwable))
		);

		busDisposables.add(RxBus.getInstance()
						.getToolbarVisibilityRelay()
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(visible -> {

							if (visible && appbar.getVisibility() != View.VISIBLE) {

								appbar.setVisibility(View.VISIBLE);

								CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) rootContainer.getLayoutParams();
								params.setBehavior(new AppBarLayout.ScrollingViewBehavior());

								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
									Window window = getWindow();
									window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
////							window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
								}

							} else if (!visible && appbar.getVisibility() != View.GONE) {

								appbar.setVisibility(View.GONE);

								CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) rootContainer.getLayoutParams();
								params.setBehavior(null);

								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
									Window window = getWindow();
									window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
									window.setStatusBarColor(Color.parseColor("#000000"));
////							window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
////									View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
								}
							}

						}, throwable -> ErrorUtils.general(this, throwable))
		);

		busDisposables.add(RxBus.getInstance()
				.getStepSelectionRelay()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(stepPosition -> displayFragment(PlayerFragment.newInstance(selectedRecipeId, stepPosition), TAG_PLAYER, true),
						throwable -> {
							ErrorUtils.general(this, throwable);
							displayFragment(StepsFragment.newInstance(selectedRecipeId), TAG_STEPS, true);
						})
		);

		busDisposables.add(RxBus.getInstance()
				.getTitleChangingRelay()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(title -> titleTextView.setText(title),
						throwable -> ErrorUtils.general(this, throwable))
		);

		busDisposables.add(RxBus.getInstance()
				.getSelectedRecipeIdRelay()
				.subscribe(id -> selectedRecipeId = id,
						throwable -> ErrorUtils.critical(this, throwable))
		);

		busDisposables.add(RxBus.getInstance()
				.getSelectedStepPositionRelay()
				.subscribe(id -> selectedStepPosition = id,
						throwable -> ErrorUtils.critical(this, throwable))
		);

		busDisposables.add(RxBus.getInstance()
				.getCurrentFragmentIdRelay()
				.subscribe(fragmentId -> currentFragmentId = fragmentId,
						throwable -> ErrorUtils.critical(this, throwable))
		);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		OrientationUtils.refreshSensorState(this);
	}

	@Override
	protected void onUserLeaveHint() {
		OrientationUtils.reset(this);
		super.onUserLeaveHint();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

		busDisposables.clear();

		Bundle state = new Bundle();

		state.putInt(STATE_SELECTED_RECIPE_ID, selectedRecipeId);
		state.putInt(STATE_CURRENT_FRAGMENT_ID, currentFragmentId);
		state.putInt(STATE_SELECTED_STEP_POSITION, selectedStepPosition);

		if (selectedRecipeId == -1) {

			if (recipesFragment != null)
				state.putBundle(TAG_RECIPES, recipesFragment.saveState());

		} else {

			if (currentFragmentId >= FRAGMENT_RECIPES && recipesFragment != null)
				state.putBundle(TAG_RECIPES, recipesFragment.saveState());

			if (currentFragmentId >= FRAGMENT_INGREDIENTS && ingredientsFragment != null)
				state.putBundle(TAG_INGREDIENTS, ingredientsFragment.saveState());

			if (currentFragmentId >= FRAGMENT_STEPS && stepsFragment != null)
				state.putBundle(TAG_STEPS, stepsFragment.saveState());

			if (currentFragmentId >= FRAGMENT_PLAYER) {

				if (selectedStepPosition == -1) {

					if (stepsFragment != null)
						state.putBundle(TAG_STEPS, stepsFragment.saveState());

				} else if (playerFragment != null) {
					state.putBundle(TAG_PLAYER, playerFragment.saveState());
				}
			}
		}

		if (isFinishing())
			for (int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i)
				fragmentManager.popBackStackImmediate();

		super.onSaveInstanceState(outState);

		outState.putAll(state);
	}

//	@Override
//	protected void onRestoreInstanceState(Bundle savedInstanceState) {
//		super.onRestoreInstanceState(savedInstanceState);
//
//		Log.e("xxxxxxxxxxxxxxxxxxxxxxx", "" + (savedInstanceState == null));
//
//		initializeOrRestoreInstanceFragments(savedInstanceState);
//	}

	@Override
	public void onBackPressed() {

		detailContainer.setVisibility(View.GONE);

		if (currentFragmentId <= FRAGMENT_RECIPES) {
			finish();
		} else {
			fragmentManager.popBackStackImmediate();
			--currentFragmentId;
		}
	}

	@Override
	protected void onStop() {

		if (selectionUpdatingDisposable != null)
			selectionUpdatingDisposable.dispose();

		if (progressCalculationDisposable != null)
			progressCalculationDisposable.dispose();

		busDisposables.clear();

		super.onStop();
	}

	@Override
	protected void onDestroy() {

		syncDisposable.dispose();

		if (dataFetchingDisposable != null)
			dataFetchingDisposable.dispose();

		unbinder.unbind();

		WidgetUtils.releaseResources();

		super.onDestroy();
	}
}
