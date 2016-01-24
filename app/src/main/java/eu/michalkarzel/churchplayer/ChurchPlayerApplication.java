package eu.michalkarzel.churchplayer;

import android.app.Application;

import java.util.ArrayList;

/**
 * Created by misio on 20.3.15.
 */
public class ChurchPlayerApplication extends Application {

    private ArrayList<String> mVideoNames = new ArrayList<String>();
    private ArrayList<String> mVideoFolders = new ArrayList<String>();

    public ArrayList<String> getVideoFolders() {
        return mVideoFolders;
    }

    public void setVideoFolders(ArrayList<String> vf) {
        mVideoFolders = vf;
    }

    public ArrayList<String> getVideoNames() {
        return mVideoNames;
    }

    public void setVideoNames(ArrayList<String> vn) {
        mVideoNames = vn;
    }
}