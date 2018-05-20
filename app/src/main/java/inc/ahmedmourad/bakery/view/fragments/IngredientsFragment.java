package inc.ahmedmourad.bakery.view.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import inc.ahmedmourad.bakery.other.BundledFragment;
import inc.ahmedmourad.bakery.utils.ErrorUtils;
import inc.ahmedmourad.bakery.utils.OrientationUtils;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class IngredientsFragment extends BundledFragment {

	private static final String ARG_RECIPE_ID = "ri";

	private static final String STATE_RECYCLER_VIEW = "ingredients_rv";

	@BindView(R.id.ingredients_recycler_view)
	RecyclerView recyclerView;

	private int recipeId = -1;

	private Context context;

	private IngredientsRecyclerAdapter recyclerAdapter;

	private Single<List<IngredientEntity>> ingredientsSingle;

	private Disposable ingredientsDisposable;

	private final CompositeDisposable disposables = new CompositeDisposable();

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
		recyclerView.setHasFixedSize(true);
	}

	private void loadIngredients() {

		ingredientsDisposable = ingredientsSingle.subscribe(ingredientsList -> {

			recyclerAdapter.updateIngredients(ingredientsList);

			restoreInstanceState();

		}, throwable -> ErrorUtils.general(context, throwable));
	}

	@Override
	public void onStart() {
		super.onStart();

		RxBus.getInstance().showBackButton(true);
		RxBus.getInstance().showAddToWidgetButton(true);
		RxBus.getInstance().showSwitch(false);
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
	public void onDestroy() {

		if (unbinder != null)
			unbinder.unbind();

		super.onDestroy();
	}

	@NonNull
	@Override
	public Bundle saveState() {

		Bundle state = new Bundle();

		if (recyclerView != null)
			state.putParcelable(STATE_RECYCLER_VIEW, recyclerView.getLayoutManager().onSaveInstanceState());

		return state;
	}

	@Override
	public void restoreState(Bundle stateBundle) {

		if (stateBundle != null)
			instanceState = stateBundle;

		if (recyclerAdapter != null && recyclerAdapter.getItemCount() != 0)
			restoreInstanceState();
	}

	private synchronized void restoreInstanceState() {

		if (instanceState != null) {

			final Parcelable recyclerViewState = instanceState.getParcelable(STATE_RECYCLER_VIEW);

			if (recyclerViewState != null)
				recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);

			instanceState = null;
		}
	}
}
