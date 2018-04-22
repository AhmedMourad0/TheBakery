package inc.ahmedmourad.bakery.model.room.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import inc.ahmedmourad.bakery.model.room.entities.IngredientEntity;
import io.reactivex.Single;

@Dao
public interface IngredientsDao {

    @Query("SELECT " +
            IngredientEntity.COLUMN_ID + ", " +
            IngredientEntity.COLUMN_RECIPE_ID + ", " +
            IngredientEntity.COLUMN_QUANTITY + ", " +
            IngredientEntity.COLUMN_MEASURE + ", " +
            IngredientEntity.COLUMN_INGREDIENT + ", " +
            IngredientEntity.COLUMN_IS_SELECTED +
            " FROM " +
            IngredientEntity.TABLE_NAME +
            " WHERE " +
            IngredientEntity.COLUMN_RECIPE_ID +
            " = :recipeId")
    Single<List<IngredientEntity>> getIngredientsByRecipeId(int recipeId);

    @Query("UPDATE " +
            IngredientEntity.TABLE_NAME +
            " SET " +
            IngredientEntity.COLUMN_IS_SELECTED +
            " = :selected" +
            " WHERE " +
            IngredientEntity.COLUMN_RECIPE_ID +
            " = :recipeId")
    void updateSelection(int recipeId, boolean selected);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void bulkInsert(List<IngredientEntity> ingredientEntities);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(IngredientEntity ingredientEntities);

    @Query("DELETE FROM " + IngredientEntity.TABLE_NAME)
    void deleteAll();
}
