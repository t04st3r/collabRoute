package it.raffaeletosti.collabroute.connection;

import android.app.Activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import it.raffaeletosti.collabroute.RoutesFragment;

/**
 * Created by raffaele on 26/05/14.
 */
public class RoutesHandler extends ConnectionHandler {
    String idUser;
    String idTravel;
    RoutesFragment routeFragment;
    JSONArray routes;
    private JSONObject error;

    public RoutesHandler(Activity activity, String idUser, String idTravel, RoutesFragment routeFragment, JSONArray routes) {
        super(activity);
        this.idUser = idUser;
        this.idTravel = idTravel;
        this.routeFragment = routeFragment;
        this.routes = routes;
        error = new JSONObject();
    }

    public RoutesHandler(Activity activity, RoutesFragment routeFragment){
        super(activity);
        this.routeFragment = routeFragment;
        error = new JSONObject();
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Sending data, please wait");
        dialog.show();
    }

    @Override
    protected Object doInBackground(String... param) {
        try {
            if (param[0].equals("geocoding"))
                return addressGeocodingRequest(param[1]);

        } catch (JSONException e) {
            System.err.println(e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object result) {
        if(dialog.isShowing()){
            dialog.dismiss();
        }
        try {
            JSONObject jsonResult = (JSONObject) result;
            String responseType = jsonResult.getString("type");
            if(responseType.equals("geocoding_request")){
                routeFragment.checkGeocodingResponse(jsonResult);
                return;
            }
        } catch (JSONException e) {
            System.err.println(e);
        }
    }

    private JSONObject addressGeocodingRequest(String queryString) throws JSONException {
        try {
            String urlString = "https://maps.googleapis.com/maps/api/geocode/json?"+queryString+"&key="+googleApiKey;
            URL url = new URL(urlString);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();
            String jsonToString = inputToString(in);
            in.close();
            return new JSONObject(jsonToString).put("type" , "geocoding_request");
        } catch (SocketTimeoutException e) {
            System.err.println(e);
            error.put("status", "CONN_TIMEDOUT").put("type", "geocoding_request");
            return error;
        } catch (ConnectException e) {
            System.err.println(e);
            error.put("status", "CONN_REFUSED").put("type", "geocoding_request");
            return error;
        } catch (MalformedURLException e) {
            System.err.println(e);
            error.put("status", "CONN_BAD_URL").put("type", "geocoding_request");
            return error;
        } catch (IOException e) {
            System.err.println(e);
            error.put("status", "CONN_GENERIC_IO_ERROR").put("type", "geocoding_request");
            return error;
        } catch (IllegalArgumentException e) {
            System.err.println(e);
            error.put("status", "CONN_GENERIC_ERROR").put("type", "geocoding_request");
            return error;
        } catch (Exception e) {
            System.err.println(e);
            error.put("status", "CONN_GENERIC_ERROR").put("type", "geocoding_request");
            return error;
        }
    }
}
