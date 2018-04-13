package inc.ahmedmourad.bakery.adapters;

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

    public void updateSteps(List<StepEntity> stepsList) {
        this.stepsList = stepsList;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.step_short_description)
        TextView shortDescription;

        @BindView(R.id.step_thumbnail)
        ImageView thumbnail;

        private Picasso picasso;

        ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
            picasso = Picasso.get();
        }

        private void bind(final int position, final StepEntity step) {

            //TODO: size
            if (!TextUtils.isEmpty(step.thumbnailUrl))
                picasso.load(step.thumbnailUrl)
                        .placeholder(R.drawable.ic_play_circle)
                        .error(R.drawable.ic_play_circle)
                        .into(thumbnail);

            shortDescription.setText(itemView.getContext().getString(R.string.step_title, (position + 1), step.shortDescription));

            itemView.setOnClickListener(v -> RxBus.getInstance().selectStep(position));
        }
    }
}
