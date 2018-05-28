package inc.ahmedmourad.bakery.model.room.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@Entity(tableName = RecipeEntity.TABLE_NAME)
public class RecipeEntity {

	@Ignore
	public static transient final String TABLE_NAME = "recipes";

	@Ignore
	public static transient final String COLUMN_ID = "id";

	@Ignore
	public static transient final String COLUMN_NAME = "name";

	@Ignore
	public static transient final String COLUMN_SERVINGS = "servings";

	@Ignore
	public static transient final String COLUMN_IMAGE = "image";

	@Ignore
	private static transient final String COLUMN_INGREDIENTS = "ingredients";

	@Ignore
	private static transient final String COLUMN_STEPS = "steps";

	@SerializedName(COLUMN_ID)
	@PrimaryKey
	@ColumnInfo(name = COLUMN_ID)
	public int id = -1;

	@SerializedName(COLUMN_NAME)
	@ColumnInfo(name = COLUMN_NAME)
	public String name = "";

	@SerializedName(COLUMN_SERVINGS)
	@ColumnInfo(name = COLUMN_SERVINGS)
	public int servings = -1;

	@SerializedName(COLUMN_IMAGE)
	@ColumnInfo(name = COLUMN_IMAGE)
	public String image = "";

	@SuppressWarnings("CanBeFinal")
	@SerializedName(COLUMN_INGREDIENTS)
	@Ignore
	public List<IngredientEntity> ingredients = null;

	@SuppressWarnings("CanBeFinal")
	@SerializedName(COLUMN_STEPS)
	@Ignore
	public List<StepEntity> steps = null;

	@Ignore
	public RecipeEntity() {
		// For Gson
	}

	public RecipeEntity(final int id, final String name, final int servings, final String image) {
		this.id = id;
		this.name = name;
		this.servings = servings;
		this.image = image;
	}
}
