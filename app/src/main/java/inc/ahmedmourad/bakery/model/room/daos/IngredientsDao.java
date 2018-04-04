package inc.ahmedmourad.bakery.model.room.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import inc.ahmedmourad.bakery.model.room.entities.IngredientEntity;

@Dao
public interface IngredientsDao {

    @Query("SELECT " +
            IngredientEntity.COLUMN_ID + ", " +
            IngredientEntity.COLUMN_RECIPE_ID + ", " +
            IngredientEntity.COLUMN_QUANTITY + ", " +
            IngredientEntity.COLUMN_MEASURE + ", " +
            IngredientEntity.COLUMN_INGREDIENT +
            " FROM " +
            IngredientEntity.TABLE_NAME +
            " WHERE " +
            IngredientEntity.COLUMN_RECIPE_ID +
            " = :id")
    List<IngredientEntity> getIngredientsByRecipeId(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void bulkInsert(List<IngredientEntity> ingredientEntities);

    @Query("DELETE FROM " + IngredientEntity.TABLE_NAME)
    void deleteAll();
}
