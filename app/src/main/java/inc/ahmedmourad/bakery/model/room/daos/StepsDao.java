package inc.ahmedmourad.bakery.model.room.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import inc.ahmedmourad.bakery.model.room.entities.StepEntity;

@Dao
public interface StepsDao {

    @Query("SELECT " +
            StepEntity.COLUMN_ID + ", " +
            StepEntity.COLUMN_RECIPE_ID + ", " +
            StepEntity.COLUMN_STEP_ID + ", " +
            StepEntity.COLUMN_SHORT_DESCRIPTION + ", " +
            StepEntity.COLUMN_DESCRIPTION + ", " +
            StepEntity.COLUMN_VIDEO_URL + ", " +
            StepEntity.COLUMN_THUMBNAIL_URL +
            " FROM " +
            StepEntity.TABLE_NAME +
            " WHERE " +
            StepEntity.COLUMN_RECIPE_ID +
            " = :id")
    List<StepEntity> getStepsByRecipeId(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void bulkInsert(List<StepEntity> stepEntities);

    @Query("DELETE FROM " + StepEntity.TABLE_NAME)
    void deleteAll();
}
