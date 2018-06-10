package inc.ahmedmourad.bakery.view.activity;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
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
import inc.ahmedmourad.bakery.utils.OrientationUtils;
import inc.ahmedmourad.bakery.utils.PreferencesUtils;
import inc.ahmedmourad.bakery.utils.WidgetSelectorUtils;
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
	private static final String TAG_STEPS = "steps";
	private static final String TAG_PLAYER = "player";

	public static final int FRAGMENT_RECIPES = 0;
	private static final int FRAGMENT_INGREDIENTS = 1;
	private static final int FRAGMENT_STEPS = 2;
	private static final int FRAGMENT_PLAYER = 3;

	private static final String STATE_SELECTED_RECIPE_ID = "main_sri";
	private static final String STATE_CURRENT_FRAGMENT_ID = "main_cfi";
	private static final String STATE_SELECTED_STEP_POSITION = "main_ssp";
	private static final String STATE_TITLE = "main_t";
	private static final String STATE_IS_INGREDIENTS_PROGRESS_DIALOG_SHOWN = "main_iipds";
	private static final String STATE_WIDGET_DIALOG_RECIPE_ID = "main_wdri";

	private static final int DURATION_ANIMATION = 100;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.main_toolbar)
	Toolbar toolbar;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.main_title)
	TextView titleTextView;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.main_progressbar)
	CircleProgressView progressBar;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.main_switch)
	SwitchCompat switchCompat;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.main_up)
	ImageView upButton;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.main_add_to_widget)
	ImageView addToWidgetButton;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.main_fab)
	FloatingActionButton fab;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.main_appbar)
	AppBarLayout appbar;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.main_details_container)
	FrameLayout detailsContainer;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.main_root_container)
	LinearLayout rootContainer;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.main_divider)
	View dividerView;

	private FragmentManager fragmentManager;

	private int selectedRecipeId = -1;
	private int selectedStepPosition = -1;
	private int currentFragmentId = FRAGMENT_RECIPES;

	private BundledFragment recipesFragment;
	private BundledFragment ingredientsFragment;
	private BundledFragment stepsFragment;
	private BundledFragment playerFragment;

	private Completable selectionUpdatingCompletable;

	private Unbinder unbinder;

	private final CompositeDisposable busDisposables = new CompositeDisposable();

	private Disposable selectionUpdatingDisposable;
	private Disposable progressCalculationDisposable;

	private boolean isIngredientsProgressDialogShown = false;
	private int widgetDialogRecipeId = -1;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		unbinder = ButterKnife.bind(this);

		setSupportActionBar(toolbar);

		final BakeryDatabase db = BakeryDatabase.getInstance(this);

		fragmentManager = getSupportFragmentManager();

		attachBusSubscribers();

		// if the current progress equals 0, set all ingredients as selected in the database
		selectionUpdatingCompletable = Completable.fromAction(() -> db.ingredientsDao().updateSelection(selectedRecipeId, Float.compare(progressBar.getProgress(), 0f) == 0))
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread());

		initializeOrRestoreInstanceFragments(savedInstanceState);

		fab.setOnClickListener(v -> {

			if (getResources().getBoolean(R.bool.useMasterDetailFlow)) {

				selectedStepPosition = 0;

				if (playerFragment != null)
					playerFragment.releaseResources();

				stepsFragment = StepsFragment.newInstance(selectedRecipeId);
				playerFragment = PlayerFragment.newInstance(selectedRecipeId, selectedStepPosition);

				moveToMasterDetailFlow();

			} else {
				displayFragment(StepsFragment.newInstance(selectedRecipeId), TAG_STEPS, true);
			}
		});

		progressBar.setOnClickListener(v -> showIngredientsSelectionDialog());

		switchCompat.setChecked(PreferencesUtils.defaultPrefs(this).getBoolean(PreferencesUtils.KEY_USE_AUTOPLAY, true));

		switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> PreferencesUtils.edit(this, e -> e.putBoolean(PreferencesUtils.KEY_USE_AUTOPLAY, isChecked)));

		upButton.setOnClickListener(v -> onBackPressed());

		addToWidgetButton.setOnClickListener(v -> WidgetSelectorUtils.startWidgetSelector(this, selectedRecipeId));
	}

	/**
	 * restores displayed fragments and backstack if the app is being re-initialized, otherwise it
	 * just initializes our fragments
	 *
	 * @param savedInstanceState savedInstanceState
	 */
	private void initializeOrRestoreInstanceFragments(@Nullable final Bundle savedInstanceState) {

		// currentFragmentId is our single source of truth as to the fragment currently being
		// displayed to the user
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

						if (playerFragment != null)
							playerFragment.releaseResources();

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

					if (currentFragmentId >= FRAGMENT_STEPS) {

						final Fragment detailsPlayerFragment = fragmentManager.findFragmentById(R.id.main_details_container);

						if (detailsPlayerFragment != null)
							fragmentManager.beginTransaction().remove(detailsPlayerFragment).commit();
					}

					if (currentFragmentId >= FRAGMENT_STEPS)
						restoreFragment(savedInstanceState, TAG_STEPS, true);

					if (currentFragmentId >= FRAGMENT_PLAYER) {
						if (selectedStepPosition == -1)
							restoreFragment(savedInstanceState, TAG_STEPS, true);
						else
							restoreFragment(savedInstanceState, TAG_PLAYER, true);
					}

					OrientationUtils.isTransactionDone = true;
				}

				titleTextView.setText(savedInstanceState.getCharSequence(STATE_TITLE));
			}

			final int recipeId = savedInstanceState.getInt(STATE_WIDGET_DIALOG_RECIPE_ID, -1);

			if (savedInstanceState.getBoolean(STATE_IS_INGREDIENTS_PROGRESS_DIALOG_SHOWN, false))
				showIngredientsSelectionDialog();
			else if (recipeId != -1)
				WidgetSelectorUtils.startWidgetSelector(this, recipeId);
		}
	}

	/**
	 * Displays our player and steps fragments side by side (MasterDetailFlow), changes
	 * currentFragmentId and changes the details container visibility to visible
	 */
	private void moveToMasterDetailFlow() {

		setDetailsContainerVisible(true);

		currentFragmentId = FRAGMENT_PLAYER;

		if (!isFinishing())
			fragmentManager.beginTransaction()
					.replace(R.id.main_master_container, stepsFragment, TAG_STEPS)
					.addToBackStack(TAG_STEPS)
					.replace(R.id.main_details_container, playerFragment, TAG_PLAYER)
					.addToBackStack(TAG_PLAYER)
					.commit();
	}

	/**
	 * changes the visibility of the detailsContainer and dividerView
	 *
	 * @param visible the new visibility
	 */
	private void setDetailsContainerVisible(final boolean visible) {
		if (visible) {
			detailsContainer.setVisibility(View.VISIBLE);
			dividerView.setVisibility(View.VISIBLE);
		} else {
			detailsContainer.setVisibility(View.GONE);
			dividerView.setVisibility(View.GONE);
		}
	}

	/**
	 * creates the fragment and restores its state
	 * @param savedInstanceState savedInstanceState
	 * @param tag the fragment tag
	 * @param attach whether or not to display the fragment to the user
	 * @return the fragment that was created and had its state restored
	 */
	private BundledFragment restoreFragment(final Bundle savedInstanceState, final String tag, final boolean attach) {

		final BundledFragment fragment;

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

	/**
	 * displays this fragment to the user and adds it to the backstack
	 * @param fragment the fragment to display
	 * @param tag the fragment tag
	 * @param updateCurrentFragmentId whether to update currentFragmentId or not
	 */
	private void displayFragment(final BundledFragment fragment, final String tag, final boolean updateCurrentFragmentId) {

		setDetailsContainerVisible(false);

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

	/**
	 * shows a dialog to prompt the user whether or not to update the ingredients selection progress
	 */
	private void showIngredientsSelectionDialog() {

		if (selectionUpdatingCompletable == null)
			return;

		final AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

		final int titleId;
		final int messageId;
		final int positiveTextId;

		final boolean isProgressZero = Float.compare(progressBar.getProgress(), 0f) == 0;

		if (isProgressZero) {
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

						RxBus.getInstance().setAllIngredientsSelected(isProgressZero);
						progressBar.setProgressWithAnimation(isProgressZero ? 100f : 0f, DURATION_ANIMATION);

					}, throwable -> ErrorUtils.general(this, throwable));

				}).create();

		final Resources resources = getResources();

		dialog.setOnShowListener(d -> {
			dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.colorSecondary));
			isIngredientsProgressDialogShown = true;
		});

		dialog.setOnDismissListener(d -> isIngredientsProgressDialogShown = false);

		dialog.show();
	}

	@Override
	protected void onStart() {
		super.onStart();
		attachBusSubscribers();
	}

	/**
	 * Attached subscribers to our bus streams
	 */
	private void attachBusSubscribers() {

		// making calling this method idempotent
		if (busDisposables.size() == 14)
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
				.getUpButtonVisibilityRelay()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(visibility -> upButton.setVisibility(visibility),
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

						// adjust behaviour to suit the new visibility
						final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) rootContainer.getLayoutParams();
						params.setBehavior(new AppBarLayout.ScrollingViewBehavior());

					} else if (!visible && appbar.getVisibility() != View.GONE) {

						appbar.setVisibility(View.GONE);

						// adjust behaviour to suit the new visibility
						final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) rootContainer.getLayoutParams();
						params.setBehavior(null);
					}

				}, throwable -> ErrorUtils.general(this, throwable))
		);

		busDisposables.add(RxBus.getInstance()
				.getStepSelectionRelay()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(stepPosition -> {

					if (!getResources().getBoolean(R.bool.useMasterDetailFlow))
						displayFragment(PlayerFragment.newInstance(selectedRecipeId, stepPosition), TAG_PLAYER, true);

				}, throwable -> {
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

		busDisposables.add(RxBus.getInstance()
				.getWidgetDialogRecipeIdRelay()
				.subscribe(recipeId -> widgetDialogRecipeId = recipeId,
						throwable -> ErrorUtils.general(this, throwable))
		);
	}


	@Override
	public void onWindowFocusChanged(final boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		// detects when the user drops down his notification panel thus potentially toggling auto rotation
		OrientationUtils.refreshSensorState(this);
	}

	@Override
	protected void onUserLeaveHint() {
		OrientationUtils.reset(this);
		super.onUserLeaveHint();
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {

		busDisposables.clear();

		final Bundle state = new Bundle();

		state.putInt(STATE_SELECTED_RECIPE_ID, selectedRecipeId);
		state.putInt(STATE_CURRENT_FRAGMENT_ID, currentFragmentId);
		state.putInt(STATE_SELECTED_STEP_POSITION, selectedStepPosition);
		state.putInt(STATE_WIDGET_DIALOG_RECIPE_ID, widgetDialogRecipeId);
		state.putBoolean(STATE_IS_INGREDIENTS_PROGRESS_DIALOG_SHOWN, isIngredientsProgressDialogShown);

		state.putCharSequence(STATE_TITLE, titleTextView.getText());

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

		if (isFinishing()) {

			if (playerFragment != null)
				playerFragment.releaseResources();

			fragmentManager.popBackStackImmediate(TAG_RECIPES, FragmentManager.POP_BACK_STACK_INCLUSIVE);

			for (int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i)
				fragmentManager.popBackStackImmediate();
		}

		super.onSaveInstanceState(outState);

		// we do this because we're not allow to mess with the fragments after onSaveInstanceState
		// and we need them -specifically the player fragment- to finish their life cycles before we leave
		// so that it doesn't continue playing in the background
		outState.putAll(state);
	}

	@Override
	public void onBackPressed() {

		setDetailsContainerVisible(false);

		if (currentFragmentId <= FRAGMENT_RECIPES) {
			finish();
		} else if (getResources().getBoolean(R.bool.useMasterDetailFlow) && currentFragmentId >= FRAGMENT_PLAYER) {
			fragmentManager.popBackStackImmediate(TAG_INGREDIENTS, 0);
			OrientationUtils.reset(this);
			currentFragmentId = FRAGMENT_INGREDIENTS;
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
		unbinder.unbind();
		WidgetSelectorUtils.releaseResources();
		super.onDestroy();
	}
}
