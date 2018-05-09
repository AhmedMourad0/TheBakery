package inc.ahmedmourad.bakery.view.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import inc.ahmedmourad.bakery.R;
import inc.ahmedmourad.bakery.bus.RxBus;
import inc.ahmedmourad.bakery.utils.OrientationUtils;
import inc.ahmedmourad.bakery.view.activity.MainActivity;

public class MasterDetailFragment extends Fragment {

	private static final String ARG_RECIPE_ID = "ri";
	private static final String ARG_STEP_POSITION = "sp";
	private static final String ARG_SAVED_INSTANCE_STATE = "sis";

	private int recipeId = -1;
	private int stepPosition = -1;
	private Bundle instanceState;

	private Fragment stepsFragment;
	private Fragment playerFragment;

	@NonNull
	public static MasterDetailFragment newInstance(Bundle savedInstanceState, int recipeId, int stepPosition) {

		Bundle args = new Bundle();

		args.putInt(ARG_RECIPE_ID, recipeId);
		args.putInt(ARG_STEP_POSITION, stepPosition);

		if (savedInstanceState != null)
			args.putBundle(ARG_SAVED_INSTANCE_STATE, savedInstanceState);

		MasterDetailFragment fragment = new MasterDetailFragment();

		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			recipeId = getArguments().getInt(ARG_RECIPE_ID, 1);
			stepPosition = getArguments().getInt(ARG_STEP_POSITION, 0);
			instanceState = getArguments().getBundle(ARG_SAVED_INSTANCE_STATE);
		}
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.fragment_master_detail, container, false);

		if (instanceState != null && getActivity() != null) {
			stepsFragment = getActivity().getSupportFragmentManager().getFragment(instanceState, MainActivity.TAG_STEPS);
			playerFragment = getActivity().getSupportFragmentManager().getFragment(instanceState, MainActivity.TAG_PLAYER);
		}

		if (stepsFragment == null)
			stepsFragment = StepsFragment.newInstance(recipeId);

		if (playerFragment == null)
			playerFragment = PlayerFragment.newInstance(recipeId, stepPosition);



		if (getActivity() != null) {
//			getActivity().getSupportFragmentManager().beginTransaction().remove(stepsFragment).remove(playerFragment).commit();
//			getActivity().getSupportFragmentManager().executePendingTransactions();
		}

		getChildFragmentManager().beginTransaction()
				.replace(R.id.master_master_container, stepsFragment, MainActivity.TAG_STEPS)
				.addToBackStack(MainActivity.TAG_STEPS)
				.replace(R.id.master_detail_container, playerFragment, MainActivity.TAG_PLAYER)
				.addToBackStack(MainActivity.TAG_PLAYER)
				.commit();

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		RxBus.getInstance().showProgress(false);
		RxBus.getInstance().setSelectedRecipeId(recipeId);
		RxBus.getInstance().showBackButton(true);
		RxBus.getInstance().showAddToWidgetButton(true);
		RxBus.getInstance().showToolbar(true);

		OrientationUtils.reset(getActivity());
	}
}
