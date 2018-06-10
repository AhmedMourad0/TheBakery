package inc.ahmedmourad.bakery.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import inc.ahmedmourad.bakery.R;
import inc.ahmedmourad.bakery.bus.RxBus;
import inc.ahmedmourad.bakery.model.room.entities.StepEntity;

public class StepsRecyclerAdapter extends RecyclerView.Adapter<StepsRecyclerAdapter.ViewHolder> {

	private List<StepEntity> stepsList = new ArrayList<>();

	private int selectedStepPosition = -1;

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull final ViewGroup container, final int viewType) {
		return new ViewHolder(LayoutInflater.from(container.getContext()).inflate(R.layout.item_step, container, false));
	}

	@Override
	public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
		holder.bind(position, stepsList.get(position));
	}

	@Override
	public int getItemCount() {
		return stepsList.size();
	}

	public void updateSteps(final List<StepEntity> stepsList) {
		this.stepsList = stepsList;
		notifyDataSetChanged();
	}

	/**
	 * Used with the master detail flow to select a certain step
	 *
	 * @param position step to be selected
	 * @return whether the selection was successful or not
	 */
	public boolean select(final int position) {

		if (position == selectedStepPosition || position == -1 || position >= getItemCount())
			return false;

		final int oldPosition = selectedStepPosition;

		selectedStepPosition = position;

		if (oldPosition != -1 && oldPosition < getItemCount())
			notifyItemChanged(oldPosition);

		notifyItemChanged(selectedStepPosition);

		return true;
	}

	/**
	 * Used with the master detail flow to clear selection
	 */
	public void clearSelection() {

		final int oldPosition = selectedStepPosition;
		selectedStepPosition = -1;

		if (oldPosition != -1 && oldPosition < getItemCount())
			notifyItemChanged(oldPosition);
	}

	class ViewHolder extends RecyclerView.ViewHolder {

		@BindView(R.id.step_short_description)
		TextView shortDescriptionTextView;

		@BindView(R.id.step_thumbnail)
		ImageView thumbnailImageView;

		private final Picasso picasso;

		ViewHolder(final View view) {
			super(view);
			ButterKnife.bind(this, view);
			picasso = Picasso.get();
		}

		private void bind(final int position, final StepEntity step) {

			final Context context = itemView.getContext();

			if (context.getResources().getBoolean(R.bool.useMasterDetailFlow)) {
				if (position == selectedStepPosition)
					itemView.setBackgroundColor(Color.parseColor("#BBDEFB"));
				else
					itemView.setBackgroundColor(Color.TRANSPARENT);
			}

			//TODO: size
			if (!TextUtils.isEmpty(step.thumbnailUrl))
				picasso.load(step.thumbnailUrl)
						.placeholder(R.drawable.ic_play_circle)
						.error(R.drawable.ic_play_circle)
						.into(thumbnailImageView);

			shortDescriptionTextView.setText(context.getString(R.string.step_title, (position + 1), step.shortDescription));

			itemView.setOnClickListener(v -> {

				if (context.getResources().getBoolean(R.bool.useMasterDetailFlow))
					select(position);

				RxBus.getInstance().selectStep(position);
			});
		}
	}
}
