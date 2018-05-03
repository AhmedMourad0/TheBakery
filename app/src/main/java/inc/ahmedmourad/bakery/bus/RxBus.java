package inc.ahmedmourad.bakery.bus;

import android.content.pm.ActivityInfo;
import android.view.View;

import com.jakewharton.rxrelay2.PublishRelay;

import java.util.List;

import inc.ahmedmourad.bakery.model.room.entities.IngredientEntity;

public class RxBus {

	private static final RxBus INSTANCE = new RxBus();

	private PublishRelay<String> activityTitleRelay = PublishRelay.create();

	private PublishRelay<Integer> recipesRelay = PublishRelay.create();

	private PublishRelay<List<IngredientEntity>> ingredientsProgressRelay = PublishRelay.create();

	private PublishRelay<Boolean> ingredientsSelectionRelay = PublishRelay.create();

	private PublishRelay<Boolean> fabVisibilityRelay = PublishRelay.create();

	private PublishRelay<Integer> progressVisibilityRelay = PublishRelay.create();

	private PublishRelay<Boolean> toolbarVisibilityRelay = PublishRelay.create();

	private PublishRelay<Integer> switchVisibilityRelay = PublishRelay.create();

	private PublishRelay<Integer> orientationModeRelay = PublishRelay.create();

	private PublishRelay<Integer> backButtonVisibilityRelay = PublishRelay.create();

	private PublishRelay<Integer> stepsRelay = PublishRelay.create();

	private PublishRelay<Integer> selectedRecipeIdRelay = PublishRelay.create();

	private PublishRelay<Integer> selectedStepIdRelay = PublishRelay.create();

	private PublishRelay<Integer> currentFragmentIdRelay = PublishRelay.create();

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

	public void setTitle(String title) {
		activityTitleRelay.accept(title);
	}

	public PublishRelay<String> getTitleChangingRelay() {
		return activityTitleRelay;
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

	public void showToolbar(boolean visible) {
		toolbarVisibilityRelay.accept(visible);
	}

	public PublishRelay<Boolean> getToolbarVisibilityRelay() {
		return toolbarVisibilityRelay;
	}

	public void showSwitch(boolean visible) {
		switchVisibilityRelay.accept(visible ? View.VISIBLE : View.GONE);
	}

	public PublishRelay<Integer> getSwitchVisibilityRelay() {
		return switchVisibilityRelay;
	}

	public void showBackButton(boolean visible) {
		backButtonVisibilityRelay.accept(visible ? View.VISIBLE : View.GONE);
	}

	public PublishRelay<Integer> getBackButtonVisibilityRelay() {
		return backButtonVisibilityRelay;
	}

	public void setOrientationLandscape(boolean landscape) {
		orientationModeRelay.accept(landscape ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	public PublishRelay<Integer> getOrientationModeRelay() {
		return orientationModeRelay;
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

	public void setSelectedRecipeId(int recipeId) {
		selectedRecipeIdRelay.accept(recipeId);
	}

	public PublishRelay<Integer> getSelectedRecipeIdRelay() {
		return selectedRecipeIdRelay;
	}

	public void setSelectedStepId(int stepId) {
		selectedStepIdRelay.accept(stepId);
	}

	public PublishRelay<Integer> getSelectedStepIdRelay() {
		return selectedStepIdRelay;
	}

	public void setCurrentFragmentId(int fragmentId) {
		currentFragmentIdRelay.accept(fragmentId);
	}

	public PublishRelay<Integer> getCurrentFragmentIdRelay() {
		return currentFragmentIdRelay;
	}
}
