package inc.ahmedmourad.bakery.utils;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import java.net.ConnectException;
import java.net.UnknownHostException;

import inc.ahmedmourad.bakery.R;
import inc.ahmedmourad.bakery.bus.RxBus;
import inc.ahmedmourad.bakery.view.activity.MainActivity;

import static inc.ahmedmourad.bakery.utils.ConcurrencyUtils.runOnUiThread;

public final class ErrorUtils {

	public static void critical(final FragmentActivity activity, final Throwable throwable) {

		general(activity, throwable);

		if (activity != null && !activity.isFinishing()) {
			activity.getSupportFragmentManager().popBackStackImmediate(MainActivity.TAG_RECIPES, 0);
			RxBus.getInstance().setCurrentFragmentId(MainActivity.FRAGMENT_RECIPES);
		}
	}

	static void network(final Context context, final Throwable throwable) {

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

	public static void general(final Context context, final Throwable throwable) {

		if (throwable == null)
			Toast.makeText(context,
					R.string.error,
					Toast.LENGTH_LONG
			).show();
		else if (throwable.getCause() == null)
			Toast.makeText(
					context,
					context.getString(
							R.string.error_no_cause,
							throwable.getLocalizedMessage()
					), Toast.LENGTH_LONG
			).show();
		else
			Toast.makeText(
					context,
					context.getString(
							R.string.error_cause,
							throwable.getLocalizedMessage(),
							throwable.getCause().getLocalizedMessage()
					), Toast.LENGTH_LONG
			).show();

		if (throwable != null)
			throwable.printStackTrace();
	}
}
