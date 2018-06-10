package inc.ahmedmourad.bakery.view.fragments;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.eralp.circleprogressview.CircleProgressView;
import com.eralp.circleprogressview.ProgressAnimationListener;
import com.google.android.exoplayer2.ExoPlaybackException;
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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import inc.ahmedmourad.bakery.R;
import inc.ahmedmourad.bakery.bus.RxBus;
import inc.ahmedmourad.bakery.datasource.CacheDataSourceFactory;
import inc.ahmedmourad.bakery.model.room.database.BakeryDatabase;
import inc.ahmedmourad.bakery.model.room.entities.StepEntity;
import inc.ahmedmourad.bakery.other.BundledFragment;
import inc.ahmedmourad.bakery.utils.CircleProgressViewUtils;
import inc.ahmedmourad.bakery.utils.ErrorUtils;
import inc.ahmedmourad.bakery.utils.OrientationUtils;
import inc.ahmedmourad.bakery.utils.PreferencesUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PlayerFragment extends BundledFragment {

	private static final String ARG_RECIPE_ID = "ri";
	private static final String ARG_STEP_POSITION = "sp";

	private static final String STATE_STEP_POSITION = "player_sp";
	private static final String STATE_PLAYER_POSITION = "player_pp";
	private static final String STATE_PLAYER_IS_PLAYING = "player_pip";
	private static final String STATE_DIALOG_IS_SHOWN = "player_dis";
	private static final String STATE_DIALOG_VALUE = "player_dv";

	private static final String MEDIA_SESSION_TAG = PlayerFragment.class.getSimpleName();

	private static final int DURATION_ANIMATION = 5000;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.player_player)
	PlayerView playerView;

	@SuppressWarnings("WeakerAccess")
	@Nullable
	@BindView(R.id.player_short_description)
	TextView shortDescriptionTextView;

	@SuppressWarnings("WeakerAccess")
	@Nullable
	@BindView(R.id.player_description)
	TextView descriptionTextView;

	@SuppressWarnings("WeakerAccess")
	@Nullable
	@BindView(R.id.player_position)
	TextView positionTextView;

	@SuppressWarnings("WeakerAccess")
	@Nullable
	@BindView(R.id.player_previous)
	Button previousButton;

	@SuppressWarnings("WeakerAccess")
	@Nullable
	@BindView(R.id.player_next)
	Button nextButton;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.player_auto_overlay)
	View autoNextOverlayLayout;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.player_auto_progressbar)
	CircleProgressView autoNextProgressBar;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.player_auto_next)
	Button autoNextButton;

	@SuppressWarnings("WeakerAccess")
	@BindView(R.id.player_auto_cancel)
	View autoCancelRelativeLayout;

	private ImageButton exoNextImageButton;
	private ImageButton exoPreviousImageButton;
	private TextView exoShortDescriptionTextView;

	private Context context;

	private List<StepEntity> stepsList;

	private int recipeId = -1;

	private int stepPosition = -1;

	public static MediaSessionCompat mediaSession;
	private MediaSessionConnector mediaSessionConnector;

	private SimpleExoPlayer exoPlayer;

	private ObjectAnimator animator;

	private NumberPicker numberPicker;

	private Disposable stepsDisposable;
	private Disposable stepsSelectionDisposable;

	private Unbinder unbinder;

	private volatile Bundle instanceState;

	private boolean playWhenReady = true;
	private long currentPlayerPosition = 0L;
	private boolean isDialogShown = false;
	private int dialogValue = -1;

	public static PlayerFragment newInstance(final int recipeId, final int stepPosition) {

		final Bundle args = new Bundle();

		args.putInt(ARG_RECIPE_ID, recipeId);
		args.putInt(ARG_STEP_POSITION, stepPosition);

		final PlayerFragment fragment = new PlayerFragment();
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			recipeId = getArguments().getInt(ARG_RECIPE_ID);
			stepPosition = getArguments().getInt(ARG_STEP_POSITION);
		}
	}

	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.fragment_player, container, false);

		context = view.getContext();

		unbinder = ButterKnife.bind(this, view);

		initializeMediaSession();

		exoNextImageButton = playerView.findViewById(R.id.next);
		exoPreviousImageButton = playerView.findViewById(R.id.prev);
		exoShortDescriptionTextView = playerView.findViewById(R.id.short_description);

		final ImageButton exoEnterFullscreenImageButton = playerView.findViewById(R.id.fullscreen_enter);
		final ImageButton exoExitFullscreenImageButton = playerView.findViewById(R.id.fullscreen_exit);

		exoNextImageButton.setOnClickListener(v -> playNext());
		exoPreviousImageButton.setOnClickListener(v -> playPrevious());

		if (exoEnterFullscreenImageButton != null) {
			if (getResources().getBoolean(R.bool.isTablet)) {
				exoEnterFullscreenImageButton.setVisibility(View.GONE);
			} else {
				exoEnterFullscreenImageButton.setVisibility(View.VISIBLE);
				exoEnterFullscreenImageButton.setOnClickListener(v -> OrientationUtils.setOrientationLandscape(getActivity(), true));
			}
		}

		if (exoExitFullscreenImageButton != null)
			exoExitFullscreenImageButton.setOnClickListener(v -> OrientationUtils.setOrientationLandscape(getActivity(), false));

		if (nextButton != null)
			nextButton.setOnClickListener(v -> playNext());

		if (previousButton != null)
			previousButton.setOnClickListener(v -> playPrevious());

		autoNextButton.setOnClickListener(v -> playNext());

		autoCancelRelativeLayout.setOnClickListener(v -> {
			autoNextOverlayLayout.setVisibility(View.GONE);
			animator.cancel();
			playerView.setUseController(true);
			exoPlayer.setPlayWhenReady(false);
		});

		autoNextProgressBar.setTextEnabled(false);

		autoNextProgressBar.addAnimationListener(new ProgressAnimationListener() {
			@Override
			public void onValueChanged(final float v) {

			}

			@Override
			public void onAnimationEnd() {
				if (autoNextProgressBar != null && Float.compare(autoNextProgressBar.getProgress(), 100f) == 0) {
					autoNextOverlayLayout.setVisibility(View.GONE);
					playNext();
				}
			}
		});

		if (positionTextView != null)
			positionTextView.setOnClickListener(v -> showGotoDialog());

		// draw behind status bar and hide it for landscape phones
		if (getResources().getBoolean(R.bool.isLandscapePhone)) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
						| View.SYSTEM_UI_FLAG_FULLSCREEN
						| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			} else {
				playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
						| View.SYSTEM_UI_FLAG_FULLSCREEN
						| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			}
		}

		stepsDisposable = BakeryDatabase.getInstance(context)
				.stepsDao()
				.getStepsByRecipeId(recipeId)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(steps -> {

					stepsList = steps;

					if (instanceState != null)
						restoreInstanceState();
					else if (exoPlayer != null && exoPlayer.getPlaybackState() == Player.STATE_IDLE)
						loadStep();

				}, throwable -> ErrorUtils.critical(getActivity(), throwable));

		return view;
	}

	private void loadStep() {

		if (stepPosition == -1)
			return;

		autoNextOverlayLayout.setVisibility(View.GONE);

		if (animator != null)
			animator.cancel();

		playerView.setUseController(true);

		final StepEntity step = stepsList.get(stepPosition);

		play(Uri.parse(step.videoUrl));

		if (shortDescriptionTextView != null)
			shortDescriptionTextView.setText(step.shortDescription);

		if (exoShortDescriptionTextView != null)
			exoShortDescriptionTextView.setText(step.shortDescription);

		if (descriptionTextView != null)
			descriptionTextView.setText(step.description);

		if (positionTextView != null)
			positionTextView.setText(getString(R.string.player_position, (stepPosition + 1), stepsList.size()));
	}

	private void showGotoDialog() {

		final Resources resources = getResources();

		numberPicker = new NumberPicker(new ContextThemeWrapper(context, R.style.DefaultNumberPickerTheme));
		numberPicker.setMinValue(1);
		numberPicker.setMaxValue(stepsList.size());
		numberPicker.setValue(dialogValue == -1 ? stepPosition + 1 : dialogValue);
		numberPicker.setWrapSelectorWheel(true);

		dialogValue = numberPicker.getValue();

		numberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> dialogValue = newVal);

		final FrameLayout layout = new FrameLayout(context);
		layout.addView(numberPicker, new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT,
				Gravity.CENTER)
		);

		final AlertDialog dialog = new AlertDialog.Builder(context)
				.setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
				.setTitle(R.string.go_to_step)
				.setPositiveButton(R.string.go, (d, which) -> {
					// clearing focus selects the value when the user enters it using soft keyboard
					// not clearing focus would cause the value not to be selected
					numberPicker.clearFocus();
					stepPosition = numberPicker.getValue() - 1;
					loadStep();
				}).setView(layout)
				.create();

		dialog.setOnShowListener(d -> {
			dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.colorSecondary));
			isDialogShown = true;
		});

		dialog.setOnDismissListener(d -> {
			isDialogShown = false;
			dialogValue = -1;
		});

		dialog.show();
	}

	/**
	 * Initializes the Media Session to be enabled with media buttons, transport controls, callbacks
	 * and media controller.
	 */
	private void initializeMediaSession() {

		releaseMediaSession();

		// Create a MediaSessionCompat.
		mediaSession = new MediaSessionCompat(context, MEDIA_SESSION_TAG);

		// Enable callbacks from MediaButtons and TransportControls.
		mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
				MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

		// Do not let MediaButtons restart the player when the app is not visible.
		mediaSession.setMediaButtonReceiver(null);

		// Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player.
		final PlaybackStateCompat state = new PlaybackStateCompat.Builder()
				.setActions(PlaybackStateCompat.ACTION_PLAY |
						PlaybackStateCompat.ACTION_PAUSE |
						PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
						PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
						PlaybackStateCompat.ACTION_PLAY_PAUSE
				).build();

		mediaSession.setPlaybackState(state);

		// Start the Media Session since the activity is active.
		mediaSession.setActive(true);

		mediaSessionConnector = new MediaSessionConnector(mediaSession);

		mediaSessionConnector.setErrorMessageProvider(throwable -> {
			throwable.printStackTrace();
			return new Pair<>(throwable.type, throwable.getLocalizedMessage());
		});
	}

	private void initializePlayer() {

		releasePlayer();

		exoPlayer = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(new DefaultBandwidthMeter())));

		exoPlayer.addListener(new Player.DefaultEventListener() {
			@Override
			public void onPlayerStateChanged(final boolean playWhenReady, final int playbackState) {
				super.onPlayerStateChanged(playWhenReady, playbackState);

				if (playWhenReady && playbackState == Player.STATE_ENDED) {
					if (!getResources().getBoolean(R.bool.isLandscapePhone) &&
							PreferencesUtils.defaultPrefs(context).getBoolean(PreferencesUtils.KEY_USE_AUTOPLAY, true) &&
							stepPosition != stepsList.size() - 1) {
						playerView.setUseController(false);
						autoNextProgressBar.setProgress(0f);
						autoNextOverlayLayout.setVisibility(View.VISIBLE);
						animator = CircleProgressViewUtils.setProgressWithAnimation(autoNextProgressBar, 100f, DURATION_ANIMATION);
					}
				}
			}

			@Override
			public void onPlayerError(ExoPlaybackException error) {
				super.onPlayerError(error);
				Toast.makeText(context, R.string.error_player, Toast.LENGTH_LONG).show();
				error.printStackTrace();
			}
		});

		playerView.setPlayer(exoPlayer);

		playerView.setDefaultArtwork(BitmapFactory.decodeResource(getResources(), R.drawable.ic_cupcake));

		mediaSessionConnector.setPlayer(exoPlayer, null);
		mediaSession.setActive(true);

		if (stepsList != null && stepsList.size() != 0 && exoPlayer.getPlaybackState() == Player.STATE_IDLE)
			loadStep();
	}

	private void playNext() {
		++stepPosition;
		loadStep();
	}

	private void playPrevious() {
		--stepPosition;
		loadStep();
	}

	private void play(final Uri mediaUri) {

		RxBus.getInstance().setSelectedStepPosition(stepPosition);

		final boolean enableNext = stepPosition != (stepsList.size() - 1);
		final boolean enablePrevious = stepPosition != 0;

		exoNextImageButton.setEnabled(enableNext);

		if (nextButton != null)
			nextButton.setEnabled(enableNext);

		exoPreviousImageButton.setEnabled(enablePrevious);

		if (previousButton != null)
			previousButton.setEnabled(enablePrevious);

		exoNextImageButton.setAlpha(enableNext ? 1f : 0.3f);

		if (nextButton != null)
			nextButton.setAlpha(enableNext ? 1f : 0.3f);

		exoPreviousImageButton.setAlpha(enablePrevious ? 1f : 0.3f);

		if (previousButton != null)
			previousButton.setAlpha(enablePrevious ? 1f : 0.3f);

		final MediaSource mediaSource = new ExtractorMediaSource.Factory(new CacheDataSourceFactory(context))
				.createMediaSource(mediaUri);

		exoPlayer.setPlayWhenReady(true);

		exoPlayer.prepare(mediaSource);
	}

	@Override
	public void onStart() {
		super.onStart();

		RxBus.getInstance().showUpButton(true);
		RxBus.getInstance().showAddToWidgetButton(true);
		RxBus.getInstance().setSelectedStepPosition(stepPosition);
		RxBus.getInstance().showSwitch(true);
		RxBus.getInstance().showToolbar(!getResources().getBoolean(R.bool.isLandscapePhone));
		RxBus.getInstance().showFab(false);
		RxBus.getInstance().showProgress(false);

		if (getResources().getBoolean(R.bool.useMasterDetailFlow)) {
			stepsSelectionDisposable = RxBus.getInstance()
					.getStepSelectionRelay()
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(position -> {
						if (position != -1 && position != stepPosition) {
							stepPosition = position;
							loadStep();
						}
					}, throwable -> ErrorUtils.general(getActivity(), throwable));
		}

		OrientationUtils.refreshSensorState(getActivity());

		initializePlayer();

		exoPlayer.setPlayWhenReady(playWhenReady);
		exoPlayer.seekTo(currentPlayerPosition);

		OrientationUtils.isTransactionDone = true;
	}

	@Override
	public void onStop() {

		if (stepsSelectionDisposable != null)
			stepsSelectionDisposable.dispose();

		RxBus.getInstance().setSelectedStepPosition(-1);
		RxBus.getInstance().showSwitch(false);

		if (exoPlayer != null) {
			playWhenReady = exoPlayer.getPlayWhenReady();
			currentPlayerPosition = exoPlayer.getCurrentPosition();
		}

		releaseResources();

		playerView.setPlayer(null);

		OrientationUtils.refreshSensorState(getActivity());

		super.onStop();
	}

	@Override
	public void onDestroy() {

		if (stepsDisposable != null)
			stepsDisposable.dispose();

		if (unbinder != null)
			unbinder.unbind();

		releaseResources();

		super.onDestroy();
	}

	@NonNull
	@Override
	public Bundle saveState() {

		final Bundle state = new Bundle();

		state.putInt(STATE_STEP_POSITION, stepPosition);

		if (exoPlayer != null) {
			state.putLong(STATE_PLAYER_POSITION, exoPlayer.getCurrentPosition());
			state.putBoolean(STATE_PLAYER_IS_PLAYING, exoPlayer.getPlayWhenReady());
		}

		state.putBoolean(STATE_DIALOG_IS_SHOWN, isDialogShown);

		if (dialogValue != -1 && numberPicker != null) {
			// clearing focus selects the value when the user enters it using soft keyboard
			// not clearing focus would cause the value not to be selected
			numberPicker.clearFocus();
			dialogValue = numberPicker.getValue();
		}

		state.putInt(STATE_DIALOG_VALUE, dialogValue);

		return state;
	}

	@Override
	public void restoreState(final Bundle stateBundle) {

		if (stateBundle != null)
			instanceState = stateBundle;

		if (exoPlayer != null && stepsList != null && stepsList.size() != 0)
			restoreInstanceState();
	}

	private synchronized void restoreInstanceState() {

		if (instanceState != null) {

			stepPosition = instanceState.getInt(STATE_STEP_POSITION, stepPosition);

			loadStep();

			exoPlayer.seekTo(instanceState.getLong(STATE_PLAYER_POSITION, 0L));

			exoPlayer.setPlayWhenReady(instanceState.getBoolean(STATE_PLAYER_IS_PLAYING, false));

			if (!getResources().getBoolean(R.bool.isLandscapePhone) &&
					instanceState.getBoolean(STATE_DIALOG_IS_SHOWN, false)) {
				dialogValue = instanceState.getInt(STATE_DIALOG_VALUE, -1);
				showGotoDialog();
			}

			instanceState = null;
		}
	}

	@Override
	public void releaseResources() {
		super.releaseResources();
		releasePlayer();
		releaseMediaSession();
	}

	private void releasePlayer() {

		if (exoPlayer != null) {
			exoPlayer.stop();
			exoPlayer.release();
			exoPlayer = null;
		}

		if (mediaSessionConnector != null)
			mediaSessionConnector.setPlayer(null, null);

		if (playerView != null)
			playerView.setPlayer(null);
	}

	private void releaseMediaSession() {
		if (mediaSession != null) {
			mediaSession.setActive(false);
			mediaSession.release();
		}
	}
}
