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
import inc.ahmedmourad.bakery.utils.WidgetUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AppWidget extends AppWidgetProvider {

	/**
	 * We use this to keep references to disposables with their widget ids to later dispose them
	 */
	private final SparseArray<Disposable> disposablesArray = new SparseArray<>();

	@Override
	public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		// There may be multiple widgets active, so update all of them
		for (final int appWidgetId : appWidgetIds) {

			final Disposable disposable = disposablesArray.get(appWidgetId);

			if (disposable != null)
				disposable.dispose();

			disposablesArray.put(appWidgetId, updateAppWidget(context, appWidgetManager, appWidgetId));
		}
	}

	/**
	 * Used to update the ui of a certain widget
	 *
	 * @param context          The Context in which this receiver is running.
	 * @param appWidgetManager A AppWidgetManager object you can call AppWidgetManager.updateAppWidget on.
	 * @param appWidgetId      The appWidgetIds for which an update is needed.
	 * @return a disposable object that needs to be disposed to free up resources
	 */
	@Nullable
	public static Disposable updateAppWidget(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId) {

		final int recipeId = WidgetUtils.loadSelectedRecipe(context, appWidgetId);

		if (recipeId == -1)
			return null;

		final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);

		views.setImageViewResource(R.id.widget_icon, R.drawable.ic_cupcake);
		views.setImageViewResource(R.id.widget_configure, R.drawable.ic_configure);

		// an intent used to call the widget configuration activity
		final Intent configurationIntent = new Intent(context, AppWidgetConfigureActivity.class);
		configurationIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		configurationIntent.setData(getUniqueDataUri(appWidgetId, recipeId));
		final PendingIntent configurationPendingIntent = PendingIntent.getActivity(context, 0, configurationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		views.setOnClickPendingIntent(R.id.widget_configure, configurationPendingIntent);

		return BakeryDatabase.getInstance(context)
				.recipesDao()
				.getRecipeById(recipeId)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(recipe -> {

					views.setTextViewText(R.id.widget_title, recipe.name);

					final Intent intent = new Intent(context, IngredientsRemoteViewsService.class);
					intent.putExtra(IngredientsRemoteViewsService.EXTRA_RECIPE_ID, recipeId);
					// Each uri must be unique in order for the widget to be updated
					// Saying that it was a pain to figure this one out is an understatement
					intent.setData(getUniqueDataUri(appWidgetId, recipeId));

					views.setRemoteAdapter(R.id.widget_list_view, intent);

					// Instruct the widget manager to update the widget
					appWidgetManager.updateAppWidget(appWidgetId, views);

				}, throwable -> ErrorUtils.general(context, throwable));
	}

	/**
	 * generates a unique data uri using the given values
	 * @param appWidgetId the widget id
	 * @param recipeId the id of the recipe to be displayed in the widget
	 * @return a unique data uri using the given values
	 */
	private static Uri getUniqueDataUri(final int appWidgetId, final int recipeId) {
		return Uri.withAppendedPath(Uri.parse("bakery://widget/id/"), String.valueOf(appWidgetId) + recipeId);
	}

	@Override
	public void onDeleted(final Context context, final int[] appWidgetIds) {
		// When the user deletes the widget, delete the preference associated with it.
		for (final int appWidgetId : appWidgetIds) {

			final Disposable disposable = disposablesArray.get(appWidgetId);

			if (disposable != null)
				disposable.dispose();

			WidgetUtils.unselectRecipe(context, appWidgetId);
		}

		disposablesArray.clear();
	}

	@Override
	public void onDisabled(final Context context) {
		WidgetUtils.unselectAllRecipes(context);
	}
}

