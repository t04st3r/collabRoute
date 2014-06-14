package it.raffaeletosti.collabroute.connection;


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

import it.raffaeletosti.collabroute.RegistrationActivity;
import it.raffaeletosti.collabroute.model.UserHandler;


/**
 * Created by raffaele on 19/03/14.
 */
public class UserRegistrationHandler extends ConnectionHandler {

    UserHandler newbie;


    private JSONObject error;

    public UserRegistrationHandler(RegistrationActivity activity, UserHandler newbie) {
        super(activity);
        this.newbie = newbie;
        error = new JSONObject();
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        if (params[0].equals("registration")) {
            try {
                return sendRegistrationData();
            } catch (JSONException e) {
                System.err.println(e);
                return null;
            }
        }
        try {
            return sendConfirmation();
        } catch (JSONException e) {
            System.err.println(e);
            return null;
        }
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Sending request, hold on please");
        dialog.show();
    }

    @Override
    protected void onPostExecute(Object result) {

        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        JSONObject jsonResult = (JSONObject) result;
        try {
            String type = jsonResult.getString("type");
            if(type.equals("request"))
                ((RegistrationActivity)activity).checkResponse(result);
            else
                ((RegistrationActivity)activity).checkConfirmation(result);
        } catch (JSONException e) {
            System.err.println(e);
        }
    }

    protected JSONObject sendRegistrationData() throws JSONException {
        try {
            String urlString = "https://" + serverUrl + ":" + serverPort + "/add/user/";
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
            urlConnection.setConnectTimeout(3000);
            urlConnection.setSSLSocketFactory(context.getSocketFactory());
            urlConnection.setHostnameVerifier(allowEveryHost);
            urlConnection.setRequestMethod("POST");
            String urlParam = "name=" + newbie.getName() + "&mail=" + newbie.getEMail() + "&pass=" + newbie.getPassword();
            DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
            printout.writeBytes(urlParam);
            printout.flush();
            printout.close();
            InputStream in = urlConnection.getInputStream();
            String jsonToString = inputToString(in);
            in.close();
            return new JSONObject(jsonToString);
        } catch (SocketTimeoutException e) {
            System.err.println(e);
            error.put("type", "request").put("result", "CONN_TIMEDOUT");
            return error;
        } catch (ConnectException e) {
            System.err.println(e);
            error.put("type", "request").put("result", "CONN_REFUSED");
            return error;
        } catch (MalformedURLException e) {
            System.err.println(e);
            error.put("type", "request").put("result", "CONN_BAD_URL");
            return error;
        } catch (IOException e) {
            System.err.println(e);
            error.put("type", "request").put("result", "CONN_GENERIC_IO_ERROR");
            return error;
        } catch (IllegalArgumentException e) {
            System.err.println(e);
            error.put("type", "request").put("result", "CONN_GENERIC_ERROR");
            return error;
        } catch (Exception e) {
            System.err.println(e);
            error.put("type", "request").put("result", "CONN_GENERIC_ERROR");
            return error;
        }
    }

    protected JSONObject sendConfirmation() throws JSONException{
        try {
            String urlString = "https://" + serverUrl + ":" + serverPort + "/confirm/user/";
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
            urlConnection.setConnectTimeout(3000);
            urlConnection.setSSLSocketFactory(context.getSocketFactory());
            urlConnection.setHostnameVerifier(allowEveryHost);
            urlConnection.setRequestMethod("PUT");
            String urlParam = "mail=" + newbie.getEMail();
            DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
            printout.writeBytes(urlParam);
            printout.flush();
            printout.close();
            InputStream in = urlConnection.getInputStream();
            String jsonToString = inputToString(in);
            in.close();
            return new JSONObject(jsonToString);
        } catch (SocketTimeoutException e) {
            System.err.println(e);
            error.put("type", "confirm").put("result", "CONN_TIMEDOUT");
            return error;
        } catch (ConnectException e) {
            System.err.println(e);
            error.put("type", "confirm").put("result", "CONN_REFUSED");
            return error;
        } catch (MalformedURLException e) {
            System.err.println(e);
            error.put("type", "confirm").put("result", "CONN_BAD_URL");
            return error;
        } catch (IOException e) {
            System.err.println(e);
            error.put("type", "confirm").put("result", "CONN_GENERIC_IO_ERROR");
            return error;
        } catch (IllegalArgumentException e) {
            System.err.println(e);
            error.put("type", "confirm").put("result", "CONN_GENERIC_ERROR");
            return error;
        } catch (Exception e) {
            System.err.println(e);
            error.put("type", "confirm").put("result", "CONN_GENERIC_ERROR");
            return error;
        }
    }
}