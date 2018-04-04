package inc.ahmedmourad.bakery.model.api;

import inc.ahmedmourad.bakery.model.room.entities.RecipeEntity;
import io.reactivex.Single;
import retrofit2.http.GET;

public interface ApiInterface {

    // http://go.udacity.com/android-baking-app-json
    @GET("android-baking-app-json")
    Single<RecipeEntity[]> getRecipes();
}
