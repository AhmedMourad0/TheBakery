package inc.ahmedmourad.bakery.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import inc.ahmedmourad.bakery.R;
import inc.ahmedmourad.bakery.adapters.RecipesRecyclerAdapter;
import inc.ahmedmourad.bakery.bus.RxBus;
import inc.ahmedmourad.bakery.model.room.database.BakeryDatabase;
import inc.ahmedmourad.bakery.model.room.entities.RecipeEntity;

public class RecipesFragment extends Fragment {

    @BindView(R.id.recipes_recycler_view)
    RecyclerView recyclerView;

    private RecipesRecyclerAdapter recyclerAdapter;

    private List<RecipeEntity> recipesList = new ArrayList<>(4);

    private Unbinder unbinder;

    @NonNull
    public static RecipesFragment newInstance() {
        return new RecipesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_recipes, container, false);

        unbinder = ButterKnife.bind(this, view);

        RxBus.getInstance().showProgress(false);

        BakeryDatabase.getInstance(view.getContext())
                .recipesDao()
                .getRecipes()
                .observe(this, recipes -> {
                    if (recipes != null) {
                        recipesList.clear();
                        recipesList.addAll(recipes);
                        recyclerAdapter.notifyDataSetChanged();
                    }
                });

        initializeRecyclerView(view.getContext());

        return view;
    }

    private void initializeRecyclerView(Context context) {
        recyclerAdapter = new RecipesRecyclerAdapter(recipesList);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setVerticalScrollBarEnabled(true);
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }
}
