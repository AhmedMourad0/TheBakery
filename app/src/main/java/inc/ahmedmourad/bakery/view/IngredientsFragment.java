package inc.ahmedmourad.bakery.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
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
import inc.ahmedmourad.bakery.utils.OrientationUtils;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class IngredientsFragment extends Fragment {

	public static final String ARG_RECIPE_ID = "ri";

	private static final String STATE_RECYCLER_VIEW = "ingredients_rv";

	@BindView(R.id.ingredients_recycler_view)
	RecyclerView recyclerView;

	private int recipeId = -1;

	private Context context;

	private IngredientsRecyclerAdapter recyclerAdapter;

	private Single<List<IngredientEntity>> ingredientsSingle;

	private Disposable ingredientsDisposable;

	private CompositeDisposable disposables = new CompositeDisposable();

	private Unbinder unbinder;

	private volatile Bundle instanceState;

	public static IngredientsFragment newInstance(int recipeId) {

		Bundle args = new Bundle();

		args.putInt(ARG_RECIPE_ID, recipeId);

		IngredientsFragment fragment = new IngredientsFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null)
			recipeId = getArguments().getInt(ARG_RECIPE_ID);
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
				.getIngredientsByRecipeId(recipeId)
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

		ingredientsDisposable = ingredientsSingle.subscribe(ingredientsList -> {

			recyclerAdapter.updateIngredients(ingredientsList);

			restoreInstanceState();

		}, throwable -> ErrorUtils.general(context, throwable));
	}

	private synchronized void restoreInstanceState() {

		if (instanceState != null) {

			final Parcelable recyclerViewState = instanceState.getParcelable(STATE_RECYCLER_VIEW);

			if (recyclerViewState != null)
				recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);

			instanceState = null;
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		RxBus.getInstance().setCurrentFragmentId(MainActivity.FRAGMENT_INGREDIENTS);
		RxBus.getInstance().showBackButton(true);
		RxBus.getInstance().showProgress(true);
		RxBus.getInstance().showFab(true);
		RxBus.getInstance().showToolbar(true);

		OrientationUtils.reset(getActivity());

		if (ingredientsDisposable.isDisposed() && recyclerAdapter.getItemCount() == 0)
			loadIngredients();

		disposables.add(RxBus.getInstance()
				.getIngredientsSelectionRelay()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(selected -> recyclerAdapter.setAllSelected(selected),
						throwable -> ErrorUtils.general(context, throwable))
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
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(STATE_RECYCLER_VIEW, recyclerView.getLayoutManager().onSaveInstanceState());
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (instanceState == null)
			instanceState = savedInstanceState;

		if (recyclerAdapter.getItemCount() != 0)
			restoreInstanceState();
	}

	@Override
	public void onDestroy() {
		unbinder.unbind();
		super.onDestroy();
	}
}
