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
import inc.ahmedmourad.bakery.model.room.entities.RecipeEntity;

public class ConfigureRecyclerAdapter extends RecyclerView.Adapter<ConfigureRecyclerAdapter.ViewHolder> {

	private final OnConfigureRecipeSelected listener;

	// My crystal ball is telling me there will be 4 items
	private List<RecipeEntity> recipesList = new ArrayList<>(4);

	public ConfigureRecyclerAdapter(@NonNull final OnConfigureRecipeSelected listener) {
		this.listener = listener;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull final ViewGroup container, final int viewType) {
		return new ViewHolder(LayoutInflater.from(container.getContext()).inflate(R.layout.item_configure_recipe, container, false));
	}

	@Override
	public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
		holder.bind(recipesList.get(position));
	}

	@Override
	public int getItemCount() {
		return recipesList.size();
	}

	// I love this, looks so neat
	public void updateRecipes(final List<RecipeEntity> recipesList) {
		this.recipesList = recipesList;
		notifyDataSetChanged();
	}

	class ViewHolder extends RecyclerView.ViewHolder {

		@BindView(R.id.configure_recipe_name)
		TextView nameTextView;

		@BindView(R.id.configure_recipe_servings)
		TextView servingsTextView;

		@BindView(R.id.configure_recipe_image)
		ImageView imageView;

		private final Picasso picasso;

		ViewHolder(final View view) {
			super(view);
			ButterKnife.bind(this, view);
			picasso = Picasso.get();
		}

		private void bind(final RecipeEntity recipe) {

			//TODO: size
			if (!TextUtils.isEmpty(recipe.image))
				picasso.load(recipe.image)
						.placeholder(R.drawable.ic_cupcake)
						.error(R.drawable.ic_cupcake)
						.into(imageView);

			itemView.setOnClickListener(v -> listener.onConfigureRecipeSelected(recipe));

			nameTextView.setText(recipe.name);

			servingsTextView.setText(itemView.getContext().getResources().getQuantityString(R.plurals.servings, recipe.servings, recipe.servings));
		}
	}

	@FunctionalInterface
	public interface OnConfigureRecipeSelected {
		void onConfigureRecipeSelected(RecipeEntity recipe);
	}
}
