package it.raffaeletosti.collabroute.connection;

import android.app.Activity;
import android.support.v4.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import it.raffaeletosti.collabroute.DirectionsFragment;
import it.raffaeletosti.collabroute.TravelActivity;

public class DirectionsHandler extends ConnectionHandler{
    
    String queryString;
    JSONObject error;
    DirectionsFragment directions;
    
    public DirectionsHandler(Activity activity, String queryString, DirectionsFragment directions){
        super(activity);
        this.queryString = queryString;
        error = new JSONObject();
        this.directions = directions;
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Sending data, please wait");
        dialog.show();
    }

    @Override
    protected Object doInBackground(String... param) {
        try{
            return directionsRequest(queryString);
        }catch(JSONException e){
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
            directions.fillListView(jsonResult);
        } catch (JSONException e) {
            System.err.println(e);
        }
    }

    private JSONObject directionsRequest(String queryString) throws JSONException {
        try {
            String urlString = "https://maps.googleapis.com/maps/api/directions/json?"+queryString+"&key="+googleApiKey;
            //System.err.println(urlString);
            URL url = new URL(urlString);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();
            String jsonToString = inputToString(in);
            in.close();
            return new JSONObject(jsonToString).put("type" , "directions_response");
        } catch (SocketTimeoutException e) {
            System.err.println(e);
            error.put("status", "CONN_TIMEDOUT").put("type", "directions_response");
            return error;
        } catch (ConnectException e) {
            System.err.println(e);
            error.put("status", "CONN_REFUSED").put("type", "directions_response");
            return error;
        } catch (MalformedURLException e) {
            System.err.println(e);
            error.put("status", "CONN_BAD_URL").put("type", "directions_response");
            return error;
        } catch (IOException e) {
            System.err.println(e);
            error.put("status", "CONN_GENERIC_IO_ERROR").put("type", "directions_response");
            return error;
        } catch (IllegalArgumentException e) {
            System.err.println(e);
            error.put("status", "CONN_GENERIC_ERROR").put("type", "directions_response");
            return error;
        } catch (Exception e) {
            System.err.println(e);
            error.put("status", "CONN_GENERIC_ERROR").put("type", "directions_response");
            return error;
        }
    }
}