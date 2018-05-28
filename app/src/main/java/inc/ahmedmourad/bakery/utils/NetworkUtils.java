package inc.ahmedmourad.bakery.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import inc.ahmedmourad.bakery.model.api.ApiClient;
import inc.ahmedmourad.bakery.model.api.ApiInterface;
import inc.ahmedmourad.bakery.model.room.database.BakeryDatabase;
import inc.ahmedmourad.bakery.model.room.entities.RecipeEntity;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public final class NetworkUtils {

	@NonNull
	public static Disposable fetchRecipes(final Context context, final BakeryDatabase db) {

		return ApiClient.getInstance()
				.create(ApiInterface.class)
				.getRecipes()
				.observeOn(Schedulers.io())
				.subscribeOn(Schedulers.io())
				.subscribe(recipeEntities -> db.runInTransaction(() -> {

					db.reset();
					db.recipesDao().bulkInsert(recipeEntities);

					for (final RecipeEntity recipeEntity : recipeEntities) {

						if (recipeEntity.ingredients != null) {

							for (int i = 0; i < recipeEntity.ingredients.size(); i++)
								recipeEntity.ingredients.get(i).recipeId = recipeEntity.id;
						}

						for (int i = 0; i < recipeEntity.steps.size(); i++)
							recipeEntity.steps.get(i).recipeId = recipeEntity.id;

						db.ingredientsDao().bulkInsert(recipeEntity.ingredients);
						db.stepsDao().bulkInsert(recipeEntity.steps);
					}

				}), throwable -> ErrorUtils.network(context, throwable));
	}
}
