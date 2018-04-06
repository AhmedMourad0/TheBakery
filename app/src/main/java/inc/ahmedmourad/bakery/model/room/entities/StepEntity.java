package inc.ahmedmourad.bakery.model.room.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = StepEntity.TABLE_NAME,
        foreignKeys = {@ForeignKey(
                entity = RecipeEntity.class,
                parentColumns = {RecipeEntity.COLUMN_ID},
                childColumns = {StepEntity.COLUMN_RECIPE_ID},
                onDelete = ForeignKey.CASCADE)},
        indices = {@Index(
                value = {StepEntity.COLUMN_RECIPE_ID, StepEntity.COLUMN_STEP_ID},
                unique = true)})
public class StepEntity {

    @Ignore
    public static transient final String TABLE_NAME = "steps";

    @Ignore
    public static transient final String COLUMN_ID = "_id";

    @Ignore
    public static transient final String COLUMN_RECIPE_ID = "recipeId";

    @Ignore
    public static transient final String COLUMN_STEP_ID = "id";

    @Ignore
    public static transient final String COLUMN_SHORT_DESCRIPTION = "shortDescription";

    @Ignore
    public static transient final String COLUMN_DESCRIPTION = "description";

    @Ignore
    public static transient final String COLUMN_VIDEO_URL = "videoURL";

    @Ignore
    public static transient final String COLUMN_THUMBNAIL_URL = "thumbnailURL";

    @SerializedName(COLUMN_ID)
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID)
    public transient int id;

    @SerializedName(COLUMN_RECIPE_ID)
    @ColumnInfo(name = COLUMN_RECIPE_ID)
    public transient int recipeId = -1;

    @SerializedName(COLUMN_STEP_ID)
    @ColumnInfo(name = COLUMN_STEP_ID)
    public int stepId = -1;

    @SerializedName(COLUMN_SHORT_DESCRIPTION)
    @ColumnInfo(name = COLUMN_SHORT_DESCRIPTION)
    public String shortDescription = "";

    @SerializedName(COLUMN_DESCRIPTION)
    @ColumnInfo(name = COLUMN_DESCRIPTION)
    public String description = "";

    @SerializedName(COLUMN_VIDEO_URL)
    @ColumnInfo(name = COLUMN_VIDEO_URL)
    public String videoUrl = "";

    @SerializedName(COLUMN_THUMBNAIL_URL)
    @ColumnInfo(name = COLUMN_THUMBNAIL_URL)
    public String thumbnailUrl = "";

    @Ignore
    public StepEntity() {
        // For Gson
    }

    public StepEntity(int id, int recipeId, int stepId, String shortDescription, String description, String videoUrl, String thumbnailUrl) {
        this.id = id;
        this.recipeId = recipeId;
        this.stepId = stepId;
        this.shortDescription = shortDescription;
        this.description = description;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
    }
}
