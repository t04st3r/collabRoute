package it.digisin.collabroute.connection;

import android.app.Activity;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import it.digisin.collabroute.LoginActivity;
import it.digisin.collabroute.model.UserHandler;
import it.digisin.collabroute.travelListActivity;

/**
 * Created by raffaele on 28/03/14.
 */
public class TravelListHandler extends ConnectionHandler {

    public static UserHandler user;
    private JSONObject error;

    public enum Response {AUTH_FAILED, DATABASE_ERROR;}

    public TravelListHandler(Activity activity, UserHandler user) {
        super(activity);
        this.user = user;
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Getting travels list, hold on please");
        dialog.show();
    }

    @Override
    protected void onPostExecute(Object result) {
        if (dialog.isShowing()) {
            dialog.dismiss();
            ((travelListActivity)activity).fillTravelList((JSONObject)result);
        }
        try {
            JSONObject jsonResult = (JSONObject) result;
            String responseType = jsonResult.getString("type");
            if (responseType.equals("login")) {
                ((LoginActivity) activity).checkCredentials(jsonResult);
                return;
            }
            //((travelListActivity)activity).confirmationResponse(jsonResult);
        } catch (JSONException e) {
            System.err.println(e);
        }
    }

    @Override
    protected JSONObject doInBackground(String... param) {
        if (param[0].equals("list"))
            try {
                return retrieveTravelList();
            } catch (JSONException e) {
                System.err.println(e);
            }
        return null;
    }

    private JSONObject retrieveTravelList() throws JSONException {
        try {

            String urlString = "https://" + serverUrl + ":" + serverPort + "/travels/";
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
            urlConnection.setRequestProperty("id" , String.valueOf(user.getId()));
            urlConnection.setRequestProperty("token" , user.getToken());
            urlConnection.setConnectTimeout(3000);
            urlConnection.setSSLSocketFactory(context.getSocketFactory());
            urlConnection.setHostnameVerifier(allowEveryHost);
            InputStream in = urlConnection.getInputStream();
            //System.err.println(inputToString(in)); debug
            String jsonToString = inputToString(in);
            in.close();
            JSONObject jsonResponse = new JSONObject(jsonToString);
            return jsonResponse;
        } catch (SocketTimeoutException e) {
            System.err.println(e);
            error.put("result", "CONN_TIMEDOUT").put("type", "login");
            return error;
        } catch (ConnectException e) {
            System.err.println(e);
            error.put("result", "CONN_REFUSED").put("type", "login");
            return error;
        } catch (MalformedURLException e) {
            System.err.println(e);
            error.put("result", "CONN_BAD_URL").put("type", "login");
            return error;
        } catch (IOException e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_IO_ERROR").put("type", "login");
            return error;
        } catch (IllegalArgumentException e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_ERROR").put("type", "login");
            return error;
        } catch (Exception e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_ERROR").put("type", "login");
            return error;
        }
    }
}

