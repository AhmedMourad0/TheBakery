package inc.ahmedmourad.bakery.utils;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.OrientationEventListener;

// This code is taken from this SO answer https://stackoverflow.com/a/28732815/7411799 with some modifications applied
public final class OrientationUtils {

	private static final int STATE_IDLE = 0;
	private static final int STATE_WATCH_FOR_LANDSCAPE_CHANGES = 1;
	private static final int STATE_SWITCH_FROM_LANDSCAPE_TO_STANDARD = 2;
	private static final int STATE_WATCH_FOR_PORTRAIT_CHANGES = 3;
	private static final int STATE_SWITCH_FROM_PORTRAIT_TO_STANDARD = 4;

	private static int mSensorStateChanges;

	private static OrientationEventListener sensorEvent;

	public static void refreshSensorState(@Nullable Activity activity) {

		if (activity == null)
			return;

		boolean isAutoRotate = Settings.System.getInt(activity.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1;

		if (sensorEvent != null && isAutoRotate)
			sensorEvent.enable();
	}

	public static void setOrientationLandscape(@Nullable Activity activity, boolean landscape) {

		if (activity == null)
			return;

		boolean isAutoRotate = Settings.System.getInt(activity.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1;

		if (landscape)
			goFullScreen(activity, isAutoRotate);
		else
			shrinkToPortraitMode(activity, isAutoRotate);
	}

	private static void goFullScreen(@NonNull Activity activity, boolean isAutoRotate) {

		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		mSensorStateChanges = STATE_WATCH_FOR_LANDSCAPE_CHANGES;

		if (sensorEvent == null)
			initialiseSensor(activity, isAutoRotate);
		else if (isAutoRotate)
			sensorEvent.enable();
	}

	private static void shrinkToPortraitMode(@NonNull Activity activity, boolean isAutoRotate) {

		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mSensorStateChanges = STATE_WATCH_FOR_PORTRAIT_CHANGES;
		if (sensorEvent == null)
			initialiseSensor(activity, isAutoRotate);
		else if (isAutoRotate)
			sensorEvent.enable();
	}

	public static void reset(@Nullable Activity activity) {

		if (activity == null)
			return;

		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	/**
	 * Initialises system sensor to detect device orientation for player changes.
	 * Don't enable sensor until playback starts on player
	 */
	private static void initialiseSensor(Activity activity, boolean isAutoRotate) {

		sensorEvent = new OrientationEventListener(activity, SensorManager.SENSOR_DELAY_NORMAL) {
			@Override
			public void onOrientationChanged(int orientation) {
				/*
				 * This logic is useful when user explicitly changes orientation using player controls, in which case orientation changes gives no callbacks.
				 * we use sensor angle to anticipate orientation and make changes accordingly.
				 */
				if (mSensorStateChanges != STATE_IDLE &&
						mSensorStateChanges == STATE_WATCH_FOR_LANDSCAPE_CHANGES &&
						((orientation >= 60 && orientation <= 120) || (orientation >= 240 && orientation <= 300))) {

					mSensorStateChanges = STATE_SWITCH_FROM_LANDSCAPE_TO_STANDARD;

				} else if (mSensorStateChanges != STATE_IDLE &&
						mSensorStateChanges == STATE_SWITCH_FROM_LANDSCAPE_TO_STANDARD &&
						(orientation <= 40 || orientation >= 320)) {

					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
					mSensorStateChanges = STATE_IDLE;
					sensorEvent.disable();

				} else if (mSensorStateChanges != STATE_IDLE &&
						mSensorStateChanges == STATE_WATCH_FOR_PORTRAIT_CHANGES &&
						((orientation >= 300 && orientation <= 359) || (orientation >= 0 && orientation <= 45))) {

					mSensorStateChanges = STATE_SWITCH_FROM_PORTRAIT_TO_STANDARD;

				} else if (mSensorStateChanges != STATE_IDLE &&
						mSensorStateChanges == STATE_SWITCH_FROM_PORTRAIT_TO_STANDARD &&
						((orientation <= 300 && orientation >= 240) || (orientation <= 130 && orientation >= 60))) {

					activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
					mSensorStateChanges = STATE_IDLE;
					sensorEvent.disable();
				}
			}
		};

		if (isAutoRotate)
			sensorEvent.enable();
	}
}
