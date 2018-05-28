package inc.ahmedmourad.bakery.model.room.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import inc.ahmedmourad.bakery.model.room.entities.RecipeEntity;
import inc.ahmedmourad.bakery.pojos.WidgetEntry;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface RecipesDao {

	@Query("SELECT " +
			RecipeEntity.COLUMN_ID + ", " +
			RecipeEntity.COLUMN_NAME + ", " +
			RecipeEntity.COLUMN_SERVINGS + ", " +
			RecipeEntity.COLUMN_IMAGE +
			" FROM " +
			RecipeEntity.TABLE_NAME +
			" WHERE " +
			RecipeEntity.COLUMN_ID +
			" = :id")
	Single<RecipeEntity> getRecipeById(int id);

	@Query("SELECT " +
			RecipeEntity.COLUMN_ID + ", " +
			RecipeEntity.COLUMN_NAME +
			" FROM " +
			RecipeEntity.TABLE_NAME +
			" WHERE " +
			RecipeEntity.COLUMN_ID +
			" IN (:ids)"
	)
	Single<List<WidgetEntry>> getRecipesByIds(List<Integer> ids);

	@Query("SELECT " +
			RecipeEntity.COLUMN_ID + ", " +
			RecipeEntity.COLUMN_NAME + ", " +
			RecipeEntity.COLUMN_SERVINGS + ", " +
			RecipeEntity.COLUMN_IMAGE +
			" FROM " +
			RecipeEntity.TABLE_NAME)
	Flowable<List<RecipeEntity>> getRecipes();

	@Query("SELECT COUNT(*) FROM " + RecipeEntity.TABLE_NAME)
	Single<Integer> getCount();

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void bulkInsert(RecipeEntity[] recipesEntities);

	@Query("DELETE FROM " + RecipeEntity.TABLE_NAME)
	void deleteAll();
}
