package inc.ahmedmourad.bakery.model.room.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = IngredientEntity.TABLE_NAME,
		foreignKeys = {@ForeignKey(
				entity = RecipeEntity.class,
				parentColumns = {RecipeEntity.COLUMN_ID},
				childColumns = {IngredientEntity.COLUMN_RECIPE_ID},
				onDelete = ForeignKey.CASCADE)},
		indices = {@Index(
				value = {IngredientEntity.COLUMN_RECIPE_ID, IngredientEntity.COLUMN_INGREDIENT},
				unique = true)})
public class IngredientEntity {

	@Ignore
	public static transient final String TABLE_NAME = "ingredients";

	@Ignore
	public static transient final String COLUMN_ID = "_id";

	@Ignore
	public static transient final String COLUMN_RECIPE_ID = "recipeId";

	@Ignore
	public static transient final String COLUMN_QUANTITY = "quantity";

	@Ignore
	public static transient final String COLUMN_MEASURE = "measure";

	@Ignore
	public static transient final String COLUMN_INGREDIENT = "ingredient";

	@Ignore
	public static transient final String COLUMN_IS_SELECTED = "isSelected";

	@SerializedName(COLUMN_ID)
	@PrimaryKey(autoGenerate = true)
	@ColumnInfo(name = COLUMN_ID)
	public transient int id;

	@SerializedName(COLUMN_RECIPE_ID)
	@ColumnInfo(name = COLUMN_RECIPE_ID)
	public transient int recipeId = -1;

	@SerializedName(COLUMN_QUANTITY)
	@ColumnInfo(name = COLUMN_QUANTITY)
	public double quantity = 0.0;

	@SerializedName(COLUMN_MEASURE)
	@ColumnInfo(name = COLUMN_MEASURE)
	public String measure = "";

	@SerializedName(COLUMN_INGREDIENT)
	@ColumnInfo(name = COLUMN_INGREDIENT)
	public String ingredient = "";

	@ColumnInfo(name = COLUMN_IS_SELECTED)
	public transient boolean isSelected = false;

	@Ignore
	public IngredientEntity() {
		// For Gson
	}

	public IngredientEntity(final int id, final int recipeId, final double quantity, final String measure, final String ingredient) {
		this.id = id;
		this.recipeId = recipeId;
		this.quantity = quantity;
		this.measure = measure;
		this.ingredient = ingredient;
	}
}
