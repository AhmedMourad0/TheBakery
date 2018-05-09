package inc.ahmedmourad.bakery.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import inc.ahmedmourad.bakery.R;
import inc.ahmedmourad.bakery.bus.RxBus;
import inc.ahmedmourad.bakery.model.room.entities.RecipeEntity;
import inc.ahmedmourad.bakery.utils.WidgetUtils;

public class RecipesRecyclerAdapter extends RecyclerView.Adapter<RecipesRecyclerAdapter.ViewHolder> {

	@NonNull
	private List<RecipeEntity> recipesList = new ArrayList<>(4);

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull final ViewGroup container, final int viewType) {
		return new ViewHolder(LayoutInflater.from(container.getContext()).inflate(R.layout.item_recipe, container, false));
	}

	@Override
	public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
		holder.bind(recipesList.get(position));
	}

	@Override
	public int getItemCount() {
		return recipesList.size();
	}

	public void updateRecipes(@NonNull final List<RecipeEntity> recipesList) {
		this.recipesList = recipesList;
		notifyDataSetChanged();
	}

	class ViewHolder extends RecyclerView.ViewHolder {

		@BindView(R.id.recipe_name)
		TextView nameTextView;

		@BindView(R.id.recipe_servings)
		TextView servingsTextView;

		@BindView(R.id.recipe_add_to_widget_button)
		Button addToWidgetButton;

		@BindView(R.id.recipe_image)
		ImageView imageView;

		private Picasso picasso;

		ViewHolder(final View view) {
			super(view);
			ButterKnife.bind(this, view);
			picasso = Picasso.get();
		}

		private void bind(final RecipeEntity recipe) {

			final Context context = itemView.getContext();

			//TODO: size
			if (!TextUtils.isEmpty(recipe.image))
				picasso.load(recipe.image)
						.placeholder(R.drawable.ic_cupcake)
						.error(R.drawable.ic_cupcake)
						.into(imageView);

			itemView.setOnClickListener(v -> {
				RxBus.getInstance().setSelectedRecipeId(recipe.id);
				RxBus.getInstance().selectRecipe(recipe.id);
				RxBus.getInstance().setTitle(recipe.name);
			});

			addToWidgetButton.setOnClickListener(v -> WidgetUtils.startWidgetChooser(context, recipe.id));

			nameTextView.setText(recipe.name);

			servingsTextView.setText(context.getResources().getQuantityString(R.plurals.servings, recipe.servings, recipe.servings));
		}
	}
}
