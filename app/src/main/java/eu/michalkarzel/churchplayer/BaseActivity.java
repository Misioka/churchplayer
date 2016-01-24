package eu.michalkarzel.churchplayer;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class BaseActivity extends Activity {

    public String mServerUrl = "http://www.michalkarzel.eu";
    public String mServerIP = "rtmp://31.31.73.231";

    protected Context context = this;
    protected JSONArray videos = null;
    protected VideoDataSource vds;
    protected List<DrawerItem> dataList;
    protected CustomDrawerAdapter adapter;
    protected DrawerLayout mDrawerLayout;
    protected ListView mDrawerList;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected CharSequence mDrawerTitle;
    protected CharSequence mTitle;
    protected ProgressDialog pDialog;

    protected boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    protected class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
            vds.setImage(result, bmImage.getContentDescription().toString());
        }
    }

    protected class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_video, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.reload:
                if (isNetworkAvailable()) {
                    if (this.getClass().getSimpleName() == "VideoActivity") {
                        Log.e("va", this.getClass().getSimpleName());
                        new GetVideos().execute();
                    } else {
                        Log.e("oa", this.getClass().getSimpleName());
                        new isPlayingStream().execute();
                    }
                } else {
                    Toast.makeText(context, R.string.no_connection, Toast.LENGTH_LONG).show();
                }
            /*case R.id.action_websearch:
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
                }
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void selectItem(int position) {
        //pDialog.setMessage(getString(R.string.wait));
        //pDialog.show();

        Bundle pos = new Bundle();
        pos.putInt("position", position);
        switch (position) {
            case 0:
                Intent onlineActivity = new Intent(BaseActivity.this, OnlineActivity.class);
                onlineActivity.putExtras(pos);
                startActivity(onlineActivity);
                this.finish();
                break;
            case 1:
                Intent videoActivity = new Intent(BaseActivity.this, VideoActivity.class);
                videoActivity.putExtras(pos);
                startActivity(videoActivity);
                this.finish();
                break;
            case 2:
                Intent programActivity = new Intent(BaseActivity.this, ProgramActivity.class);
                programActivity.putExtras(pos);
                startActivity(programActivity);
                this.finish();
                break;
        }

        mDrawerList.setItemChecked(position, true);
        /*setTitle(vds.getVideoBy("_id", "" + position).getVideo());*/
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        if (mTitle != null) getActionBar().setTitle(mTitle);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void createContent() {
        try {
            View.OnClickListener playVideo = new View.OnClickListener() {
                public void onClick(View v) {
                    Intent playActivity = new Intent(BaseActivity.this, PlayActivity.class);
                    Bundle pos = new Bundle();
                    pos.putString("video", v.getContentDescription().toString());
                    playActivity.putExtras(pos);
                    startActivity(playActivity);
                    finish();
                }
            };
            View.OnClickListener downloadVideo = new View.OnClickListener() {
                public void onClick(View v) {
                    TableRow tr = (TableRow) v.getParent();
                    tr.removeView(v);

                    final ProgressBar pb = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
                    pb.setProgress(0);

                    tr.addView(pb, 3);

                    String urlDownload = mServerUrl + "/videos/" + v.getContentDescription() + "/video.mp4";
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(urlDownload));

                    request.setDescription(getResources().getString(R.string.download_description) + " " + v.getContentDescription().toString() + ".mp4");
                    request.setTitle(getResources().getString(R.string.download));
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, v.getContentDescription().toString() + ".mp4");

                    final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

                    final long downloadId = manager.enqueue(request);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            boolean downloading = true;

                            while (downloading) {

                                DownloadManager.Query q = new DownloadManager.Query();
                                q.setFilterById(downloadId);

                                Cursor cursor = manager.query(q);
                                cursor.moveToFirst();
                                int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                                int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                                    downloading = false;
                                    String folder = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION));
                                    folder = folder.substring(folder.length() - 14, folder.length() - 4);
                                    vds.setSaved(folder);
                                }

                                final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);

                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        pb.setProgress(dl_progress);
                                    }
                                });
                                cursor.close();
                            }

                        }
                    }).start();
                }
            };
            List<Video> videos = vds.getAllVideos();
            Log.e("videos", String.valueOf(videos.size()));
            if (videos.size() > 0) {
                for (int i = 0; i < videos.size(); i++) {
                    try {
                        TableLayout tl = (TableLayout) findViewById(R.id.mainTable);
                        tl.removeAllViews();
                        TableRow tr = new TableRow(this);
                        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
                        tr.setId(i);
                        tr.setGravity(Gravity.CENTER);

                        ImageView preview = new ImageView(this);
                        String iUri = mServerUrl + "/videos/" + videos.get(i).getFolder() + "/image.png";
                        preview.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 0.1f));
                        preview.setOnClickListener(playVideo);
                        preview.setContentDescription(videos.get(i).getFolder());

                        if (videos.get(i).getImage() != null) {
                            preview.setImageBitmap(videos.get(i).getImage());
                        } else if (isNetworkAvailable()) {
                            new DownloadImageTask(preview).execute(iUri);
                        } else {
                            preview.setImageDrawable(getResources().getDrawable(R.drawable.mediacontroller_play));
                        }

                        TextView iName = new TextView(this);
                        String[] date = videos.get(i).getFolder().split("-");
                        iName.setContentDescription(videos.get(i).getFolder());
                        String isName = videos.get(i).getVideo() + "\n" + date[2] + "." + date[1] + "." + date[0];
                        iName.setText(isName);
                        iName.setTextColor(Color.WHITE);
                        iName.setTextSize(20);
                        iName.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 0.6f));
                        iName.setSingleLine(false);
                        iName.setPadding(10, 0, 0, 5);
                        iName.setOnClickListener(playVideo);

                        ImageView play = new ImageView(this);
                        play.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 0.15f));
                        play.setImageDrawable(getResources().getDrawable(R.drawable.play));
                        play.setOnClickListener(playVideo);
                        play.setContentDescription(videos.get(i).getFolder());

                        tr.addView(preview, 0);
                        tr.addView(iName, 1);
                        tr.addView(play, 2);

                        if (isNetworkAvailable() && videos.get(i).getSaved() == 0) {
                            ImageView download = new ImageView(this);
                            download.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 0.15f));
                            download.setImageDrawable(getResources().getDrawable(R.drawable.download));
                            download.setContentDescription(videos.get(i).getFolder());
                            download.setOnClickListener(downloadVideo);

                            tr.addView(download, 3);
                        } else if (videos.get(i).getSaved() != 0) {
                            ImageView download = new ImageView(this);
                            download.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                            download.setImageDrawable(getResources().getDrawable(R.drawable.downloaded));
                            download.setContentDescription(videos.get(i).getFolder());
                            download.setOnClickListener(downloadVideo);

                            tr.addView(download, 3);
                        }

                        tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT, 0.15f));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (pDialog != null) {
                if (pDialog.isShowing()) pDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Async task class to get json by making HTTP call
     */
    protected class isPlayingStream extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage(BaseActivity.this.getString(R.string.wait));
            if (!pDialog.isShowing()) pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            ServiceHandler sh = new ServiceHandler();

            String url = "http://31.31.73.231:8080/stat";
            String xmlStr = sh.makeServiceCall(url, ServiceHandler.GET);

            Log.d("Response: ", "> " + xmlStr);

            try {
                String[] streamSplit = (mServerUrl + "/live/applo").split("/");
                String stream = streamSplit[4];

                Log.d("Response: ", "> " + stream);

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(xmlStr));

                Document doc = db.parse(is);
                NodeList nodes = doc.getElementsByTagName("stream");
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element element = (Element) nodes.item(i);

                    NodeList name = element.getElementsByTagName("name");
                    String sname = getCharacterDataFromElement((Element) name.item(0));

                    NodeList liveNodes = element.getElementsByTagName("nclients");
                    int clients = Integer.valueOf(getCharacterDataFromElement((Element) liveNodes.item(0)));

                    NodeList bytesNodes = element.getElementsByTagName("bytes_in");
                    int bytes = Integer.valueOf(getCharacterDataFromElement((Element) bytesNodes.item(0)));
                    if (sname.equals(stream) && clients > 0 && bytes > 0) {
                        try {
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    View.OnClickListener playVideo = new View.OnClickListener() {
                                        public void onClick(View v) {
                                            Intent playActivity = new Intent(BaseActivity.this, PlayActivity.class);
                                            Bundle pos = new Bundle();
                                            pos.putString("video", v.getContentDescription().toString());
                                            playActivity.putExtras(pos);
                                            startActivity(playActivity);
                                            finish();
                                        }
                                    };

                                    ImageButton LQ = (ImageButton) findViewById(R.id.lq);
                                    ImageButton HQ = (ImageButton) findViewById(R.id.hq);
                                    ImageButton Audio = (ImageButton) findViewById(R.id.audio);

                                    LQ.setOnClickListener(playVideo);
                                    LQ.setImageDrawable(context.getResources().getDrawable(R.drawable.play_lq));

                                    HQ.setOnClickListener(playVideo);
                                    HQ.setImageDrawable(context.getResources().getDrawable(R.drawable.play_hq));

                                    Audio.setOnClickListener(playVideo);
                                    Audio.setImageDrawable(context.getResources().getDrawable(R.drawable.play_audio));
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        public String getCharacterDataFromElement(Element e) {
            Node child = e.getFirstChild();
            if (child instanceof CharacterData) {
                CharacterData cd = (CharacterData) child;
                return cd.getData();
            }
            return "?";
        }

        @Override
        protected void onPostExecute(Void result) {
            if (pDialog.isShowing()) { pDialog.hide();}
            super.onPostExecute(result);
        }

    }

    /**
     * Async task class to get json by making HTTP call
     */
    protected class GetVideos extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage(BaseActivity.this.getString(R.string.wait));
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {

            vds.deleteAll();
            List<Video> videosA = vds.getAllVideos();
            if (videosA.size() > 0 || !isNetworkAvailable()) {
                return null;
            }
            ServiceHandler sh = new ServiceHandler();

            String url = mServerUrl + "/videolist";
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    videos = jsonObj.getJSONArray("video");
                    for (int i = 0; i < videos.length(); i++) {
                        JSONObject c = videos.getJSONObject(i);

                        String name = new String(c.getString("name").getBytes("ISO-8859-1"), "UTF-8");
                        String folder = c.getString("folder");

                        vds.insertUpdate(name, folder);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (pDialog.isShowing()) { pDialog.hide();}
            super.onPostExecute(result);
            createContent();
        }

    }

    protected void setupDrawer() {
        dataList = new ArrayList<DrawerItem>();
        dataList.add(new DrawerItem("Online", R.drawable.o));
        dataList.add(new DrawerItem("Archív", R.drawable.a));
        dataList.add(new DrawerItem("Program", R.drawable.p));

        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        adapter = new CustomDrawerAdapter(this, R.layout.custom_drawer_item, dataList);

        mDrawerList.setAdapter(adapter);
    }
}
