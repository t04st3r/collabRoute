package it.digisin.collabroute;


import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * Created by raffaele on 12/03/14.
 */
public class UserLoginHandler extends ConnectionHandler {

    /*Connection Errors */
    protected static final int CONN_TIMEDOUT = 2;
    protected static final int CONN_REFUSED = 3;
    protected static final int CONN_BAD_URL = 4;
    protected static final int CONN_GENERIC_IO_ERROR = 5;
    protected static final int CONN_GENERIC_ERROR = 6;
    protected static final int CONFIRM_MAIL_ERROR = 7;
    protected static final int EMAIL_NOT_FOUND = 8;
    protected static final int AUTH_FAILED = -1;
    protected static final int AUTH_DB_ERROR = 0;
    protected static final int OK = 1;

    public static UserHandler user;

    public static Map<Integer, String> errors = null;

    private JSONObject error;

    public LoginActivity login;

    public enum Response {OK, AUTH_FAILED, DATABASE_ERROR;}

    public UserLoginHandler(Context activity, UserHandler user, LoginActivity login) {
        super(activity);
        this.user = user;
        this.login = login;
        loadErrorMap();
        error = new JSONObject();
    }

    private void loadErrorMap() {
        if (errors == null) {
            errors = new HashMap<Integer, String>();
            errors.put(CONN_TIMEDOUT, "Connection Timed Out");
            errors.put(CONN_REFUSED, "Connection Refused");
            errors.put(CONN_BAD_URL, "Bad Url");
            errors.put(CONN_GENERIC_IO_ERROR, "I/O Error");
            errors.put(CONN_GENERIC_ERROR, "Generic Connection Error");
            errors.put(AUTH_FAILED, "Authentication Failed");
            errors.put(AUTH_DB_ERROR, "Database Error");
            errors.put(CONFIRM_MAIL_ERROR, "Error on sending confirm mail, please try again later");
            errors.put(EMAIL_NOT_FOUND, "Mail not found while confirm registration");
            errors.put(OK, "Welcome on board!");
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
        try {
            JSONObject jsonResult = (JSONObject) result;
            String responseType = jsonResult.getString("type");
            if(responseType.equals("login")){
                login.checkCredentials(jsonResult);
                return;
            }
            login.confirmationResponse(jsonResult);
        } catch (JSONException e) {
            System.err.println(e);
        }
    }
    @Override
    protected JSONObject doInBackground(String... params) {
        try {
            if (params[0].equals("login"))
                return doLoginData();
            return confirmUser();
        } catch (JSONException e) {
            System.err.println(e);
        }
        return null;
    }

    JSONObject doLoginData() throws JSONException {
        try {

            String urlString = "https://" + serverUrl + ":" + serverPort + "/auth/" + user.getEMail() + "/" + user.getPassword();
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
            InputStream in = urlConnection.getInputStream();
            //System.err.println(inputToString(in)); debug
            String jsonToString = inputToString(in);
            in.close();
            JSONObject jsonResponse = new JSONObject(jsonToString);
            return jsonResponse;
        } catch (SocketTimeoutException e) {
            System.err.println(e);
            error.put("result", "CONN_TIMEDOUT").put("type" , "login");
            return error;
        } catch (ConnectException e) {
            System.err.println(e);
            error.put("result", "CONN_REFUSED").put("type" , "login");
            return error;
        } catch (MalformedURLException e) {
            System.err.println(e);
            error.put("result", "CONN_BAD_URL").put("type" , "login");
            return error;
        } catch (IOException e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_IO_ERROR").put("type" , "login");
            return error;
        } catch (IllegalArgumentException e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_ERROR").put("type" , "login");
            return error;
        } catch (Exception e) {
            System.err.println(e);
            error.put("result", "CONN_GENERIC_ERROR").put("type" , "login");
            return error;
        }
    }

    protected JSONObject confirmUser() throws JSONException{
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
            urlConnection.setRequestMethod("POST");
            String urlParam = "mail=" + user.getEMail();
            DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
            printout.writeBytes(urlParam);
            printout.flush();
            printout.close();
            InputStream in = urlConnection.getInputStream();
            String jsonToString = inputToString(in);
            in.close();
            JSONObject jsonResponse = new JSONObject(jsonToString);
            return jsonResponse;
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
