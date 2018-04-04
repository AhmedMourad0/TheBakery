package inc.ahmedmourad.bakery.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.net.ConnectException;
import java.net.UnknownHostException;

import inc.ahmedmourad.bakery.R;
import inc.ahmedmourad.bakery.model.api.ApiClient;
import inc.ahmedmourad.bakery.model.api.ApiInterface;
import inc.ahmedmourad.bakery.model.room.database.BakeryDatabase;
import inc.ahmedmourad.bakery.model.room.entities.RecipeEntity;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static inc.ahmedmourad.bakery.utils.ConcurrencyUtils.runOnUiThread;

public final class NetworkUtils {

    @NonNull
    public static Disposable fetchRecipes(final Context context, final BakeryDatabase db) {

        return ApiClient.getInstance()
                .create(ApiInterface.class)
                .getRecipes()
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(recipeEntities -> db.runInTransaction(() -> {

                    db.reset();
                    db.recipesDao().bulkInsert(recipeEntities);

                    for (final RecipeEntity recipeEntity : recipeEntities) {

                        for (int i = 0; i < recipeEntity.ingredients.size(); i++)
                            recipeEntity.ingredients.get(i).recipeId = recipeEntity.id;

                        for (int i = 0; i < recipeEntity.steps.size(); i++)
                            recipeEntity.steps.get(i).recipeId = recipeEntity.id;

                        db.ingredientsDao().bulkInsert(recipeEntity.ingredients);
                        db.stepsDao().bulkInsert(recipeEntity.steps);
                    }

                }), throwable -> handleError(context, throwable));
    }

    private static void handleError(final Context context, final Throwable throwable) {

        // static import, because it's pretty
        runOnUiThread(context, () -> {

            if (throwable == null ||
                    throwable instanceof ConnectException ||
                    throwable instanceof UnknownHostException)
                Toast.makeText(
                        context,
                        R.string.network_error,
                        Toast.LENGTH_LONG
                ).show();
            else if (throwable.getCause() == null)
                Toast.makeText(
                        context,
                        context.getString(
                                R.string.network_error_no_cause,
                                throwable.getLocalizedMessage()
                        ), Toast.LENGTH_LONG
                ).show();
            else
                Toast.makeText(
                        context,
                        context.getString(
                                R.string.network_error_cause,
                                throwable.getLocalizedMessage(),
                                throwable.getCause().getLocalizedMessage()
                        ), Toast.LENGTH_LONG
                ).show();
        });

        if (throwable != null)
            throwable.printStackTrace();
    }
}
