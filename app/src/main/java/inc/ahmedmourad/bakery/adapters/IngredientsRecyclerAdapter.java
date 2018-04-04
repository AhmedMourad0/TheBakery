package inc.ahmedmourad.bakery.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.vipulasri.timelineview.TimelineView;

import java.math.BigDecimal;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import inc.ahmedmourad.bakery.R;
import inc.ahmedmourad.bakery.bus.RxBus;
import inc.ahmedmourad.bakery.model.room.entities.IngredientEntity;

public class IngredientsRecyclerAdapter extends RecyclerView.Adapter<IngredientsRecyclerAdapter.ViewHolder> {

    private final List<IngredientEntity> ingredientsList;

    public IngredientsRecyclerAdapter(@NonNull final List<IngredientEntity> ingredientsList) {
        this.ingredientsList = ingredientsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup container, final int viewType) {
        return new ViewHolder(LayoutInflater.from(container.getContext()).inflate(R.layout.item_ingredient, container, false), viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.bind(position, ingredientsList.get(position));
    }

    @Override
    public int getItemCount() {
        return ingredientsList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ingredient_card)
        CardView card;

        @BindView(R.id.ingredient_name)
        TextView name;

        @BindView(R.id.ingredient_quantity)
        TextView quantity;

        @BindView(R.id.ingredient_timeline)
        TimelineView timeline;

        ViewHolder(final View view, int viewType) {
            super(view);
            ButterKnife.bind(this, view);
            timeline.initLine(viewType);
        }

        private void bind(final int position, final IngredientEntity ingredient) {

            card.setOnClickListener(v -> {

                ingredient.isSelected = !ingredient.isSelected;

                notifyItemChanged(position);

                RxBus.getInstance().updateProgress(ingredientsList);
            });

            updateTimeLineMarker(itemView.getContext(), ingredient.isSelected);

            name.setText(ingredient.ingredient);

            quantity.setText(itemView.getContext().getString(
                    R.string.ingredient_quantity,
                    BigDecimal.valueOf(ingredient.quantity).stripTrailingZeros().toPlainString(),
                    ingredient.measure.toLowerCase())
            );
        }

        private void updateTimeLineMarker(Context context, boolean isSelected) {

            if (isSelected)
                timeline.setMarker(ContextCompat.getDrawable(context, R.drawable.marker_selected));
            else
                timeline.setMarker(ContextCompat.getDrawable(context, R.drawable.marker_unselected));
        }
    }
}
