package inc.ahmedmourad.bakery.view.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import inc.ahmedmourad.bakery.other.BundledFragment;
import inc.ahmedmourad.bakery.utils.ErrorUtils;
import inc.ahmedmourad.bakery.utils.OrientationUtils;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class StepsFragment extends BundledFragment {

	private static final String ARG_RECIPE_ID = "ri";

	private static final String STATE_RECYCLER_VIEW = "steps_rv";

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.steps_recycler_view)
	RecyclerView recyclerView;

	private int recipeId = -1;

	private StepsRecyclerAdapter recyclerAdapter;

	private Single<List<StepEntity>> stepsSingle;

	private Disposable stepsDisposable;
	private Disposable stepsSelectionDisposable;

	private Unbinder unbinder;

	private volatile Bundle instanceState;

	public static StepsFragment newInstance(final int recipeId) {

		final Bundle args = new Bundle();

		args.putInt(ARG_RECIPE_ID, recipeId);

		final StepsFragment fragment = new StepsFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.e("00000000000000000000000", "onCreate");

		if (getArguments() != null)
			recipeId = getArguments().getInt(ARG_RECIPE_ID);
	}

	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

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

		if (stepsDisposable != null)
			stepsDisposable.dispose();

		stepsDisposable = stepsSingle.subscribe(stepsList -> {

			recyclerAdapter.updateSteps(stepsList);

			restoreInstanceState();

		}, throwable -> ErrorUtils.critical(getActivity(), throwable));
	}

	private void initializeRecyclerView(final Context context) {
		recyclerAdapter = new StepsRecyclerAdapter();
		recyclerView.setAdapter(recyclerAdapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
		recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
		recyclerView.setVerticalScrollBarEnabled(true);
		recyclerView.setHasFixedSize(true);
	}

	@Override
	public void onStart() {
		super.onStart();

		Log.e("00000000000000000000000", "onStart");

		RxBus.getInstance().showBackButton(true);
		RxBus.getInstance().showAddToWidgetButton(true);
		RxBus.getInstance().showToolbar(true);
		RxBus.getInstance().showSwitch(false);
		RxBus.getInstance().showFab(false);
		RxBus.getInstance().showProgress(false);

		if (getResources().getBoolean(R.bool.useMasterDetailFlow)) {
			stepsSelectionDisposable = RxBus.getInstance()
					.getSelectedStepPositionRelay()
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(position -> {

						if (position == -1)
							recyclerAdapter.clearSelection();
						else if (recyclerAdapter.select(position))
							recyclerView.scrollToPosition(position);

					}, throwable -> ErrorUtils.general(getActivity(), throwable));
		}

		OrientationUtils.reset(getActivity());

		if (stepsDisposable.isDisposed() && recyclerAdapter.getItemCount() == 0)
			loadSteps();
	}

	@Override
	public void onStop() {

		stepsDisposable.dispose();

		if (stepsSelectionDisposable != null)
			stepsSelectionDisposable.dispose();

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

		final Bundle state = new Bundle();

		if (recyclerView != null)
			state.putParcelable(STATE_RECYCLER_VIEW, recyclerView.getLayoutManager().onSaveInstanceState());

		return state;
	}

	@Override
	public void restoreState(final Bundle stateBundle) {

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
