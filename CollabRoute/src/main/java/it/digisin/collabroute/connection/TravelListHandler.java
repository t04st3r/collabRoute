package it.digisin.collabroute.connection;

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

import it.digisin.collabroute.LoginActivity;
import it.digisin.collabroute.model.Travel;
import it.digisin.collabroute.model.UserHandler;
import it.digisin.collabroute.travelDetailActivity;
import it.digisin.collabroute.travelListActivity;

/**
 * Created by raffaele on 28/03/14.
 */
public class TravelListHandler extends ConnectionHandler {

    public static UserHandler user;
    public Travel travel;
    private JSONObject error;

    public enum Response {AUTH_FAILED, DATABASE_ERROR;}

    public TravelListHandler(Activity activity, UserHandler user) {
        super(activity);
        this.user = user;
        error = new JSONObject();
    }

    public TravelListHandler(Activity activity, Travel travel) {
        super(activity);
        this.travel = travel;
        error = new JSONObject();

    }


    @Override
    protected void onPostExecute(Object result) {
         try {
            JSONObject jsonResult = (JSONObject) result;
            String resultType = jsonResult.getString("type");
            if (resultType.equals("adm_mbr_list")) {
                ((travelListActivity) activity).fillTravelList((JSONObject) result);
                return;
            }
            if(resultType.equals("routes_list")){
                ((travelDetailActivity) activity).routesResponse((JSONObject) result);
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected JSONObject doInBackground(String... param) {
        try {
            if (param[0].equals("list"))
                return retrieveTravelList();
            if (param[0].equals("routes"))
                return retrieveTravelRoutes();
            if(param[0].equals("newTravel"))
                return addNewTravel(param[1]);
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
            urlConnection.setRequestProperty("id", String.valueOf(user.getId()));
            urlConnection.setRequestProperty("token", user.getToken());
            urlConnection.setConnectTimeout(3000);
            urlConnection.setSSLSocketFactory(context.getSocketFactory());
            urlConnection.setHostnameVerifier(allowEveryHost);
            InputStream in = urlConnection.getInputStream();
            String jsonToString = inputToString(in);
            in.close();
            return new JSONObject(jsonToString);
        } catch (SocketTimeoutException e) {
            System.err.println(e);
            error.put("result", "CONN_TIMEDOUT").put("type", "adm_mbr_list");
            return error;
        } catch (ConnectException e) {
            System.err.println(e);
            error.put("result", "CONN_REFUSED").put("type", "adm_mbr_list");
            return error;
        } catch (MalformedURLException e) {
            System.err.println(e);
            error.put("result", "CONN_BAD_URL").put("type", "adm_mbr_list");
            return error;
        } catch (IOException e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_IO_ERROR").put("type", "adm_mbr_list");
            return error;
        } catch (IllegalArgumentException e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_ERROR").put("type", "adm_mbr_list");
            return error;
        } catch (Exception e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_ERROR").put("type", "adm_mbr_list");
            return error;
        }
    }

    private JSONObject retrieveTravelRoutes() throws JSONException {
        try {
           String urlString = "https://" + serverUrl + ":" + serverPort + "/routes/" + travel.getId();
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
            urlConnection.setConnectTimeout(3000);
            urlConnection.setSSLSocketFactory(context.getSocketFactory());
            urlConnection.setHostnameVerifier(allowEveryHost);
            InputStream in = urlConnection.getInputStream();
            String jsonToString = inputToString(in);
            in.close();
            return new JSONObject(jsonToString);
        } catch (SocketTimeoutException e) {
            System.err.println(e);
            error.put("result", "CONN_TIMEDOUT").put("type", "routes_list");
            return error;
        } catch (ConnectException e) {
            System.err.println(e);
            error.put("result", "CONN_REFUSED").put("type", "routes_list");
            return error;
        } catch (MalformedURLException e) {
            System.err.println(e);
            error.put("result", "CONN_BAD_URL").put("type", "routes_list");
            return error;
        } catch (IOException e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_IO_ERROR").put("type", "routes_list");
            return error;
        } catch (IllegalArgumentException e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_ERROR").put("type", "routes_list");
            return error;
        } catch (Exception e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_ERROR").put("type", "routes_list");
            return error;
        }
    }

    private JSONObject addNewTravel(String request) throws JSONException {
        try {
            String urlString = "https://" + serverUrl + ":" + serverPort + "/add/travel/";
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
            urlConnection.setConnectTimeout(3000);
            urlConnection.setSSLSocketFactory(context.getSocketFactory());
            urlConnection.setHostnameVerifier(allowEveryHost);
            urlConnection.setRequestMethod("POST");
            DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
            printout.writeBytes(request);
            printout.flush();
            printout.close();
            InputStream in = urlConnection.getInputStream();
            String jsonToString = inputToString(in);
            in.close();
            JSONObject jsonResponse = new JSONObject(jsonToString);
            return jsonResponse;
        } catch (SocketTimeoutException e) {
            System.err.println(e);
            error.put("result", "CONN_TIMEDOUT").put("type", "add_new_travel");
            return error;
        } catch (ConnectException e) {
            System.err.println(e);
            error.put("result", "CONN_REFUSED").put("type", "add_new_travel");
            return error;
        } catch (MalformedURLException e) {
            System.err.println(e);
            error.put("result", "CONN_BAD_URL").put("type", "add_new_travel");
            return error;
        } catch (IOException e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_IO_ERROR").put("type", "add_new_travel");
            return error;
        } catch (IllegalArgumentException e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_ERROR").put("type", "add_new_travel");
            return error;
        } catch (Exception e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_ERROR").put("type", "add_new_travel");
            return error;
        }
    }
}

