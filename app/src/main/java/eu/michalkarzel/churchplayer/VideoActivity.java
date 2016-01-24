package eu.michalkarzel.churchplayer;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import io.vov.vitamio.LibsChecker;

public class VideoActivity extends BaseActivity {

    final Context context = this;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        pDialog = new ProgressDialog(this);
        pDialog.setTitle("Načítám archív ...");
        pDialog.show();
        vds = new VideoDataSource(context);
        vds.open();

        setContentView(R.layout.splash);

        try {
            new GetVideos().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!LibsChecker.checkVitamioLibs(this))
            return;

        setupDrawer();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }
}