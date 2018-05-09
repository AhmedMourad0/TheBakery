package inc.ahmedmourad.bakery.utils;

import android.appwidget.AppWidgetManager;
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

	private static CompositeDisposable disposables = new CompositeDisposable();

	private static Disposable recipesDisposables;

	public static void startWidgetChooser(final Context context, final int newRecipeId) {

		if (newRecipeId == -1)
			return;

		final Comparator<WidgetEntry> comparator =
				(o1, o2) -> Integer.compare(Integer.valueOf(o1.recipeId), Integer.valueOf(o2.recipeId));

		final List<WidgetEntry> widgetEntries = getWidgetEntries(context);

		if (widgetEntries.size() == 0) {

			Toast.makeText(context, R.string.no_widgets_found, Toast.LENGTH_LONG).show();

			return;

//			if (context instanceof MainActivity) {
//				Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
//				pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 2);
//				((MainActivity)context).startActivityForResult(pickIntent, 2018);
//			}

		} else if (widgetEntries.size() == 1) {

			updateWidget(context, Integer.valueOf(widgetEntries.get(0).widgetId), newRecipeId);

			return;
		}

		final List<Integer> ids = new ArrayList<>(widgetEntries.size());

		Collections.sort(widgetEntries, comparator);

		for (int i = 0; i < widgetEntries.size(); ++i)
			ids.add(Integer.valueOf(widgetEntries.get(i).recipeId));

		final WidgetEntriesListAdapter adapter = new WidgetEntriesListAdapter();

		if (recipesDisposables != null)
			recipesDisposables.dispose();

		recipesDisposables = BakeryDatabase.getInstance(context)
				.recipesDao()
				.getRecipesByIds(ids)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(entries -> {

					Collections.sort(entries, comparator);

					for (int i = 0; i < widgetEntries.size(); ++i)
						widgetEntries.get(i).recipeName = entries.get(i).recipeName;

					Collections.sort(widgetEntries, (o1, o2) -> Integer.compare(Integer.valueOf(o1.widgetId), Integer.valueOf(o2.widgetId)));

					adapter.updateEntries(widgetEntries);

				}, throwable -> ErrorUtils.general(context, throwable));


		new AlertDialog.Builder(context)
				.setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
				.setTitle("Select widget to update")
				.setAdapter(adapter, (dialog, position) -> {

					final WidgetEntry entry = (WidgetEntry) adapter.getItem(position);

					if (entry != null)
						updateWidget(context, Integer.valueOf(entry.widgetId), newRecipeId);

				}).show();
	}

	public static void addWidgetId(final Context context, final int widgetId, final int recipeId) {

		final Map<String, String> map = getWidgetsMap(context);

		map.put(Integer.toString(widgetId), Integer.toString(recipeId));

		updateWidgetsMap(context, map);
	}

	public static void removeWidgetId(final Context context, final int widgetId) {

		final Map<String, String> map = getWidgetsMap(context);

		map.remove(Integer.toString(widgetId));

		updateWidgetsMap(context, map);
	}

	private static void updateWidget(final Context context, final int widgetId, final int newRecipeId) {

		AppWidgetConfigureActivity.updateSelectedRecipe(context, widgetId, newRecipeId);

		final Map<String, String> map = getWidgetsMap(context);

		map.put(Integer.toString(widgetId), Integer.toString(newRecipeId));

		updateWidgetsMap(context, map);

		Disposable d = AppWidget.updateAppWidget(context, AppWidgetManager.getInstance(context), widgetId);

		if (d != null)
			disposables.add(d);

		Toast.makeText(context, R.string.update_widget_success, Toast.LENGTH_LONG).show();
	}

	private static List<WidgetEntry> getWidgetEntries(final Context context) {

		final Map<String, String> entriesMap = getWidgetsMap(context);

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
	private static Map<String, String> getWidgetsMap(final Context context) {
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
