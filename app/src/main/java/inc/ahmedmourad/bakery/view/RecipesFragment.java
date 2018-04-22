package inc.ahmedmourad.bakery.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import inc.ahmedmourad.bakery.R;
import inc.ahmedmourad.bakery.adapters.RecipesRecyclerAdapter;
import inc.ahmedmourad.bakery.bus.RxBus;
import inc.ahmedmourad.bakery.model.room.database.BakeryDatabase;
import inc.ahmedmourad.bakery.model.room.entities.RecipeEntity;
import inc.ahmedmourad.bakery.utils.ErrorUtils;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RecipesFragment extends Fragment {

    @BindView(R.id.recipes_recycler_view)
    RecyclerView recyclerView;

    private Context context;

    private RecipesRecyclerAdapter recyclerAdapter;

    private Single<List<RecipeEntity>> recipesSingle;

    private Disposable recipesDisposable;

    private Unbinder unbinder;

    @NonNull
    public static RecipesFragment newInstance() {
        return new RecipesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_recipes, container, false);

        unbinder = ButterKnife.bind(this, view);

        context = view.getContext();

        RxBus.getInstance().showProgress(false);
        RxBus.getInstance().setTitle(getString(R.string.app_name));

        recipesSingle = BakeryDatabase.getInstance(context)
                .recipesDao()
                .getRecipes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        initializeRecyclerView(context);

        loadRecipes();

        return view;
    }

    private void loadRecipes() {
        recipesDisposable = recipesSingle.subscribe(recyclerAdapter::updateRecipes,
                throwable -> ErrorUtils.general(context, throwable));
    }

    private void initializeRecyclerView(Context context) {
        recyclerAdapter = new RecipesRecyclerAdapter();
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setVerticalScrollBarEnabled(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (recipesDisposable.isDisposed() && recyclerAdapter.getItemCount() == 0)
            loadRecipes();
    }

    @Override
    public void onStop() {
        recipesDisposable.dispose();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }
}
