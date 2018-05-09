package inc.ahmedmourad.bakery.external.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.widget.RemoteViews;

import inc.ahmedmourad.bakery.R;
import inc.ahmedmourad.bakery.external.adapter.IngredientsRemoteViewsService;
import inc.ahmedmourad.bakery.model.room.database.BakeryDatabase;
import inc.ahmedmourad.bakery.utils.ErrorUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link AppWidgetConfigureActivity AppWidgetConfigureActivity}
 */
public class AppWidget extends AppWidgetProvider {

	private SparseArray<Disposable> disposablesArray = new SparseArray<>();

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// There may be multiple widgets active, so update all of them
		for (int appWidgetId : appWidgetIds) {

			Disposable disposable = disposablesArray.get(appWidgetId);

			if (disposable != null)
				disposable.dispose();

			disposablesArray.put(appWidgetId, updateAppWidget(context, appWidgetManager, appWidgetId));
		}
	}

	@Nullable
	public static Disposable updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

		int recipeId = AppWidgetConfigureActivity.loadSelectedRecipe(context, appWidgetId);

		if (recipeId == -1)
			return null;

		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);

		views.setImageViewResource(R.id.widget_icon, R.drawable.ic_cupcake);
		views.setImageViewResource(R.id.widget_configure, R.drawable.ic_configure);

		Intent configurationIntent = new Intent(context, AppWidgetConfigureActivity.class);
		configurationIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		configurationIntent.setData(getUniqueDataUri(appWidgetId, recipeId));
		PendingIntent configurationPendingIntent = PendingIntent.getActivity(context, 0, configurationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		views.setOnClickPendingIntent(R.id.widget_configure, configurationPendingIntent);

		return BakeryDatabase.getInstance(context)
				.recipesDao()
				.getRecipeById(recipeId)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(recipe -> {

					views.setTextViewText(R.id.widget_title, recipe.name);

					Intent intent = new Intent(context, IngredientsRemoteViewsService.class);
					intent.putExtra(IngredientsRemoteViewsService.EXTRA_RECIPE_ID, recipeId);
					intent.setData(getUniqueDataUri(appWidgetId, recipeId));

					views.setRemoteAdapter(R.id.widget_list_view, intent);

					// Instruct the widget manager to update the widget
					appWidgetManager.updateAppWidget(appWidgetId, views);

				}, throwable -> ErrorUtils.general(context, throwable));
	}

	private static Uri getUniqueDataUri(final int appWidgetId, final int recipeId) {
		return Uri.withAppendedPath(Uri.parse("bakery://widget/id/"), String.valueOf(appWidgetId) + recipeId);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// When the user deletes the widget, delete the preference associated with it.
		for (int appWidgetId : appWidgetIds) {

			Disposable disposable = disposablesArray.get(appWidgetId);

			if (disposable != null)
				disposable.dispose();

			AppWidgetConfigureActivity.unselectRecipe(context, appWidgetId);
		}

		disposablesArray.clear();
	}

	@Override
	public void onEnabled(Context context) {
		// Enter relevant functionality for when the first widget is created
	}

	@Override
	public void onDisabled(Context context) {
		// Enter relevant functionality for when the last widget is disabled
	}
}

