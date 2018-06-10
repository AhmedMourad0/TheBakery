package inc.ahmedmourad.bakery.bus;

import android.view.View;

import com.jakewharton.rxrelay2.PublishRelay;

import java.util.List;

import inc.ahmedmourad.bakery.model.room.entities.IngredientEntity;

/**
 * WARNING: VERY REACTIVE CLASS
 * A singleton object of this class is used to send data across the app via reactive streams
 */
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

	private final PublishRelay<Integer> upButtonVisibilityRelay = PublishRelay.create();

	private final PublishRelay<Integer> addToWidgetButtonVisibilityRelay = PublishRelay.create();

	private final PublishRelay<Integer> stepsRelay = PublishRelay.create();

	private final PublishRelay<Integer> selectedRecipeIdRelay = PublishRelay.create();

	private final PublishRelay<Integer> selectedStepPositionRelay = PublishRelay.create();

	private final PublishRelay<Integer> currentFragmentIdRelay = PublishRelay.create();

	private final PublishRelay<Integer> widgetDialogRecipeIdRelay = PublishRelay.create();

	private final PublishRelay<String> networkErrorRelay = PublishRelay.create();

	// To prevent instantiation outside the class
	private RxBus() {

	}

	/**
	 * get the singleton object of the class
	 *
	 * @return singleton object of the class
	 */
	public static RxBus getInstance() {
		return INSTANCE;
	}

	/**
	 * Send a signal that a recipe has been selected
	 *
	 * @param id the selected recipe's id
	 */
	public void selectRecipe(final int id) {
		recipesRelay.accept(id);
	}

	/**
	 * Listen for recipe selection signals
	 *
	 * @return a {@link PublishRelay} object which holds the stream for the emitted items
	 */
	public PublishRelay<Integer> getRecipeSelectionRelay() {
		return recipesRelay;
	}

	/**
	 * Send a signal to update the activity's title
	 *
	 * @param title the new title
	 */
	public void setTitle(final String title) {
		activityTitleRelay.accept(title);
	}

	/**
	 * Listen for title changing signals
	 *
	 * @return a {@link PublishRelay} object which holds the stream for the emitted items
	 */
	public PublishRelay<String> getTitleChangingRelay() {
		return activityTitleRelay;
	}

	/**
	 * Send a signal to update the activity's ingredients progressbar
	 *
	 * @param ingredients list of {@link IngredientEntity} to calculate progress from
	 */
	public void updateProgress(final List<IngredientEntity> ingredients) {
		ingredientsProgressRelay.accept(ingredients);
	}

	/**
	 * Listen for ingredient's progress changing signals
	 *
	 * @return a {@link PublishRelay} object which holds the stream for the emitted items
	 */
	public PublishRelay<List<IngredientEntity>> getIngredientsProgressRelay() {
		return ingredientsProgressRelay;
	}

	/**
	 * Send a signal to change the activity's ingredients progressbar visibility
	 *
	 * @param visible the new progressbar visibility
	 */
	public void showProgress(final boolean visible) {
		progressVisibilityRelay.accept(visible ? View.VISIBLE : View.GONE);
	}

	/**
	 * Listen for ingredient's progress visibility changing signals
	 *
	 * @return a {@link PublishRelay} object which holds the stream for the emitted items
	 */
	public PublishRelay<Integer> getProgressVisibilityRelay() {
		return progressVisibilityRelay;
	}

	/**
	 * Send a signal to change the activity's toolbar visibility
	 *
	 * @param visible the new toolbar visibility
	 */
	public void showToolbar(final boolean visible) {
		toolbarVisibilityRelay.accept(visible);
	}

	/**
	 * Listen for the activity's toolbar visibility changing signals
	 *
	 * @return a {@link PublishRelay} object which holds the stream for the emitted items
	 */
	public PublishRelay<Boolean> getToolbarVisibilityRelay() {
		return toolbarVisibilityRelay;
	}

	/**
	 * Send a signal to change the activity's switch visibility
	 *
	 * @param visible the new switch visibility
	 */
	public void showSwitch(final boolean visible) {
		switchVisibilityRelay.accept(visible ? View.VISIBLE : View.GONE);
	}

	/**
	 * Listen for the activity's switch visibility changing signals
	 *
	 * @return a {@link PublishRelay} object which holds the stream for the emitted items
	 */
	public PublishRelay<Integer> getSwitchVisibilityRelay() {
		return switchVisibilityRelay;
	}

	/**
	 * Send a signal to change the activity's up button visibility
	 *
	 * @param visible the new up button visibility
	 */
	public void showUpButton(final boolean visible) {
		upButtonVisibilityRelay.accept(visible ? View.VISIBLE : View.GONE);
	}

	/**
	 * Listen for the activity's up button visibility changing signals
	 *
	 * @return a {@link PublishRelay} object which holds the stream for the emitted items
	 */
	public PublishRelay<Integer> getUpButtonVisibilityRelay() {
		return upButtonVisibilityRelay;
	}

	/**
	 * Send a signal to change the activity's add to widget button visibility
	 *
	 * @param visible the new add to widget button visibility
	 */
	public void showAddToWidgetButton(final boolean visible) {
		addToWidgetButtonVisibilityRelay.accept(visible ? View.VISIBLE : View.GONE);
	}

	/**
	 * Listen for the activity's add to widget button visibility changing signals
	 *
	 * @return a {@link PublishRelay} object which holds the stream for the emitted items
	 */
	public PublishRelay<Integer> getAddToWidgetButtonVisibilityRelay() {
		return addToWidgetButtonVisibilityRelay;
	}

	/**
	 * Send a signal to change the activity's fab visibility
	 *
	 * @param visible the new fab visibility
	 */
	public void showFab(final boolean visible) {
		fabVisibilityRelay.accept(visible);
	}

	/**
	 * Listen for the activity's fab visibility changing signals
	 *
	 * @return a {@link PublishRelay} object which holds the stream for the emitted items
	 */
	public PublishRelay<Boolean> getFabVisibilityRelay() {
		return fabVisibilityRelay;
	}

	/**
	 * Send a signal that a step has been selected
	 *
	 * @param stepPosition the selected step's position
	 */
	public void selectStep(final int stepPosition) {
		stepsRelay.accept(stepPosition);
	}

	/**
	 * Listen for step selection signals
	 *
	 * @return a {@link PublishRelay} object which holds the stream for the emitted items
	 */
	public PublishRelay<Integer> getStepSelectionRelay() {
		return stepsRelay;
	}

	/**
	 * Send a signal to change all ingredients' selection state
	 *
	 * @param selected the new ingredients' selection state
	 */
	public void setAllIngredientsSelected(final boolean selected) {
		ingredientsSelectionRelay.accept(selected);
	}

	/**
	 * Listen for ingredients' selection state changing signals
	 *
	 * @return a {@link PublishRelay} object which holds the stream for the emitted items
	 */
	public PublishRelay<Boolean> getIngredientsSelectionRelay() {
		return ingredientsSelectionRelay;
	}

	/**
	 * Send a signal to notify that the selected recipe has changed
	 *
	 * @param recipeId the newly selected recipe id
	 */
	public void setSelectedRecipeId(final int recipeId) {
		selectedRecipeIdRelay.accept(recipeId);
	}

	/**
	 * Listen for selected recipe changes signals
	 *
	 * @return a {@link PublishRelay} object which holds the stream for the emitted items
	 */
	public PublishRelay<Integer> getSelectedRecipeIdRelay() {
		return selectedRecipeIdRelay;
	}

	/**
	 * Send a signal to notify that the selected step has changed
	 *
	 * @param stepPosition the newly selected step position
	 */
	public void setSelectedStepPosition(final int stepPosition) {
		selectedStepPositionRelay.accept(stepPosition);
	}

	/**
	 * Listen for selected step changes signals
	 *
	 * @return a {@link PublishRelay} object which holds the stream for the emitted items
	 */
	public PublishRelay<Integer> getSelectedStepPositionRelay() {
		return selectedStepPositionRelay;
	}

	/**
	 * Send a signal to notify that the current fragment has changed
	 *
	 * @param fragmentId the newly displayed fragment id
	 */
	public void setCurrentFragmentId(final int fragmentId) {
		currentFragmentIdRelay.accept(fragmentId);
	}

	/**
	 * Listen for current fragment changes signals
	 *
	 * @return a {@link PublishRelay} object which holds the stream for the emitted items
	 */
	public PublishRelay<Integer> getCurrentFragmentIdRelay() {
		return currentFragmentIdRelay;
	}

	/**
	 * Send a signal to with the recipe id for the widget dialog
	 *
	 * @param recipeId the recipe id associated with the dialog
	 */
	public void setWidgetDialogRecipeId(final int recipeId) {
		widgetDialogRecipeIdRelay.accept(recipeId);
	}

	/**
	 * Listen for widget dialog recipes ids signals
	 *
	 * @return a {@link PublishRelay} object which holds the stream for the emitted items
	 */
	public PublishRelay<Integer> getWidgetDialogRecipeIdRelay() {
		return widgetDialogRecipeIdRelay;
	}

	/**
	 * Send a signal to notify that a network error has occurred
	 *
	 * @param errorCode the code assigned to the error
	 */
	public void notifyNetworkError(final String errorCode) {
		networkErrorRelay.accept(errorCode);
	}

	/**
	 * Listen for network errors signals
	 *
	 * @return a {@link PublishRelay} object which holds the stream for the emitted items
	 */
	public PublishRelay<String> getNetworkErrorRelay() {
		return networkErrorRelay;
	}
}
