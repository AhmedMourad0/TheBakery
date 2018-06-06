package inc.ahmedmourad.bakery.bus;

import android.view.View;

import com.jakewharton.rxrelay2.PublishRelay;

import java.util.List;

import inc.ahmedmourad.bakery.model.room.entities.IngredientEntity;

public class RxBus {

	private static final RxBus INSTANCE = new RxBus();

	private final PublishRelay<String> activityTitleRelay = PublishRelay.create();

	private final PublishRelay<Integer> recipesRelay = PublishRelay.create();

	private final PublishRelay<List<IngredientEntity>> ingredientsProgressRelay = PublishRelay.create();

	private final PublishRelay<Boolean> ingredientsSelectionRelay = PublishRelay.create();

	private final PublishRelay<Boolean> fabVisibilityRelay = PublishRelay.create();

	private final PublishRelay<Integer> progressVisibilityRelay = PublishRelay.create();

	private final PublishRelay<Boolean> toolbarVisibilityRelay = PublishRelay.create();

	private final PublishRelay<Integer> switchVisibilityRelay = PublishRelay.create();

	private final PublishRelay<Integer> backButtonVisibilityRelay = PublishRelay.create();

	private final PublishRelay<Integer> addToWidgetButtonVisibilityRelay = PublishRelay.create();

	private final PublishRelay<Integer> stepsRelay = PublishRelay.create();

	private final PublishRelay<Integer> selectedRecipeIdRelay = PublishRelay.create();

	private final PublishRelay<Integer> selectedStepPositionRelay = PublishRelay.create();

	private final PublishRelay<Integer> currentFragmentIdRelay = PublishRelay.create();

	private final PublishRelay<String> networkErrorRelay = PublishRelay.create();

	private RxBus() {

	}

	public static RxBus getInstance() {
		return INSTANCE;
	}

	public void selectRecipe(final int id) {
		recipesRelay.accept(id);
	}

	public PublishRelay<Integer> getRecipeSelectionRelay() {
		return recipesRelay;
	}

	public void setTitle(final String title) {
		activityTitleRelay.accept(title);
	}

	public PublishRelay<String> getTitleChangingRelay() {
		return activityTitleRelay;
	}

	public void updateProgress(final List<IngredientEntity> ingredients) {
		ingredientsProgressRelay.accept(ingredients);
	}

	public PublishRelay<List<IngredientEntity>> getIngredientsProgressRelay() {
		return ingredientsProgressRelay;
	}

	public void showProgress(final boolean visible) {
		progressVisibilityRelay.accept(visible ? View.VISIBLE : View.GONE);
	}

	public PublishRelay<Integer> getProgressVisibilityRelay() {
		return progressVisibilityRelay;
	}

	public void showToolbar(final boolean visible) {
		toolbarVisibilityRelay.accept(visible);
	}

	public PublishRelay<Boolean> getToolbarVisibilityRelay() {
		return toolbarVisibilityRelay;
	}

	public void showSwitch(final boolean visible) {
		switchVisibilityRelay.accept(visible ? View.VISIBLE : View.GONE);
	}

	public PublishRelay<Integer> getSwitchVisibilityRelay() {
		return switchVisibilityRelay;
	}

	public void showBackButton(final boolean visible) {
		backButtonVisibilityRelay.accept(visible ? View.VISIBLE : View.GONE);
	}

	public PublishRelay<Integer> getBackButtonVisibilityRelay() {
		return backButtonVisibilityRelay;
	}

	public void showAddToWidgetButton(final boolean visible) {
		addToWidgetButtonVisibilityRelay.accept(visible ? View.VISIBLE : View.GONE);
	}

	public PublishRelay<Integer> getAddToWidgetButtonVisibilityRelay() {
		return addToWidgetButtonVisibilityRelay;
	}

	public void showFab(final boolean visible) {
		fabVisibilityRelay.accept(visible);
	}

	public PublishRelay<Boolean> getFabVisibilityRelay() {
		return fabVisibilityRelay;
	}

	public void selectStep(final int stepPosition) {
		stepsRelay.accept(stepPosition);
	}

	public PublishRelay<Integer> getStepSelectionRelay() {
		return stepsRelay;
	}

	public void setAllIngredientsSelected(final boolean selected) {
		ingredientsSelectionRelay.accept(selected);
	}

	public PublishRelay<Boolean> getIngredientsSelectionRelay() {
		return ingredientsSelectionRelay;
	}

	public void setSelectedRecipeId(final int recipeId) {
		selectedRecipeIdRelay.accept(recipeId);
	}

	public PublishRelay<Integer> getSelectedRecipeIdRelay() {
		return selectedRecipeIdRelay;
	}

	public void setSelectedStepPosition(final int stepPosition) {
		selectedStepPositionRelay.accept(stepPosition);
	}

	public PublishRelay<Integer> getSelectedStepPositionRelay() {
		return selectedStepPositionRelay;
	}

	public void setCurrentFragmentId(final int fragmentId) {
		currentFragmentIdRelay.accept(fragmentId);
	}

	public PublishRelay<Integer> getCurrentFragmentIdRelay() {
		return currentFragmentIdRelay;
	}

	public void notifyNetworkError(final String errorCode) {
		networkErrorRelay.accept(errorCode);
	}

	public PublishRelay<String> getNetworkErrorRelay() {
		return networkErrorRelay;
	}
}
