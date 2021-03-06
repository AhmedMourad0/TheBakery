package inc.ahmedmourad.bakery.utils;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.OrientationEventListener;

// This code is taken from this SO answer https://stackoverflow.com/a/28732815/7411799
// with some improvements applied to suit this apps needs
public final class OrientationUtils {

	private static final int STATE_IDLE = 0;
	private static final int STATE_WATCH_FOR_LANDSCAPE_CHANGES = 1;
	private static final int STATE_SWITCH_FROM_LANDSCAPE_TO_STANDARD = 2;
	private static final int STATE_WATCH_FOR_PORTRAIT_CHANGES = 3;
	private static final int STATE_SWITCH_FROM_PORTRAIT_TO_STANDARD = 4;

	private static int sensorStateChanges;

	private static OrientationEventListener sensorEvent;

	public static boolean isTransactionDone = true;

	/**
	 * refreshes the sensor state, used to update the state when the user toggles auto rotate
	 *
	 * @param activity main activity
	 */
	public static void refreshSensorState(@Nullable final Activity activity) {

		if (activity == null)
			return;

		final boolean isAutoRotate = Settings.System.getInt(activity.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1;

		if (sensorEvent != null) {
			if (isAutoRotate)
				sensorEvent.enable();
			else
				sensorEvent.disable();
		}
	}

	/**
	 * Changes the orientation to either landscape or portrait
	 *
	 * @param activity  main activity
	 * @param landscape if true, orientation is changed to landscape, otherwise it's changed to portrait
	 */
	public static void setOrientationLandscape(@Nullable final Activity activity, final boolean landscape) {

		if (activity == null)
			return;

		isTransactionDone = false;

		final boolean isAutoRotate = Settings.System.getInt(activity.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1;

		if (landscape)
			switchToLandscapeMode(activity, isAutoRotate);
		else
			shrinkToPortraitMode(activity, isAutoRotate);
	}

	/**
	 * Changes the orientation to landscape
	 *
	 * @param activity     main activity
	 * @param isAutoRotate whether the user has auto rotate toggles or not
	 */
	private static void switchToLandscapeMode(@NonNull final Activity activity, final boolean isAutoRotate) {

		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		sensorStateChanges = STATE_WATCH_FOR_LANDSCAPE_CHANGES;

		if (sensorEvent == null)
			initialiseSensor(activity, isAutoRotate);
		else if (isAutoRotate)
			sensorEvent.enable();
	}

	/**
	 * Changes the orientation to portrait
	 *
	 * @param activity     main activity
	 * @param isAutoRotate whether the user has auto rotate toggles or not
	 */
	private static void shrinkToPortraitMode(@NonNull final Activity activity, final boolean isAutoRotate) {

		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		sensorStateChanges = STATE_WATCH_FOR_PORTRAIT_CHANGES;

		if (sensorEvent == null)
			initialiseSensor(activity, isAutoRotate);
		else if (isAutoRotate)
			sensorEvent.enable();
	}

	/**
	 * Used to reset everything back to normal so that the screen wouldn't be
	 * stuck in either of the modes when he leaves the app
	 *
	 * @param activity main activity
	 */
	public static void reset(@Nullable final Activity activity) {

		if (activity == null || !isTransactionDone)
			return;

		if (sensorEvent != null)
			sensorEvent.disable();

		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	/**
	 * Initialises system sensor to detect device orientation
	 *
	 * @param activity     main activity
	 * @param isAutoRotate whether the user has auto rotate toggles or not
	 */
	private static void initialiseSensor(final Activity activity, final boolean isAutoRotate) {

		sensorEvent = new OrientationEventListener(activity, SensorManager.SENSOR_DELAY_NORMAL) {
			@Override
			public void onOrientationChanged(final int orientation) {
				/*
				 * This logic is useful when user explicitly changes orientation using player controls, in which case orientation changes gives no callbacks.
				 * we use sensor angle to anticipate orientation and make changes accordingly.
				 */
				if (sensorStateChanges != STATE_IDLE &&
						sensorStateChanges == STATE_WATCH_FOR_LANDSCAPE_CHANGES &&
						((orientation >= 60 && orientation <= 120) || (orientation >= 240 && orientation <= 300))) {

					sensorStateChanges = STATE_SWITCH_FROM_LANDSCAPE_TO_STANDARD;

				} else if (sensorStateChanges != STATE_IDLE &&
						sensorStateChanges == STATE_SWITCH_FROM_LANDSCAPE_TO_STANDARD &&
						(orientation <= 40 || orientation >= 320)) {

					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
					sensorStateChanges = STATE_IDLE;
					sensorEvent.disable();

				} else if (sensorStateChanges != STATE_IDLE &&
						sensorStateChanges == STATE_WATCH_FOR_PORTRAIT_CHANGES &&
						((orientation >= 300 && orientation <= 359) || (orientation >= 0 && orientation <= 45))) {

					sensorStateChanges = STATE_SWITCH_FROM_PORTRAIT_TO_STANDARD;

				} else if (sensorStateChanges != STATE_IDLE &&
						sensorStateChanges == STATE_SWITCH_FROM_PORTRAIT_TO_STANDARD &&
						((orientation <= 300 && orientation >= 240) || (orientation <= 130 && orientation >= 60))) {

					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
					sensorStateChanges = STATE_IDLE;
					sensorEvent.disable();
				}
			}
		};

		if (isAutoRotate)
			sensorEvent.enable();
	}
}
