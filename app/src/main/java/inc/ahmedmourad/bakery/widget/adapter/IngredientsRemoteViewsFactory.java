package inc.ahmedmourad.bakery.widget.adapter;

import android.content.Context;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import inc.ahmedmourad.bakery.R;
import inc.ahmedmourad.bakery.model.room.database.BakeryDatabase;
import inc.ahmedmourad.bakery.model.room.entities.IngredientEntity;
import inc.ahmedmourad.bakery.utils.ErrorUtils;
import io.reactivex.disposables.Disposable;

public class IngredientsRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

	private Context context;

	private int recipeId;

	private List<IngredientEntity> ingredients = new ArrayList<>(0);

	private Disposable disposable;

	IngredientsRemoteViewsFactory(Context context, int recipeId) {
		this.context = context.getApplicationContext();
		this.recipeId = recipeId;
	}

	@Override
	public void onCreate() {

	}

	@Override
	public void onDataSetChanged() {

		if (disposable != null)
			disposable.dispose();

		disposable = BakeryDatabase.getInstance(context)
				.ingredientsDao()
				.getIngredientsByRecipeId(recipeId)
				.subscribe(ingredients -> this.ingredients = ingredients,
						throwable -> ErrorUtils.general(context, throwable));
	}

	@Override
	public void onDestroy() {
		disposable.dispose();
	}

	@Override
	public int getCount() {
		return ingredients.size();
	}

	@Override
	public RemoteViews getViewAt(int position) {

		if (ingredients.size() == 0)
			return null;

		IngredientEntity ingredient = ingredients.get(position);

		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.item_widget_ingredient);

		views.setTextViewText(R.id.widget_ingredient_name, ingredient.ingredient);

		views.setTextViewText(R.id.widget_ingredient_quantity, context.getString(R.string.ingredient_quantity,
				BigDecimal.valueOf(ingredient.quantity).stripTrailingZeros().toPlainString(),
				ingredient.measure.toLowerCase())
		);

		// Fill in the onClick PendingIntent Template using the specific plant Id for each item individually
//		Bundle extras = new Bundle();
//		extras.putLong(PlantDetailActivity.EXTRA_PLANT_ID, plantId);
//		Intent fillInIntent = new Intent();
//		fillInIntent.putExtras(extras);
//		views.setOnClickFillInIntent(R.id.widget_plant_image, fillInIntent);

		return views;
	}

	@Override
	public RemoteViews getLoadingView() {
		return null;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public long getItemId(int position) {
		return ingredients.get(position).id;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}
}
