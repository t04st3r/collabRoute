package it.digisin.collabroute.connection;


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

import it.digisin.collabroute.R;
import it.digisin.collabroute.RegistrationActivity;
import it.digisin.collabroute.model.UserHandler;


/**
 * Created by raffaele on 19/03/14.
 */
public class UserRegistrationHandler extends ConnectionHandler {

    UserHandler newbie;

    /*Connection Errors */
    protected static final int EMAIL_SEND_ERROR = -2;
    protected static final int EMAIL_EXISTS_ERROR = -1;
    protected static final int DB_ERROR = 0;
    protected static final int OK = 1;
    protected static final int CONN_TIMEDOUT = 2;
    protected static final int CONN_REFUSED = 3;
    protected static final int CONN_BAD_URL = 4;
    protected static final int CONN_GENERIC_IO_ERROR = 5;
    protected static final int CONN_GENERIC_ERROR = 6;
    protected static final int EMAIL_NOT_FOUND = 7;
    protected static final int AUTH_FAILED = 8;

    public static Map<Integer, String> errors = null;

    private JSONObject error;

    public enum Response {OK, EMAIL_SEND_ERROR, DATABASE_ERROR, EMAIL_EXISTS_ERROR, EMAIL_NOT_FOUND;}

    public RegistrationActivity registration;

    public UserRegistrationHandler(Context activity, UserHandler newbie, RegistrationActivity registration) {
        super(activity);
        this.newbie = newbie;
        this.registration = registration;
        if (errors == null) {
            loadErrorMap();
        }
        error = new JSONObject();
    }

    private void loadErrorMap() {
        if (errors == null) {
            errors = new HashMap<Integer, String>();
            errors.put(CONN_TIMEDOUT, registration.getString(R.string.error_connectionTimedOut));
            errors.put(CONN_REFUSED, registration.getString(R.string.error_connectionRefused));
            errors.put(CONN_BAD_URL,registration.getString(R.string.error_connectionBadUrl));
            errors.put(CONN_GENERIC_IO_ERROR, registration.getString(R.string.error_connectionIOError));
            errors.put(CONN_GENERIC_ERROR, registration.getString(R.string.error_connectionError));
            errors.put(EMAIL_SEND_ERROR, registration.getString(R.string.error_mailForward));
            errors.put(EMAIL_EXISTS_ERROR,registration.getString(R.string.error_mailExists));
            errors.put(DB_ERROR, registration.getString(R.string.error_databaseError));
            errors.put(EMAIL_NOT_FOUND, registration.getString(R.string.error_mailNotFound));
            errors.put(OK, registration.getString(R.string.login_success));
            errors.put(AUTH_FAILED,registration.getString(R.string.error_authError));
        }
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
                registration.checkResponse(result);
            else
                registration.checkConfirmation(result);
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
            JSONObject jsonResponse = new JSONObject(jsonToString);
            return jsonResponse;
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
            urlConnection.setRequestMethod("POST");
            String urlParam = "mail=" + newbie.getEMail();
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