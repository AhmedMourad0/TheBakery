package inc.ahmedmourad.bakery.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import inc.ahmedmourad.bakery.R;
import inc.ahmedmourad.bakery.adapters.StepsRecyclerAdapter;
import inc.ahmedmourad.bakery.bus.RxBus;
import inc.ahmedmourad.bakery.model.room.database.BakeryDatabase;
import inc.ahmedmourad.bakery.model.room.entities.StepEntity;
import inc.ahmedmourad.bakery.utils.ErrorUtils;
import inc.ahmedmourad.bakery.utils.OrientationUtils;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class StepsFragment extends Fragment {

	public static final String ARG_RECIPE_ID = "ri";

	private static final String STATE_RECYCLER_VIEW = "steps_rv";

	@BindView(R.id.steps_recycler_view)
	RecyclerView recyclerView;

	private int recipeId = -1;

	private StepsRecyclerAdapter recyclerAdapter;

	private Single<List<StepEntity>> stepsSingle;

	private Disposable disposable;

	private Unbinder unbinder;

	private volatile Bundle instanceState;

	public static StepsFragment newInstance(int recipeId) {

		Bundle args = new Bundle();

		args.putInt(ARG_RECIPE_ID, recipeId);

		StepsFragment fragment = new StepsFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.e("00000000000000000000000", "onCreate");

		if (getArguments() != null)
			recipeId = getArguments().getInt(ARG_RECIPE_ID);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		Log.e("00000000000000000000000", "onCreateView");

		final View view = inflater.inflate(R.layout.fragment_steps, container, false);

		final Context context = view.getContext();

		unbinder = ButterKnife.bind(this, view);

		stepsSingle = BakeryDatabase.getInstance(context)
				.stepsDao()
				.getStepsByRecipeId(recipeId)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread());

		initializeRecyclerView(context);

		loadSteps();

		return view;
	}

	private void loadSteps() {

		if (disposable != null)
			disposable.dispose();

		disposable = stepsSingle.subscribe(stepsList -> {

			recyclerAdapter.updateSteps(stepsList);

			restoreInstanceState();

		}, throwable -> ErrorUtils.critical(getActivity(), throwable));
	}

	private synchronized void restoreInstanceState() {

		if (instanceState != null) {

			final Parcelable recyclerViewState = instanceState.getParcelable(STATE_RECYCLER_VIEW);

			if (recyclerViewState != null)
				recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);

			instanceState = null;
		}
	}

	private void initializeRecyclerView(Context context) {
		recyclerAdapter = new StepsRecyclerAdapter();
		recyclerView.setAdapter(recyclerAdapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
		recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
		recyclerView.setVerticalScrollBarEnabled(true);
	}

	@Override
	public void onStart() {
		super.onStart();

		Log.e("00000000000000000000000", "onStart");

		RxBus.getInstance().setCurrentFragmentId(MainActivity.FRAGMENT_STEPS);
		RxBus.getInstance().showBackButton(true);
		RxBus.getInstance().showToolbar(true);

		OrientationUtils.reset(getActivity());

		if (disposable.isDisposed() && recyclerAdapter.getItemCount() == 0)
			loadSteps();
	}

	@Override
	public void onStop() {
		disposable.dispose();
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
