package inc.ahmedmourad.bakery.utils;

import android.content.Context;

import java.util.Map;

public final class WidgetUtils {

	private static final String PREF_PREFIX_KEY = "appwidget_";

	public static void selectRecipe(final Context context, final int appWidgetId, final int recipeId) {
		updateSelectedRecipe(context, appWidgetId, recipeId);
		WidgetSelectorUtils.addWidgetIdToPrefMap(context, appWidgetId, recipeId);
	}

	static void updateSelectedRecipe(final Context context, final int appWidgetId, final int recipeId) {
		PreferencesUtils.edit(context, e -> e.putInt(PREF_PREFIX_KEY + appWidgetId, recipeId));
	}

	public static int loadSelectedRecipe(final Context context, final int appWidgetId) {
		return PreferencesUtils.defaultPrefs(context).getInt(PREF_PREFIX_KEY + appWidgetId, -1);
	}

	public static void unselectRecipe(final Context context, final int appWidgetId) {
		PreferencesUtils.edit(context, e -> e.remove(PREF_PREFIX_KEY + appWidgetId));
		WidgetSelectorUtils.removeWidgetIdFromPrefMap(context, appWidgetId);
	}

	public static void unselectAllRecipes(final Context context) {

		final Map<String, String> map = WidgetSelectorUtils.getWidgetsPrefMap(context);

		for (String widgetId : map.keySet())
			PreferencesUtils.edit(context, e -> e.remove(PREF_PREFIX_KEY + widgetId));

		PreferencesUtils.edit(context, e -> e.remove(PreferencesUtils.KEY_WIDGET_IDS));
	}
}
