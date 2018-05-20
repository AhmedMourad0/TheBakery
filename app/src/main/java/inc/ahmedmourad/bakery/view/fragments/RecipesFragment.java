package inc.ahmedmourad.bakery.view.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
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
import inc.ahmedmourad.bakery.other.BundledFragment;
import inc.ahmedmourad.bakery.utils.ErrorUtils;
import inc.ahmedmourad.bakery.utils.OrientationUtils;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class RecipesFragment extends BundledFragment {

	private static final String STATE_RECYCLER_VIEW = "recipes_rv";

	@BindView(R.id.recipes_recycler_view)
	RecyclerView recyclerView;

	private Context context;

	private RecipesRecyclerAdapter recyclerAdapter;

	private Flowable<List<RecipeEntity>> recipesFlowable;

	private Disposable recipesDisposable;

	private Unbinder unbinder;

	private volatile Bundle instanceState;

	@NonNull
	public static RecipesFragment newInstance() {
		return new RecipesFragment();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.fragment_recipes, container, false);

		unbinder = ButterKnife.bind(this, view);

		context = view.getContext();

		recipesFlowable = BakeryDatabase.getInstance(context)
				.recipesDao()
				.getRecipes()
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread());

		initializeRecyclerView(context);

		loadRecipes();

		return view;
	}

	private void loadRecipes() {

		recipesDisposable = recipesFlowable.subscribe(recipesList -> {

			recyclerAdapter.updateRecipes(recipesList);

			restoreInstanceState();

		}, throwable -> ErrorUtils.general(context, throwable));
	}

	private void initializeRecyclerView(Context context) {
		recyclerAdapter = new RecipesRecyclerAdapter();
		recyclerView.setAdapter(recyclerAdapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
		recyclerView.setVerticalScrollBarEnabled(true);
		recyclerView.setHasFixedSize(true);
	}

	@Override
	public void onStart() {
		super.onStart();

		RxBus.getInstance().showProgress(false);
		RxBus.getInstance().setTitle(getString(R.string.app_name));
		RxBus.getInstance().showBackButton(false);
		RxBus.getInstance().showAddToWidgetButton(false);
		RxBus.getInstance().showToolbar(true);
		RxBus.getInstance().showProgress(false);
		RxBus.getInstance().showFab(false);

		OrientationUtils.reset(getActivity());

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
