package eu.michalkarzel.churchplayer;

import android.graphics.Bitmap;

/**
 * Created by misio on 21.3.15.
 */
public class Video {
    private long id;
    private String video;
    private Integer saved;
    private String folder;
    private Bitmap image;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getVideo() {
        return this.video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getFolder() {
        return this.folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public Integer getSaved() {
        return this.saved;
    }

    public void setSaved(Integer saved) {
        this.saved = saved;
    }

    public Bitmap getImage() {
        if (image != null) {
            return image;
        }

        return null;
    }

    public Bitmap setImage(Bitmap image) {
        this.image = image;
        return image;
    }

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return video;
    }
}