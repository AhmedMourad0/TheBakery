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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import inc.ahmedmourad.bakery.R;
import inc.ahmedmourad.bakery.adapters.WidgetEntriesListAdapter;
import inc.ahmedmourad.bakery.bus.RxBus;
import inc.ahmedmourad.bakery.external.widget.AppWidget;
import inc.ahmedmourad.bakery.model.room.database.BakeryDatabase;
import inc.ahmedmourad.bakery.pojos.WidgetEntry;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public final class WidgetSelectorUtils {

	private static Disposable recipesDisposables;

	private static final CompositeDisposable disposables = new CompositeDisposable();

	/**
	 * if the user one widget on his home screen, it's updated directly,
	 * if more, he's shown a dialog with these widgets to choose which one to update,
	 * if none, the user is notified of that
	 *
	 * @param context     context
	 * @param newRecipeId the id of the new recipe the widget is going to display
	 */
	public static void startWidgetSelector(final Context context, final int newRecipeId) {

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

		} else if (prefWidgetsEntries.size() == 1) {
			updateWidget(context, Integer.valueOf(prefWidgetsEntries.get(0).widgetId), newRecipeId);
			return;
		}

		final WidgetEntriesListAdapter adapter = new WidgetEntriesListAdapter();

		if (recipesDisposables != null)
			recipesDisposables.dispose();

		recipesDisposables = BakeryDatabase.getInstance(context)
				.recipesDao()
				.getRecipesByIds(recipesIds)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(entries -> {

					// I really hate this nested for loop thing but i can't think of
					// a better way to do this right now
					// Here we map the recipe names we received from the database to their
					// recipe ids at the prefWidgetsEntries
					WidgetEntry prefEntry;
					WidgetEntry dbResponseEntry;
					for (int i = 0; i < prefWidgetsEntries.size(); ++i) {

						prefEntry = prefWidgetsEntries.get(i);

						for (int j = 0; j < entries.size(); ++j) {

							dbResponseEntry = entries.get(j);

							if (prefEntry.recipeId.equals(dbResponseEntry.recipeId)) {
								prefEntry.recipeName = dbResponseEntry.recipeName;
								break;
							}
						}
					}

					// we sort the results by widget id to display them to the user in the same
					// order he created them
					Collections.sort(prefWidgetsEntries, (o1, o2) -> Integer.compare(Integer.valueOf(o1.widgetId), Integer.valueOf(o2.widgetId)));

					adapter.updateEntries(prefWidgetsEntries);

				}, throwable -> ErrorUtils.general(context, throwable));

		final AlertDialog dialog = new AlertDialog.Builder(context)
				.setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
				.setTitle(R.string.select_widget_to_update)
				.setAdapter(adapter, (d, position) -> {

					final WidgetEntry entry = (WidgetEntry) adapter.getItem(position);

					if (entry != null)
						updateWidget(context, Integer.valueOf(entry.widgetId), newRecipeId);

				}).create();

		dialog.setOnShowListener(d -> RxBus.getInstance().setWidgetDialogRecipeId(newRecipeId));
		dialog.setOnDismissListener(d -> RxBus.getInstance().setWidgetDialogRecipeId(-1));

		dialog.show();
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
	 * @return A list of recipes ids of real widgets
	 */
	private static List<Integer> saltAndBurnPhantomWidgets(final Context context,
	                                                       final List<WidgetEntry> prefWidgetsEntries,
	                                                       final List<Integer> prefWidgetsIds,
	                                                       final List<Integer> systemWidgetsIds) {

		final Map<String, String> prefWidgetsMap = getWidgetsPrefMap(context);

		final List<Integer> intersectionIdsResult = intersect(prefWidgetsIds, systemWidgetsIds);

		// clear pref widget map of phantoms
		final Set<String> prefWidgetsMapKeySet = prefWidgetsMap.keySet();
		final List<String> removedIds = new ArrayList<>();

		for (final String key : prefWidgetsMapKeySet) {
			if (!intersectionIdsResult.contains(Integer.valueOf(key)))
				removedIds.add(key);
		}

		//To avoid ConcurrentModificationException
		prefWidgetsMapKeySet.removeAll(removedIds);

		// clear given prefWidgetsEntries list of phantoms
		String entryPrefWidgetId;
		for (int i = 0; i < prefWidgetsEntries.size(); ++i) {

			entryPrefWidgetId = prefWidgetsEntries.get(i).widgetId;

			if (!intersectionIdsResult.contains(Integer.valueOf(entryPrefWidgetId)))
				prefWidgetsEntries.remove(i);
		}

		updateWidgetsPrefMap(context, prefWidgetsMap);

		// delete phantom widgets
		final AppWidgetHost appWidgetHost = new AppWidgetHost(context, 1);

		int entrySystemWidgetId;

		for (int i = 0; i < systemWidgetsIds.size(); ++i) {

			entrySystemWidgetId = systemWidgetsIds.get(i);

			if (!intersectionIdsResult.contains(entrySystemWidgetId))
				appWidgetHost.deleteAppWidgetId(entrySystemWidgetId);
		}

		// get list of recipe ids for real widgets and return it
		final List<Integer> recipesIds = new ArrayList<>(prefWidgetsEntries.size());

		for (int i = 0; i < prefWidgetsEntries.size(); ++i)
			recipesIds.add(Integer.valueOf(prefWidgetsEntries.get(i).recipeId));

		return recipesIds;
	}

	static void addWidgetIdToPrefMap(final Context context, final int widgetId, final int recipeId) {

		final Map<String, String> map = getWidgetsPrefMap(context);

		map.put(Integer.toString(widgetId), Integer.toString(recipeId));

		updateWidgetsPrefMap(context, map);
	}

	static void removeWidgetIdFromPrefMap(final Context context, final int widgetId) {

		final Map<String, String> map = getWidgetsPrefMap(context);

		map.remove(Integer.toString(widgetId));

		updateWidgetsPrefMap(context, map);
	}

	/**
	 * gets the intersection of two lists
	 *
	 * @param list1 the first list
	 * @param list2 the second list
	 * @return the intersection of the two lists
	 */
	private static List<Integer> intersect(final List<Integer> list1, final List<Integer> list2) {

		final List<Integer> result = new ArrayList<>();

		for (int i = 0; i < list1.size(); ++i) {

			final Integer item = list1.get(i);

			if (list2.contains(item))
				result.add(item);
		}

		return result;
	}

	private static int[] getSystemWidgetsIds(final Context context) {
		final ComponentName name = new ComponentName(context.getApplicationContext(), AppWidget.class);
		return AppWidgetManager.getInstance(context.getApplicationContext()).getAppWidgetIds(name);
	}

	private static void updateWidget(final Context context, final int widgetId, final int newRecipeId) {

		WidgetUtils.updateSelectedRecipe(context, widgetId, newRecipeId);

		final Map<String, String> map = getWidgetsPrefMap(context);

		map.put(Integer.toString(widgetId), Integer.toString(newRecipeId));

		updateWidgetsPrefMap(context, map);

		final Disposable d = AppWidget.updateAppWidget(context, AppWidgetManager.getInstance(context), widgetId);

		if (d != null)
			disposables.add(d);

		Toast.makeText(context, R.string.update_widget_success, Toast.LENGTH_LONG).show();
	}

	private static List<WidgetEntry> getPrefWidgetsEntries(final Context context) {

		final Map<String, String> entriesMap = getWidgetsPrefMap(context);

		final List<WidgetEntry> widgetEntries = new ArrayList<>(entriesMap.size());

		for (final Map.Entry<String, String> e : entriesMap.entrySet()) {

			final WidgetEntry widgetEntry = new WidgetEntry();

			widgetEntry.widgetId = e.getKey();
			widgetEntry.recipeId = e.getValue();

			widgetEntries.add(widgetEntry);
		}

		return widgetEntries;
	}

	@NonNull
	static Map<String, String> getWidgetsPrefMap(final Context context) {
		final String idsStr = PreferencesUtils.defaultPrefs(context)
				.getString(PreferencesUtils.KEY_WIDGET_IDS, "");

		return stringToMap(idsStr);
	}

	private static void updateWidgetsPrefMap(final Context context, final Map<String, String> map) {
		PreferencesUtils.edit(context, e -> e.putString(PreferencesUtils.KEY_WIDGET_IDS, mapToString(map)));
	}

	public static void releaseResources() {

		if (recipesDisposables != null)
			recipesDisposables.dispose();

		disposables.clear();
	}

	private static List<Integer> toList(final int[] arr) {

		final List<Integer> list = new ArrayList<>(arr.length);

		for (final int item : arr)
			list.add(item);

		return list;
	}

	@NonNull
	private static String mapToString(final Map<String, String> map) {

		final StringBuilder builder = new StringBuilder();

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
