package inc.ahmedmourad.bakery.utils;

import android.content.Context;
import android.os.Handler;

final class ConcurrencyUtils {

    /**
     * Got your context, run on Ui thread here
     * @param context Goat context
     * @param runnable the code to run
     */
    static void runOnUiThread(final Context context, final Runnable runnable) {
        new Handler(context.getMainLooper()).post(runnable);
    }
}
