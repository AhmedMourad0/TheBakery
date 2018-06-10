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
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import inc.ahmedmourad.bakery.R;
import inc.ahmedmourad.bakery.bus.RxBus;
import inc.ahmedmourad.bakery.model.room.database.BakeryDatabase;
import inc.ahmedmourad.bakery.model.room.entities.IngredientEntity;
import inc.ahmedmourad.bakery.utils.ErrorUtils;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class IngredientsRecyclerAdapter extends RecyclerView.Adapter<IngredientsRecyclerAdapter.ViewHolder> {

	private List<IngredientEntity> ingredientsList = new ArrayList<>();

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
	public int getItemViewType(final int position) {
		return TimelineView.getTimeLineViewType(position, getItemCount());
	}

	public void updateIngredients(final List<IngredientEntity> ingredientsList) {

		this.ingredientsList = ingredientsList;

		notifyDataSetChanged();

		RxBus.getInstance().updateProgress(ingredientsList);
	}

	public void setAllSelected(final Boolean selected) {

		IngredientEntity ingredient;

		for (int i = 0; i < ingredientsList.size(); ++i) {

			ingredient = ingredientsList.get(i);

			if (ingredient.isSelected != selected) {
				ingredient.isSelected = selected;
				notifyItemChanged(i);
			}
		}
	}

	class ViewHolder extends RecyclerView.ViewHolder {

		@BindView(R.id.ingredient_card)
		CardView cardView;

		@BindView(R.id.ingredient_name)
		TextView nameTextView;

		@BindView(R.id.ingredient_quantity)
		TextView quantityTextView;

		@BindView(R.id.ingredient_timeline)
		TimelineView timeline;

		private final BakeryDatabase db;

		private Disposable disposable;

		ViewHolder(final View view, final int viewType) {
			super(view);
			ButterKnife.bind(this, view);
			timeline.initLine(viewType);

			db = BakeryDatabase.getInstance(view.getContext());
		}

		private void bind(final int position, @NonNull final IngredientEntity ingredient) {

			final Completable selectionUpdatingCompletable = Completable.fromAction(() -> db.ingredientsDao().update(ingredient))
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread());

			cardView.setOnClickListener(v -> {

				ingredient.isSelected = !ingredient.isSelected;

				if (disposable != null)
					disposable.dispose();

				disposable = selectionUpdatingCompletable.subscribe(() -> {

							notifyItemChanged(position);
							RxBus.getInstance().updateProgress(ingredientsList);

						}, throwable -> {

					// rollback
							ErrorUtils.general(cardView.getContext(), throwable);
							ingredient.isSelected = !ingredient.isSelected;
							notifyItemChanged(position);
						}
				);
			});

			updateTimeLineMarker(itemView.getContext(), ingredient.isSelected);

			nameTextView.setText(ingredient.ingredient);

			quantityTextView.setText(itemView.getContext().getString(R.string.ingredient_quantity,
					BigDecimal.valueOf(ingredient.quantity).stripTrailingZeros().toPlainString(),
					ingredient.measure.toLowerCase())
			);
		}

		private void updateTimeLineMarker(final Context context, final boolean isSelected) {
			if (isSelected)
				timeline.setMarker(ContextCompat.getDrawable(context, R.drawable.marker_selected));
			else
				timeline.setMarker(ContextCompat.getDrawable(context, R.drawable.marker_unselected));
		}
	}
}
