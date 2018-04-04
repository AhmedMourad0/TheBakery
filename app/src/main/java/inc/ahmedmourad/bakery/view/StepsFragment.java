package inc.ahmedmourad.bakery.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
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
import inc.ahmedmourad.bakery.adapters.StepsRecyclerAdapter;
import inc.ahmedmourad.bakery.model.room.database.BakeryDatabase;
import inc.ahmedmourad.bakery.model.room.entities.StepEntity;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class StepsFragment extends Fragment {

    public static final String ARG_RECIPE_ID = "ri";

    @BindView(R.id.steps_recycler_view)
    RecyclerView recyclerView;

    private int id = -1;

    private StepsRecyclerAdapter recyclerAdapter;

    private List<StepEntity> stepsList = new ArrayList<>();

    private Disposable disposable;

    private Unbinder unbinder;

    public static StepsFragment newInstance(int id) {

        Bundle args = new Bundle();

        args.putInt(ARG_RECIPE_ID, id);

        StepsFragment fragment = new StepsFragment();
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

        final View view = inflater.inflate(R.layout.fragment_steps, container, false);

        final Context context = view.getContext();

        unbinder = ButterKnife.bind(this, view);

        disposable = Single.<List<StepEntity>>create(emitter ->
                emitter.onSuccess(BakeryDatabase.getInstance(context)
                        .stepsDao()
                        .getStepsByRecipeId(id)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(steps -> {

                    if (steps != null) {
                        stepsList.clear();
                        stepsList.addAll(steps);
                        recyclerAdapter.notifyDataSetChanged();
                    }

                }, throwable -> MainActivity.handleError(getActivity(), throwable));

        initializeRecyclerView(context);

        return view;
    }

    private void initializeRecyclerView(Context context) {
        recyclerAdapter = new StepsRecyclerAdapter(stepsList);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        recyclerView.setVerticalScrollBarEnabled(true);
    }

    @Override
    public void onStop() {
        disposable.dispose();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }
}
