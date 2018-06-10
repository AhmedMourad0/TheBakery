package inc.ahmedmourad.bakery.pojos;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Ignore;

import java.util.Arrays;

import inc.ahmedmourad.bakery.model.room.entities.RecipeEntity;

public class WidgetEntry {

	@Ignore
	public String widgetId = "";

	@ColumnInfo(name = RecipeEntity.COLUMN_ID)
	public String recipeId = "";

	@ColumnInfo(name = RecipeEntity.COLUMN_NAME)
	public String recipeName = "";

	@Ignore
	public WidgetEntry() {

	}

	public WidgetEntry(final String recipeId, final String recipeName) {
		this.recipeId = recipeId;
		this.recipeName = recipeName;
	}

	@Override
	public boolean equals(final Object o) {

		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		final WidgetEntry that = (WidgetEntry) o;

		return widgetId.equals(that.widgetId) &&
				recipeId.equals(that.recipeId);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(new String[]{widgetId, recipeId});
	}
}
