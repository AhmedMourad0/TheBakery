package inc.ahmedmourad.bakery.model.room.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

import inc.ahmedmourad.bakery.model.api.ApiClient;
import inc.ahmedmourad.bakery.model.room.daos.IngredientsDao;
import inc.ahmedmourad.bakery.model.room.daos.RecipesDao;
import inc.ahmedmourad.bakery.model.room.daos.StepsDao;
import inc.ahmedmourad.bakery.model.room.entities.IngredientEntity;
import inc.ahmedmourad.bakery.model.room.entities.RecipeEntity;
import inc.ahmedmourad.bakery.model.room.entities.StepEntity;

@Database(entities = {RecipeEntity.class, IngredientEntity.class, StepEntity.class}, version = 1)
public abstract class BakeryDatabase extends RoomDatabase {

	private static final String DATABASE_NAME = "BakeryDatabase";

	private static volatile BakeryDatabase INSTANCE = null;

	@NonNull
	public static BakeryDatabase getInstance(final Context context) {

		if (INSTANCE != null) {

			return INSTANCE;

		} else {

			synchronized (ApiClient.class) {
				return INSTANCE != null ? INSTANCE : (INSTANCE = buildDatabase(context));
			}
		}
	}

	@NonNull
	private static BakeryDatabase buildDatabase(final Context context) {

		return Room.databaseBuilder(
				context.getApplicationContext(),
				BakeryDatabase.class,
				BakeryDatabase.DATABASE_NAME).build();
	}

	public void reset() {
		stepsDao().deleteAll();
		recipesDao().deleteAll();
		ingredientsDao().deleteAll();
	}

	public abstract RecipesDao recipesDao();

	public abstract IngredientsDao ingredientsDao();

	public abstract StepsDao stepsDao();
}
