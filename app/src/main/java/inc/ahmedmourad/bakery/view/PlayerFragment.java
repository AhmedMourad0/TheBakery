package inc.ahmedmourad.bakery.view;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import inc.ahmedmourad.bakery.R;
import inc.ahmedmourad.bakery.datasource.CacheDataSourceFactory;
import inc.ahmedmourad.bakery.model.room.database.BakeryDatabase;
import inc.ahmedmourad.bakery.model.room.entities.StepEntity;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PlayerFragment extends Fragment {

    public static final String ARG_RECIPE_ID = "ri";
    public static final String ARG_STEP_ID = "si";

    private static final String MEDIA_SESSION_TAG = PlayerFragment.class.getSimpleName();

    @BindView(R.id.player_player)
    PlayerView playerView;

    @BindView(R.id.player_short_description)
    TextView shortDescription;

    @BindView(R.id.player_description)
    TextView description;

    private int recipeId = -1;
    private int stepId = -1;

    public static MediaSessionCompat mediaSession;
    private MediaSessionConnector mediaSessionConnector;

    private SimpleExoPlayer exoPlayer;

    private Disposable disposable;

    private Unbinder unbinder;

    public static PlayerFragment newInstance(int recipeId, int stepId) {

        Bundle args = new Bundle();

        args.putInt(ARG_RECIPE_ID, recipeId);
        args.putInt(ARG_STEP_ID, stepId);

        PlayerFragment fragment = new PlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            recipeId = getArguments().getInt(ARG_RECIPE_ID);
            stepId = getArguments().getInt(ARG_STEP_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_player, container, false);

        final Context context = view.getContext();

        unbinder = ButterKnife.bind(this, view);

        /**/ /**/ /**/
        // Load the question mark as the background image until the user answers the question.
        playerView.setDefaultArtwork(BitmapFactory.decodeResource(getResources(), R.drawable.ic_cupcake));
        /**/ /**/ /**/

        initializeMediaSession(context);

        disposable = Single.<StepEntity>create(emitter ->
                emitter.onSuccess(BakeryDatabase.getInstance(context)
                        .stepsDao()
                        .getStepById(recipeId, stepId)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(step -> {

                    if (step != null) {

                        initializePlayer(context, Uri.parse(step.videoUrl));

                        shortDescription.setText(step.shortDescription);

                        description.setText(step.description);
                    }

                }, throwable -> MainActivity.handleError(getActivity(), throwable));

        return view;
    }

    /**
     * Initializes the Media Session to be enabled with media buttons, transport controls, callbacks
     * and media controller.
     */
    private void initializeMediaSession(final Context context) {

        // Create a MediaSessionCompat.
        mediaSession = new MediaSessionCompat(context, MEDIA_SESSION_TAG);

        // Enable callbacks from MediaButtons and TransportControls.
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Do not let MediaButtons restart the player when the app is not visible.
        mediaSession.setMediaButtonReceiver(null);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player.
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                        PlaybackStateCompat.ACTION_PLAY_PAUSE
                );

        mediaSession.setPlaybackState(stateBuilder.build());

        // Start the Media Session since the activity is active.
        mediaSession.setActive(true);

        mediaSessionConnector = new MediaSessionConnector(mediaSession);

        mediaSessionConnector.setErrorMessageProvider(throwable -> {
            throwable.printStackTrace();
            return new Pair<>(throwable.type, throwable.getLocalizedMessage());
        });
    }

    /**
     * Initialize ExoPlayer.
     *
     * @param mediaUri The URI of the sample to play.
     */
    private void initializePlayer(Context context, Uri mediaUri) {

        if (exoPlayer == null) {

            exoPlayer = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(new DefaultBandwidthMeter())));

            MediaSource mediaSource = new ExtractorMediaSource.Factory(new CacheDataSourceFactory(context, 100 * 1024 * 1024, 20 * 1024 * 1024)).createMediaSource(mediaUri);

            exoPlayer.setPlayWhenReady(true);

            exoPlayer.prepare(mediaSource);

            playerView.setPlayer(exoPlayer);

            mediaSessionConnector.setPlayer(exoPlayer, new MediaSessionConnector.PlaybackPreparer() {
                @Override
                public long getSupportedPrepareActions() {
                    return 0;
                }

                @Override
                public void onPrepare() {

                }

                @Override
                public void onPrepareFromMediaId(String mediaId, Bundle extras) {

                }

                @Override
                public void onPrepareFromSearch(String query, Bundle extras) {

                }

                @Override
                public void onPrepareFromUri(Uri uri, Bundle extras) {

                }

                @Override
                public String[] getCommands() {
                    return new String[0];
                }

                @Override
                public void onCommand(Player player, String command, Bundle extras, ResultReceiver cb) {

                }
            });
        }
    }

    @Override
    public void onStop() {
        disposable.dispose();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();
        releasePlayer();
        mediaSession.setActive(false);
        super.onDestroy();
    }

    /**
     * Release ExoPlayer.
     */
    private void releasePlayer() {
//        mNotificationManager.cancelAll();
        exoPlayer.stop();
        exoPlayer.release();
        exoPlayer = null;
    }
}
