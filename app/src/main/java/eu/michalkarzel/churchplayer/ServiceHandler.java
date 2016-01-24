package eu.michalkarzel.churchplayer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by misio on 21.1.15.
 */
public class ServiceHandler {

    public final static int GET = 1;
    public final static int POST = 2;
    static String response = null;

    public ServiceHandler() {

    }

    /**
     * Making service call
     *
     * @url - url to make request
     * @method - http request method
     */
    public String makeServiceCall(String url, int method) {
        return this.makeServiceCall(url, method, null);
    }

    /**
     * Making service call
     *
     * String @uri - url to make request
     * int @method - http request method
     * String @params - http request params
     */
    public String makeServiceCall(String uri, int method, String params) {
        try {
            // http client
            URL url = new URL(uri);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            try {
                // Checking http request method type
                if (method == POST) {
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);

                    //Send request
                    DataOutputStream wr = new DataOutputStream (
                            urlConnection.getOutputStream ());
                    wr.writeBytes(params);
                    wr.flush ();
                    wr.close ();

                }
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                response = readStream(in);
            } catch (java.io.IOException e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }

}
