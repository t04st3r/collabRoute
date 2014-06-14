package it.raffaeletosti.collabroute.connection;

import android.app.Activity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import it.raffaeletosti.collabroute.GMapFragment;
import it.raffaeletosti.collabroute.model.UserHandler;

/**
 * Created by raffaele on 19/04/14.
 */
public class CoordinatesHandler extends ConnectionHandler{

    UserHandler user;
    private JSONObject error;
    private GMapFragment fragment;
    String travelId;

    public CoordinatesHandler(Activity activity, UserHandler user, GMapFragment fragment, String travelId) {
        super(activity);
        this.user = user;
        error = new JSONObject();
        this.fragment = fragment;
        this.travelId = travelId;
    }

    @Override
    protected JSONObject doInBackground(String... param) {
        try {
            return updateLocation(param[0] , param[1]);
        } catch (JSONException e) {
            System.err.println(e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        fragment.confirmationResponse((JSONObject)o);

    }

    private JSONObject updateLocation(String longitude, String latitude) throws JSONException {
        try {

            String urlString = "https://" + serverUrl + ":" + serverPort + "/update/coordinates/";
            URL url = new URL(urlString);

            /** Create all-trusting host name verifier
             * to avoid the following :
             * java.security.cert.CertificateException: No name matching
             * This is because Java by default verifies that the certificate CN (Common Name) is
             * the same as host name in the URL. If they are not, the web service client fails.
             **/

            HostnameVerifier allowEveryHost = new HostnameVerifier() {

                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestProperty("id", String.valueOf(user.getId()));
            urlConnection.setRequestProperty("token", user.getToken());
            urlConnection.setRequestProperty("Content-Type" , "application/json");
            urlConnection.setConnectTimeout(3000);
            urlConnection.setSSLSocketFactory(context.getSocketFactory());
            urlConnection.setHostnameVerifier(allowEveryHost);
            urlConnection.setRequestMethod("PUT");
            DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
            printout.writeBytes(new JSONObject().put("longitude", longitude).put("latitude", latitude).put("travelId" , travelId).toString());
            printout.flush();
            printout.close();
            InputStream in = urlConnection.getInputStream();
            String jsonToString = inputToString(in);
            in.close();
            return new JSONObject(jsonToString);
        } catch (SocketTimeoutException e) {
            System.err.println(e);
            error.put("result", "CONN_TIMEDOUT").put("type", "update_coordinates");
            return error;
        } catch (ConnectException e) {
            System.err.println(e);
            error.put("result", "CONN_REFUSED").put("type", "update_coordinates");
            return error;
        } catch (MalformedURLException e) {
            System.err.println(e);
            error.put("result", "CONN_BAD_URL").put("type", "update_coordinates");
            return error;
        } catch (IOException e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_IO_ERROR").put("type", "update_coordinates");
            return error;
        } catch (IllegalArgumentException e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_ERROR").put("type", "update_coordinates");
            return error;
        } catch (Exception e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_ERROR").put("type", "update_coordinates");
            return error;
        }
    }

}
