package it.raffaeletosti.collabroute.connection;

import android.app.Activity;

import org.json.JSONArray;
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

import it.raffaeletosti.collabroute.RoutesFragment;
import it.raffaeletosti.collabroute.model.UserHandler;

/**
 * Created by raffaele on 26/05/14.
 */
public class RoutesHandler extends ConnectionHandler {
    UserHandler user;
    String idTravel;
    String idRoute;
    RoutesFragment routeFragment;
    JSONArray routes;
    private JSONObject error;

    public RoutesHandler(Activity activity, UserHandler user, String idTravel, RoutesFragment routeFragment, JSONArray routes) {
        super(activity);
        this.user = user;
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

    public RoutesHandler(Activity activity, RoutesFragment routeFragment, String idTravel, String idRoute, UserHandler user){
        super(activity);
        this.user = user;
        this.routeFragment = routeFragment;
        this.idTravel = idTravel;
        this.idRoute = idRoute;
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
            if(param[0].equals("addRoute"))
                return addRoutes();
            if(param[0].equals("deleteRoute")){
                return deleteRoute();
            }
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
            if(responseType.equals("add_new_routes")){
                routeFragment.routeAddedResponse(jsonResult);
            }
            if(responseType.equals("delete_route")){
                routeFragment.routeDeletedResponse(jsonResult);
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

    private JSONObject addRoutes() throws JSONException {
        try {

            String urlString = "https://" + serverUrl + ":" + serverPort + "/add/routes/";
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
            urlConnection.setRequestMethod("POST");
            DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
            printout.writeBytes(new JSONObject().put("travelId", idTravel).put("routes" , routes).toString());
            printout.flush();
            printout.close();
            InputStream in = urlConnection.getInputStream();
            String jsonToString = inputToString(in);
            in.close();
            return new JSONObject(jsonToString);
        } catch (SocketTimeoutException e) {
            System.err.println(e);
            error.put("result", "CONN_TIMEDOUT").put("type", "add_new_routes");
            return error;
        } catch (ConnectException e) {
            System.err.println(e);
            error.put("result", "CONN_REFUSED").put("type", "add_new_routes");
            return error;
        } catch (MalformedURLException e) {
            System.err.println(e);
            error.put("result", "CONN_BAD_URL").put("type", "add_new_routes");
            return error;
        } catch (IOException e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_IO_ERROR").put("type", "add_new_routes");
            return error;
        } catch (IllegalArgumentException e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_ERROR").put("type", "add_new_routes");
            return error;
        } catch (Exception e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_ERROR").put("type", "add_new_routes");
            return error;
        }
    }

    private JSONObject deleteRoute() throws JSONException {
        try {

            String urlString = "https://" + serverUrl + ":" + serverPort + "/delete/routes/";
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
            urlConnection.setRequestMethod("POST");
            DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
            printout.writeBytes(new JSONObject().put("travelId", idTravel).put("routeId" , idRoute).toString());
            printout.flush();
            printout.close();
            InputStream in = urlConnection.getInputStream();
            String jsonToString = inputToString(in);
            in.close();
            return new JSONObject(jsonToString);
        } catch (SocketTimeoutException e) {
            System.err.println(e);
            error.put("result", "CONN_TIMEDOUT").put("type", "delete_route");
            return error;
        } catch (ConnectException e) {
            System.err.println(e);
            error.put("result", "CONN_REFUSED").put("type", "delete_route");
            return error;
        } catch (MalformedURLException e) {
            System.err.println(e);
            error.put("result", "CONN_BAD_URL").put("type", "delete_route");
            return error;
        } catch (IOException e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_IO_ERROR").put("type", "delete_route");
            return error;
        } catch (IllegalArgumentException e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_ERROR").put("type", "delete_route");
            return error;
        } catch (Exception e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_ERROR").put("type", "delete_route");
            return error;
        }
    }

}
