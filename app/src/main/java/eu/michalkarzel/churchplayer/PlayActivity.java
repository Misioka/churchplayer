package eu.michalkarzel.churchplayer;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import eu.michalkarzel.churchplayer.util.SystemUiHider;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

import io.vov.vitamio.LibsChecker;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class PlayActivity extends BaseActivity implements io.vov.vitamio.MediaPlayer.OnInfoListener {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
    Handler mHideHandler = new Handler();
    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    private ProgressDialog pDialog;
    private VideoView mVideoView;
    private ProgressBar pb;
    private TextView downloadRateView, loadRateView;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!LibsChecker.checkVitamioLibs(this))
            return;

        setContentView(R.layout.activity_play);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        final View water = findViewById(R.id.water);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.wait));
        pDialog.show();

        Bundle pos = getIntent().getExtras();
        String video = pos.getString("video").toUpperCase();


        pb = (ProgressBar) findViewById(R.id.probar);
        downloadRateView = (TextView) findViewById(R.id.download_rate);
        loadRateView = (TextView) findViewById(R.id.load_rate);
        mVideoView = (VideoView) findViewById(R.id.buffer);
        Uri uri;
        switch (video) {
            case "LQ":
                uri = Uri.parse(mServerIP + "/live/applo");
                break;
            case "HQ":
                uri = Uri.parse(mServerIP + "/live/apphi");
                break;
            case "AUDIO":
                uri = Uri.parse("http://stream.awrstudio.cz:8005/");
                break;
            default:
                uri = Uri.parse(mServerUrl + "/videos/" + video + "/video.mp4");
                break;
        }
        mVideoView.setVideoURI(uri);
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.requestFocus();
        mVideoView.setBufferSize(1);
        mVideoView.setOnInfoListener(this);
        mVideoView.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                if (pDialog.isShowing()) {
                    pDialog.hide();
                }
                mediaPlayer.setPlaybackSpeed(1.0f);
                mediaPlayer.start();
                Log.v("BU", "Buffer percentage done: " + i);
            }
        });
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                // optional need Vitamio 4.0
                if (pDialog.isShowing()) {
                    pDialog.hide();
                }
                mediaPlayer.setPlaybackSpeed(1.0f);
                mediaPlayer.start();
            }
        });

        /*// init player
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");

        mMediaController = new AndroidMediaController(this, false);
        mVideoView.setVideoURI(uri);
        mVideoView.start();*/

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, mVideoView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (visible) delayedHide(AUTO_HIDE_DELAY_MILLIS);
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        mVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        water.setOnTouchListener(mDelayHideTouchListener);

        /*Button backButton = (Button)this.findViewById(R.id.back);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });*/
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Intent videoActivity = new Intent(PlayActivity.this, VideoActivity.class);
            startActivity(videoActivity);
            this.finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Play Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://eu.michalkarzel.churchplayer/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Play Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://eu.michalkarzel.churchplayer/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (mVideoView.isPlaying()) {
                    pb.setVisibility(View.VISIBLE);
                    downloadRateView.setText("");
                    loadRateView.setText("");
                    downloadRateView.setVisibility(View.VISIBLE);
                    loadRateView.setVisibility(View.VISIBLE);

                }
                mp.start();
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                mVideoView.start();
                pb.setVisibility(View.GONE);
                downloadRateView.setVisibility(View.GONE);
                loadRateView.setVisibility(View.GONE);
                break;
        }
        return true;
    }

    /*public void startPlay(View view) {
        if (video.equals("LQ")) {
            mVideoView.setVideoPath(mServerUrl + "/live/applo");
        } else if (video.equals("HQ")) {
            mVideoView.setVideoPath(mServerUrl + "/live/apphi");
        } else if (video.equals("AUDIO")) {
            mVideoView.setVideoPath("http://stream.awrstudio.cz:8005/");
        } else {
            mVideoView.setVideoPath(mServerUrl + "/videos/" + video + "/video.mp4");
        }
    }

    public void openVideo(View View) {
        if (video.equals("LQ")) {
            mVideoView.setVideoPath(mServerUrl + "/live/applo");
        } else if (video.equals("HQ")) {
            mVideoView.setVideoPath(mServerUrl + "/live/apphi");
        } else if (video.equals("AUDIO")) {
            mVideoView.setVideoPath("http://stream.awrstudio.cz:8005/");
        } else {
            mVideoView.setVideoPath(mServerUrl + "/videos/" + video + "/video.mp4");
        }
    }*/
}
