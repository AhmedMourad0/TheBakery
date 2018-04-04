package inc.ahmedmourad.bakery.model.room.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import inc.ahmedmourad.bakery.model.room.entities.RecipeEntity;

@Dao
public interface RecipesDao {

    @Query("SELECT " +
            RecipeEntity.COLUMN_ID + ", " +
            RecipeEntity.COLUMN_NAME + ", " +
            RecipeEntity.COLUMN_SERVINGS + ", " +
            RecipeEntity.COLUMN_IMAGE +
            " FROM " +
            RecipeEntity.TABLE_NAME)
    LiveData<List<RecipeEntity>> getRecipes();

    @Query("SELECT COUNT(*) FROM " + RecipeEntity.TABLE_NAME)
    int getCount();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void bulkInsert(RecipeEntity[] recipesEntities);

    @Query("DELETE FROM " + RecipeEntity.TABLE_NAME)
    void deleteAll();
}
