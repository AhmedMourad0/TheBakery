package inc.ahmedmourad.bakery.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import inc.ahmedmourad.bakery.adapters.IngredientsRecyclerAdapter;
import inc.ahmedmourad.bakery.bus.RxBus;
import inc.ahmedmourad.bakery.model.room.database.BakeryDatabase;
import inc.ahmedmourad.bakery.model.room.entities.IngredientEntity;
import inc.ahmedmourad.bakery.utils.ErrorUtils;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class IngredientsFragment extends Fragment {

    public static final String ARG_RECIPE_ID = "ri";

    @BindView(R.id.ingredients_recycler_view)
    RecyclerView recyclerView;

    private int id = -1;

    private Context context;

    private IngredientsRecyclerAdapter recyclerAdapter;

    private Single<List<IngredientEntity>> ingredientsSingle;

    private Disposable ingredientsDisposable;

    private CompositeDisposable disposables = new CompositeDisposable();

    private Unbinder unbinder;

    public static IngredientsFragment newInstance(int id) {

        Bundle args = new Bundle();

        args.putInt(ARG_RECIPE_ID, id);

        IngredientsFragment fragment = new IngredientsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
            id = getArguments().getInt(ARG_RECIPE_ID);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_ingredients, container, false);

        context = view.getContext();

        unbinder = ButterKnife.bind(this, view);

        initializeRecyclerView();

        RxBus.getInstance().updateProgress(new ArrayList<>(0));

        ingredientsSingle = BakeryDatabase.getInstance(context)
                .ingredientsDao()
                .getIngredientsByRecipeId(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        loadIngredients();

        return view;
    }

    private void initializeRecyclerView() {
        recyclerAdapter = new IngredientsRecyclerAdapter();
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setVerticalScrollBarEnabled(true);
    }

    private void loadIngredients() {
        ingredientsDisposable = ingredientsSingle.subscribe(recyclerAdapter::updateIngredients,
                throwable -> ErrorUtils.general(context, throwable));
    }

    @Override
    public void onStart() {
        super.onStart();

        RxBus.getInstance().showProgress(true);
        RxBus.getInstance().showFab(true);

        if (ingredientsDisposable.isDisposed() && recyclerAdapter.getItemCount() == 0)
            loadIngredients();

        disposables.add(RxBus.getInstance()
                .getIngredientsSelectionRelay()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(selected ->
                                recyclerAdapter.setAllSelected(selected),
                        throwable ->
                                ErrorUtils.general(context, throwable)
                )
        );
    }

    @Override
    public void onStop() {
        ingredientsDisposable.dispose();
        disposables.clear();
        RxBus.getInstance().showProgress(false);
        RxBus.getInstance().showFab(false);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }
}
