package inc.ahmedmourad.bakery.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.MediaButtonReceiver;

import inc.ahmedmourad.bakery.view.PlayerFragment;

/**
 * Broadcast Receiver registered to receive the MEDIA_BUTTON intent coming from clients.
 */
public class MediaReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        MediaButtonReceiver.handleIntent(PlayerFragment.mediaSession, intent);
    }
}