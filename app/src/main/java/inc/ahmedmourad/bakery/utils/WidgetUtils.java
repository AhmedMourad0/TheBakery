package inc.ahmedmourad.bakery.utils;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import inc.ahmedmourad.bakery.R;
import inc.ahmedmourad.bakery.adapters.WidgetEntriesListAdapter;
import inc.ahmedmourad.bakery.external.widget.AppWidget;
import inc.ahmedmourad.bakery.external.widget.AppWidgetConfigureActivity;
import inc.ahmedmourad.bakery.model.room.database.BakeryDatabase;
import inc.ahmedmourad.bakery.pojos.WidgetEntry;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public final class WidgetUtils {

	private static final CompositeDisposable disposables = new CompositeDisposable();

	private static Disposable recipesDisposables;

	public static void startWidgetChooser(final Context context, final int newRecipeId) {

		if (newRecipeId == -1)
			return;

		final List<WidgetEntry> prefWidgetsEntries = getPrefWidgetsEntries(context);

		final List<Integer> prefWidgetsIds = new ArrayList<>(prefWidgetsEntries.size());

		for (int i = 0; i < prefWidgetsEntries.size(); ++i)
			prefWidgetsIds.add(Integer.valueOf(prefWidgetsEntries.get(i).widgetId));

		final List<Integer> recipesIds = saltAndBurnPhantomWidgets(context,
				prefWidgetsEntries,
				prefWidgetsIds,
				toList(getSystemWidgetsIds(context.getApplicationContext()))
		);

		if (prefWidgetsEntries.size() == 0) {
			Toast.makeText(context, R.string.no_widgets_found, Toast.LENGTH_LONG).show();
			return;
//			if (context instanceof MainActivity) {
//				Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
//				pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 2017);
//				((MainActivity)context).startActivityForResult(pickIntent, 2018);
//			}
		} else if (prefWidgetsEntries.size() == 1) {
			updateWidget(context, Integer.valueOf(prefWidgetsEntries.get(0).widgetId), newRecipeId);
			return;
		}

		final Comparator<WidgetEntry> recipeIdComparator =
				(o1, o2) -> Integer.compare(Integer.valueOf(o1.recipeId), Integer.valueOf(o2.recipeId));

		Collections.sort(prefWidgetsEntries, recipeIdComparator);

		final WidgetEntriesListAdapter adapter = new WidgetEntriesListAdapter();

		if (recipesDisposables != null)
			recipesDisposables.dispose();

		recipesDisposables = BakeryDatabase.getInstance(context)
				.recipesDao()
				.getRecipesByIds(recipesIds)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(entries -> {

					Collections.sort(entries, recipeIdComparator);

					for (int i = 0; i < prefWidgetsEntries.size(); ++i)
						prefWidgetsEntries.get(i).recipeName = entries.get(i).recipeName;

					Collections.sort(prefWidgetsEntries, (o1, o2) -> Integer.compare(Integer.valueOf(o1.widgetId), Integer.valueOf(o2.widgetId)));

					adapter.updateEntries(prefWidgetsEntries);

				}, throwable -> ErrorUtils.general(context, throwable));

		new AlertDialog.Builder(context)
				.setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
				.setTitle(R.string.select_widget_to_update)
				.setAdapter(adapter, (dialog, position) -> {

					final WidgetEntry entry = (WidgetEntry) adapter.getItem(position);

					if (entry != null)
						updateWidget(context, Integer.valueOf(entry.widgetId), newRecipeId);

				}).show();
	}

	/**
	 * Attempts to get rid of phantom widgets by getting available widgets from two sources, preferences
	 * and the system, then uses their intersection as the single source of truth for real widgets then removes
	 * phantom widgets from given lists.
	 *
	 * @param context            Rock salt
	 * @param prefWidgetsEntries widget entries from preferences
	 * @param prefWidgetsIds     widget ids of widget entries from preferences
	 * @param systemWidgetsIds   widget ids requested from the system
	 * @return A list of recipes ids for real widgets
	 */
	private static List<Integer> saltAndBurnPhantomWidgets(final Context context,
	                                                       final List<WidgetEntry> prefWidgetsEntries,
	                                                       final List<Integer> prefWidgetsIds,
	                                                       final List<Integer> systemWidgetsIds) {

		final Map<String, String> prefWidgetsMap = getPrefWidgetsMap(context);

		final List<Integer> intersectionIdsResult = intersect(prefWidgetsIds, systemWidgetsIds);

		String entryPrefWidgetId;

		for (Map.Entry<String, String> entry : prefWidgetsMap.entrySet()) {

			entryPrefWidgetId = entry.getKey();

			if (!intersectionIdsResult.contains(Integer.valueOf(entryPrefWidgetId)))
				prefWidgetsMap.remove(entryPrefWidgetId);
		}

		for (int i = 0; i < prefWidgetsEntries.size(); ++i) {

			entryPrefWidgetId = prefWidgetsEntries.get(i).widgetId;

			if (!intersectionIdsResult.contains(Integer.valueOf(entryPrefWidgetId)))
				prefWidgetsEntries.remove(i);
		}

		updateWidgetsMap(context, prefWidgetsMap);

		final AppWidgetHost appWidgetHost = new AppWidgetHost(context, 1);

		int entrySystemWidgetId;

		for (int i = 0; i < systemWidgetsIds.size(); ++i) {

			entrySystemWidgetId = systemWidgetsIds.get(i);

			if (!intersectionIdsResult.contains(entrySystemWidgetId))
				appWidgetHost.deleteAppWidgetId(entrySystemWidgetId);
		}

		final List<Integer> recipesIds = new ArrayList<>(prefWidgetsEntries.size());

		for (int i = 0; i < prefWidgetsEntries.size(); ++i)
			recipesIds.add(Integer.valueOf(prefWidgetsEntries.get(i).recipeId));

		return recipesIds;
	}

	public static void addWidgetId(final Context context, final int widgetId, final int recipeId) {

		final Map<String, String> map = getPrefWidgetsMap(context);

		map.put(Integer.toString(widgetId), Integer.toString(recipeId));

		updateWidgetsMap(context, map);
	}

	public static void removeWidgetId(final Context context, final int widgetId) {

		final Map<String, String> map = getPrefWidgetsMap(context);

		map.remove(Integer.toString(widgetId));

		updateWidgetsMap(context, map);
	}

	private static List<Integer> toList(int[] arr) {

		List<Integer> list = new ArrayList<>(arr.length);

		for (int item : arr)
			list.add(item);

		return list;
	}

	private static List<Integer> intersect(List<Integer> list1, List<Integer> list2) {

		List<Integer> result = new ArrayList<>();

		for (int i = 0; i < list1.size(); ++i) {

			Integer item = list1.get(i);

			if (list2.contains(item))
				result.add(item);
		}

		return result;
	}

	private static int[] getSystemWidgetsIds(Context context) {
		ComponentName name = new ComponentName(context.getApplicationContext(), AppWidget.class);
		return AppWidgetManager.getInstance(context.getApplicationContext()).getAppWidgetIds(name);
	}

	private static void updateWidget(final Context context, final int widgetId, final int newRecipeId) {

		AppWidgetConfigureActivity.updateSelectedRecipe(context, widgetId, newRecipeId);

		final Map<String, String> map = getPrefWidgetsMap(context);

		map.put(Integer.toString(widgetId), Integer.toString(newRecipeId));

		updateWidgetsMap(context, map);

		Disposable d = AppWidget.updateAppWidget(context, AppWidgetManager.getInstance(context), widgetId);

		if (d != null)
			disposables.add(d);

		Toast.makeText(context, R.string.update_widget_success, Toast.LENGTH_LONG).show();
	}

	private static List<WidgetEntry> getPrefWidgetsEntries(final Context context) {

		final Map<String, String> entriesMap = getPrefWidgetsMap(context);

		final List<WidgetEntry> widgetEntries = new ArrayList<>(entriesMap.size());

		for (final Map.Entry<String, String> e : entriesMap.entrySet()) {

			final WidgetEntry widgetEntry = new WidgetEntry();

			widgetEntry.widgetId = e.getKey();
			widgetEntry.recipeId = e.getValue();

			widgetEntries.add(widgetEntry);
		}

		return widgetEntries;
	}

	public static void releaseResources() {

		if (recipesDisposables != null)
			recipesDisposables.dispose();

		disposables.clear();
	}

	@NonNull
	private static Map<String, String> getPrefWidgetsMap(final Context context) {
		final String idsStr = PreferencesUtils.defaultPrefs(context)
				.getString(PreferencesUtils.KEY_WIDGET_IDS, "");

		return stringToMap(idsStr);
	}

	private static void updateWidgetsMap(final Context context, final Map<String, String> map) {
		PreferencesUtils.edit(context, e -> e.putString(PreferencesUtils.KEY_WIDGET_IDS, mapToString(map)));
	}

	@NonNull
	private static String mapToString(final Map<String, String> map) {

		StringBuilder builder = new StringBuilder();

		for (final Map.Entry<String, String> entry : map.entrySet())
			builder.append(entry.getKey())
					.append(",,,,")
					.append(entry.getValue())
					.append("||||");

		if (builder.length() >= 4)
			builder.delete(builder.length() - 4, builder.length());

		return builder.toString();
	}

	@NonNull
	private static Map<String, String> stringToMap(final String str) {

		final Map<String, String> map = new HashMap<>();

		final String[] entries = str.split("\\|\\|\\|\\|");

		for (final String entry : entries) {

			final String[] data = entry.split(",,,,");

			if (data.length == 2)
				map.put(data[0], data[1]);
		}

		return map;
	}
}
