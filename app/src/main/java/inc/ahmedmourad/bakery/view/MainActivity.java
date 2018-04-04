package inc.ahmedmourad.bakery.view;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.eralp.circleprogressview.CircleProgressView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import inc.ahmedmourad.bakery.R;
import inc.ahmedmourad.bakery.bus.RxBus;
import inc.ahmedmourad.bakery.model.room.database.BakeryDatabase;
import inc.ahmedmourad.bakery.utils.NetworkUtils;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    public static final String TAG_RECIPES = "recipes";
    public static final String TAG_INGREDIENTS = "ingredients";
    public static final String TAG_STEPS = "steps";
    public static final String TAG_PLAYER = "player";

    @BindView(R.id.main_toolbar)
    Toolbar toolbar;

    @BindView(R.id.main_progressbar)
    CircleProgressView progressBar;

    @BindView(R.id.main_fab)
    FloatingActionButton fab;

    private FragmentManager fragmentManager;

    private RecipesFragment recipesFragment;

    private int selectedRecipeId = -1;

    private Unbinder unbinder;

    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        unbinder = ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        final BakeryDatabase db = BakeryDatabase.getInstance(this);

        disposables.add(Single.<Boolean>create(emitter -> emitter.onSuccess(db.recipesDao().getCount() < 4))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(needsSync -> {
                    if (needsSync)
                        disposables.add(NetworkUtils.fetchRecipes(getApplicationContext(), db));
                }, throwable -> disposables.add(NetworkUtils.fetchRecipes(getApplicationContext(), db))));

        fragmentManager = getSupportFragmentManager();

        recipesFragment = RecipesFragment.newInstance();

        if (!isFinishing())
            fragmentManager.beginTransaction()
                    .add(R.id.main_container, recipesFragment, TAG_RECIPES)
                    .addToBackStack(TAG_RECIPES)
                    .commit();

        fab.setOnClickListener(v -> {

            StepsFragment stepsFragment = StepsFragment.newInstance(selectedRecipeId);

            if (!isFinishing())
                fragmentManager.beginTransaction()
                        .replace(R.id.main_container, stepsFragment, TAG_STEPS)
                        .addToBackStack(TAG_STEPS)
                        .commit();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        disposables.add(RxBus.getInstance()
                .getRecipeSelectionRelay()
                .subscribe(id -> {

                    selectedRecipeId = id;

                    IngredientsFragment ingredientsFragment = IngredientsFragment.newInstance(id);

                    if (!isFinishing())
                        fragmentManager.beginTransaction()
                                .replace(R.id.main_container, ingredientsFragment, TAG_INGREDIENTS)
                                .addToBackStack(TAG_INGREDIENTS)
                                .commit();

                }, throwable -> {

                    BakeryDatabase.handleError(this, throwable);

                    if (!isFinishing())
                        fragmentManager.beginTransaction()
                                .replace(R.id.main_container, recipesFragment, TAG_RECIPES)
                                .addToBackStack(TAG_RECIPES)
                                .commit();
                }));

        // You may think it's an overkill to stream the whole list to calculate progress
        // instead of just sending an int to add to existing progress,
        // but doing that caused unexpected problems when the user clicked multiple
        // items at the same time which was only fixed using this approach
        disposables.add(RxBus.getInstance()
                .getIngredientsProgressRelay()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ingredients -> {

                    if (ingredients.isEmpty())
                        progressBar.setProgress(0f);
                    else
                        disposables.add(Observable.fromIterable(ingredients)
                                .filter(ingredient -> ingredient.isSelected)
                                .count()
                                .map(selectedCount -> 100f / ingredients.size() * selectedCount)
                                .subscribe(progress -> progressBar.setProgressWithAnimation(progress, 100),
                                        throwable -> BakeryDatabase.handleError(this, throwable))
                        );

                }, throwable -> BakeryDatabase.handleError(this, throwable))
        );

        disposables.add(RxBus.getInstance()
                .getProgressVisibilityRelay()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(visible -> progressBar.setVisibility(visible ? View.VISIBLE : View.GONE),
                        throwable -> BakeryDatabase.handleError(this, throwable))
        );

        disposables.add(RxBus.getInstance()
                .getFabVisibilityRelay()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(visible -> {

                    if (visible)
                        fab.show();
                    else
                        fab.hide();

                }, throwable -> BakeryDatabase.handleError(this, throwable))
        );

        disposables.add(RxBus.getInstance()
                .getStepSelectionRelay()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stepId -> {

                    PlayerFragment playerFragment = PlayerFragment.newInstance(selectedRecipeId, stepId);

                    if (!isFinishing())
                        fragmentManager.beginTransaction()
                                .replace(R.id.main_container, playerFragment, TAG_PLAYER)
                                .addToBackStack(TAG_PLAYER)
                                .commit();

                }, throwable -> {

                    // TODO:
                    BakeryDatabase.handleError(this, throwable);

                    if (!isFinishing())
                        fragmentManager.beginTransaction()
                                .replace(R.id.main_container, recipesFragment, TAG_RECIPES)
                                .addToBackStack(TAG_RECIPES)
                                .commit();

                }));
    }

    static void handleError(FragmentActivity activity, Throwable throwable) {

        BakeryDatabase.handleError(activity, throwable);

        if (activity != null && !activity.isFinishing())
            activity.getSupportFragmentManager().popBackStackImmediate(MainActivity.TAG_RECIPES, 0);
    }

    @Override
    protected void onStop() {
        disposables.clear();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }
}
