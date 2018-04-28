package inc.ahmedmourad.bakery.view;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.eralp.circleprogressview.CircleProgressView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import inc.ahmedmourad.bakery.R;
import inc.ahmedmourad.bakery.bus.RxBus;
import inc.ahmedmourad.bakery.model.room.database.BakeryDatabase;
import inc.ahmedmourad.bakery.utils.ErrorUtils;
import inc.ahmedmourad.bakery.utils.NetworkUtils;
import inc.ahmedmourad.bakery.utils.PreferencesUtils;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

	public static final String TAG_RECIPES = "recipes";
	public static final String TAG_INGREDIENTS = "ingredients";
	public static final String TAG_STEPS = "steps";
	public static final String TAG_PLAYER = "player";

	private static final String STATE_SELECTED_RECIPE_ID = "sri";
	private static final String STATE_CURRENT_FRAGMENT_TAG = "ft";

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

	@BindView(R.id.main_fab)
	FloatingActionButton fab;

	private FragmentManager fragmentManager;

	private int selectedRecipeId = -1;
	private String currentFragmentTag = TAG_RECIPES;

	private Unbinder unbinder;

	private CompositeDisposable disposables = new CompositeDisposable();

	private Disposable selectionUpdatingDisposable, dataFetchingDisposable, progressCalculationDisposable;

	private Fragment recipesFragment, ingredientsFragment, stepsFragment, playerFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		unbinder = ButterKnife.bind(this);

		setSupportActionBar(toolbar);

		final BakeryDatabase db = BakeryDatabase.getInstance(this);

		dataFetchingDisposable = db.recipesDao()
				.getCount()
				.subscribeOn(Schedulers.io())
				.observeOn(Schedulers.io())
				.map(count -> count < 4)
				.subscribe(needsSync -> {
					if (needsSync)
						disposables.add(NetworkUtils.fetchRecipes(getApplicationContext(), db));
				}, throwable -> disposables.add(NetworkUtils.fetchRecipes(getApplicationContext(), db)));

		fragmentManager = getSupportFragmentManager();

		if (savedInstanceState == null) {

			displayFragment(RecipesFragment.newInstance(), TAG_RECIPES);

		} else {

			selectedRecipeId = savedInstanceState.getInt(STATE_SELECTED_RECIPE_ID, -1);

			if (selectedRecipeId == -1) {

				displayFragment(RecipesFragment.newInstance(), TAG_RECIPES);

			} else {

				recipesFragment = ingredientsFragment = stepsFragment = playerFragment = null;

				currentFragmentTag = savedInstanceState.getString(STATE_CURRENT_FRAGMENT_TAG, TAG_RECIPES);

				final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

				if (!currentFragmentTag.equals(TAG_RECIPES)) {

					recipesFragment = fragmentManager.getFragment(savedInstanceState, TAG_RECIPES);
					fragmentTransaction.add(recipesFragment, TAG_RECIPES);

					if (!currentFragmentTag.equals(TAG_INGREDIENTS)) {

						ingredientsFragment = fragmentManager.getFragment(savedInstanceState, TAG_INGREDIENTS);
						fragmentTransaction.add(ingredientsFragment, TAG_INGREDIENTS);

						if (!currentFragmentTag.equals(TAG_STEPS)) {

							stepsFragment = fragmentManager.getFragment(savedInstanceState, TAG_STEPS);
							fragmentTransaction.add(stepsFragment, TAG_STEPS);

							if (!currentFragmentTag.equals(TAG_PLAYER)) {
								playerFragment = fragmentManager.getFragment(savedInstanceState, TAG_PLAYER);
								fragmentTransaction.add(playerFragment, TAG_PLAYER);
							}
						}
					}
				}

				final Fragment currentFragment = getCurrentFragment();

				if (!isFinishing())
					fragmentTransaction.replace(R.id.main_container, currentFragment, currentFragmentTag)
							.addToBackStack(currentFragmentTag)
							.commit();

				assignCurrentFragmentReference(currentFragmentTag, currentFragment);
			}
		}

		fab.setOnClickListener(v -> displayFragment(StepsFragment.newInstance(selectedRecipeId), TAG_STEPS));

		Completable selectionUpdatingCompletable = Completable.fromAction(() -> db.ingredientsDao().updateSelection(selectedRecipeId, Float.compare(progressBar.getProgress(), 0f) == 0))
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread());

		progressBar.setOnClickListener(v -> showIngredientsSelectionDialog(selectionUpdatingCompletable));

		switchCompat.setChecked(PreferencesUtils.defaultPrefs(this).getBoolean(PreferencesUtils.KEY_USE_AUTOPLAY, true));

		switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> PreferencesUtils.edit(this, e -> e.putBoolean(PreferencesUtils.KEY_USE_AUTOPLAY, isChecked)));

		backButton.setOnClickListener(v -> {
			if (!isFinishing())
				getSupportFragmentManager().popBackStackImmediate();
		});
	}

	private Fragment getCurrentFragment() {

		switch (currentFragmentTag) {

			case TAG_RECIPES:
				return recipesFragment;

			case TAG_INGREDIENTS:
				return ingredientsFragment;

			case TAG_STEPS:
				return stepsFragment;

			case TAG_PLAYER:
				return playerFragment;

			default:
				return recipesFragment;
		}
	}

	private void assignCurrentFragmentReference(String fragmentTag, Fragment fragment) {

		switch (fragmentTag) {

			case TAG_RECIPES:
				recipesFragment = fragment;
				ingredientsFragment = null;
				stepsFragment = null;
				playerFragment = null;
				break;

			case TAG_INGREDIENTS:
				ingredientsFragment = fragment;
				stepsFragment = null;
				playerFragment = null;
				break;

			case TAG_STEPS:
				stepsFragment = fragment;
				playerFragment = null;
				break;

			case TAG_PLAYER:
				playerFragment = fragment;
				break;
		}
	}

	private void clearFragmentReferences() {

		switch (currentFragmentTag) {

			case TAG_RECIPES:
				ingredientsFragment = null;
				stepsFragment = null;
				playerFragment = null;
				break;

			case TAG_INGREDIENTS:
				stepsFragment = null;
				playerFragment = null;
				break;

			case TAG_STEPS:
				playerFragment = null;
				break;
		}
	}

	private void displayFragment(final Fragment fragment, final String tag) {

		if (!isFinishing()) {

			fragmentManager.beginTransaction()
					.replace(R.id.main_container, fragment, tag)
					.addToBackStack(tag)
					.commit();

			assignCurrentFragmentReference(tag, fragment);
		}
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

		disposables.add(RxBus.getInstance()
				.getRecipeSelectionRelay()
				.subscribe(id -> displayFragment(IngredientsFragment.newInstance(id), TAG_INGREDIENTS),
						throwable -> ErrorUtils.critical(this, throwable))
		);

		// You may think it's an overkill to stream the whole list to calculate progress
		// instead of just sending an int to add to existing progress,
		// but doing that caused unexpected problems when the user clicked multiple
		// items at the same time which was only fixed using this approach
		disposables.add(RxBus.getInstance()
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

		disposables.add(RxBus.getInstance()
				.getProgressVisibilityRelay()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(visibility -> progressBar.setVisibility(visibility),
						throwable -> ErrorUtils.general(this, throwable))
		);

		disposables.add(RxBus.getInstance()
				.getSwitchVisibilityRelay()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(visibility -> switchCompat.setVisibility(visibility),
						throwable -> ErrorUtils.general(this, throwable))
		);

		disposables.add(RxBus.getInstance()
				.getBackButtonVisibilityRelay()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(visibility -> backButton.setVisibility(visibility),
						throwable -> ErrorUtils.general(this, throwable))
		);

		disposables.add(RxBus.getInstance()
				.getFabVisibilityRelay()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(visible -> {

					if (visible)
						fab.show();
					else
						fab.hide();

				}, throwable -> ErrorUtils.general(this, throwable))
		);

		disposables.add(RxBus.getInstance()
				.getStepSelectionRelay()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(stepPosition -> displayFragment(PlayerFragment.newInstance(selectedRecipeId, stepPosition), TAG_PLAYER),
						throwable -> {
							ErrorUtils.general(this, throwable);
							displayFragment(StepsFragment.newInstance(selectedRecipeId), TAG_STEPS);
						})
		);

		disposables.add(RxBus.getInstance()
				.getTitleChangingRelay()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(title -> titleTextView.setText(title),
						throwable -> ErrorUtils.general(this, throwable))
		);

		disposables.add(RxBus.getInstance()
				.getSelectedRecipeIdRelay()
				.subscribe(id -> selectedRecipeId = id,
						throwable -> ErrorUtils.critical(this, throwable))
		);

		disposables.add(RxBus.getInstance()
				.getCurrentFragmentTagRelay()
				.subscribe(tag -> {
					currentFragmentTag = tag;
					clearFragmentReferences();
				}, throwable -> ErrorUtils.critical(this, throwable))
		);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

		fragmentManager.beginTransaction().remove(getCurrentFragment()).commit();

		super.onSaveInstanceState(outState);

		outState.putInt(STATE_SELECTED_RECIPE_ID, selectedRecipeId);
		outState.putString(STATE_CURRENT_FRAGMENT_TAG, currentFragmentTag);

		if (recipesFragment != null)
			fragmentManager.putFragment(outState, TAG_RECIPES, recipesFragment);

		if (ingredientsFragment != null)
			fragmentManager.putFragment(outState, TAG_INGREDIENTS, ingredientsFragment);

		if (stepsFragment != null)
			fragmentManager.putFragment(outState, TAG_STEPS, stepsFragment);

		if (playerFragment != null)
			fragmentManager.putFragment(outState, TAG_PLAYER, playerFragment);
	}


	@Override
	protected void onStop() {

		if (selectionUpdatingDisposable != null)
			selectionUpdatingDisposable.dispose();

		if (progressCalculationDisposable != null)
			progressCalculationDisposable.dispose();

		disposables.clear();

		super.onStop();
	}


	@Override
	protected void onDestroy() {
		dataFetchingDisposable.dispose();
		unbinder.unbind();
		super.onDestroy();
	}
}
