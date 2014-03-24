package it.digisin.collabroute;



import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;
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
public class UserLoginHandler extends ConnectionHandler{

     /*Connection Errors */
    protected static final int CONN_TIMEDOUT = 1;
    protected static final int CONN_REFUSED = 2;
    protected static final int CONN_BAD_URL = 3;
    protected static final int CONN_GENERIC_IO_ERROR = 4;
    protected static final int CONN_GENERIC_ERROR = 5;
    protected static final int AUTH_FAILED = -1;
    protected static final int AUTH_DB_ERROR = 0;

    public static UserHandler user;

    public static Map<Integer,String> errors = null;

    private JSONObject error;

    public LoginActivity login;

    public enum Response { OK,  AUTH_FAILED,  DATABASE_ERROR;}

    public UserLoginHandler(Context activity, UserHandler user, LoginActivity login) {
        super(activity);
        this.user = user;
        this.login = login;
        loadErrorMap();
        error = new JSONObject();
    }

    private void loadErrorMap(){
        if(errors == null) {
            errors = new HashMap<Integer, String>();
            errors.put(CONN_TIMEDOUT, "Connection Timed Out");
            errors.put(CONN_REFUSED, "Connection Refused");
            errors.put(CONN_BAD_URL, "Bad Url");
            errors.put(CONN_GENERIC_IO_ERROR, "I/O Error");
            errors.put(CONN_GENERIC_ERROR, "Generic Connection Error");
            errors.put(AUTH_FAILED , "Authentication Failed");
            errors.put(AUTH_DB_ERROR , "Database Error");
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
        login.checkCredentials(jsonResult);
    }


    @Override
    protected JSONObject doInBackground(String... params) {
        try{
            return doLoginData();
        }catch(JSONException e){System.err.println(e);}
        return null;
    }

    JSONObject doLoginData() throws JSONException{
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
           error.put("result", "CONN_TIMEDOUT");
           return error;
       } catch (ConnectException e) {
           System.err.println(e);
           error.put("result", "CONN_REFUSED");
           return error;
       } catch (MalformedURLException e) {
           System.err.println(e);
           error.put("result", "CONN_BAD_URL");
           return error;
       } catch (IOException e) {
           System.err.println(e);
           error.put("result", "CONN_GENERIC_IO_ERROR");
           return error;
       } catch (IllegalArgumentException e) {
           System.err.println(e);
           error.put("result", "CONN_GENERIC_ERROR");
           return error;
       } catch (Exception e) {
           System.err.println(e);
           error.put("result", "CONN_GENERIC_ERROR");
           return error;
       }
    }
}
