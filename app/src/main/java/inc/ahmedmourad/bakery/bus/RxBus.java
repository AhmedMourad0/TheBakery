package inc.ahmedmourad.bakery.bus;

import android.view.View;

import com.jakewharton.rxrelay2.PublishRelay;

import java.util.List;

import inc.ahmedmourad.bakery.model.room.entities.IngredientEntity;

public class RxBus {

    private static final RxBus INSTANCE = new RxBus();

    private PublishRelay<Integer> recipesRelay = PublishRelay.create();

    private PublishRelay<List<IngredientEntity>> ingredientsProgressRelay = PublishRelay.create();

    private PublishRelay<Boolean> ingredientsSelectionRelay = PublishRelay.create();

    private PublishRelay<Boolean> fabVisibilityRelay = PublishRelay.create();

    private PublishRelay<Integer> progressVisibilityRelay = PublishRelay.create();

    private PublishRelay<Integer> stepsRelay = PublishRelay.create();

    private RxBus() {

    }

    public static RxBus getInstance() {
        return INSTANCE;
    }

    public void selectRecipe(int id) {
        recipesRelay.accept(id);
    }

    public PublishRelay<Integer> getRecipeSelectionRelay() {
        return recipesRelay;
    }

    public void updateProgress(List<IngredientEntity> ingredients) {
        ingredientsProgressRelay.accept(ingredients);
    }

    public PublishRelay<List<IngredientEntity>> getIngredientsProgressRelay() {
        return ingredientsProgressRelay;
    }

    public void showProgress(boolean visible) {
        progressVisibilityRelay.accept(visible ? View.VISIBLE : View.GONE);
    }

    public PublishRelay<Integer> getProgressVisibilityRelay() {
        return progressVisibilityRelay;
    }

    public void showFab(boolean visible) {
        fabVisibilityRelay.accept(visible);
    }

    public PublishRelay<Boolean> getFabVisibilityRelay() {
        return fabVisibilityRelay;
    }

    public void selectStep(int stepPosition) {
        stepsRelay.accept(stepPosition);
    }

    public PublishRelay<Integer> getStepSelectionRelay() {
        return stepsRelay;
    }

    public void setAllIngredientsSelected(boolean selected) {
        ingredientsSelectionRelay.accept(selected);
    }

    public PublishRelay<Boolean> getIngredientsSelectionRelay() {
        return ingredientsSelectionRelay;
    }
}
