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


    public enum Response { OK,  AUTH_FAILED,  DATABASE_ERROR;}

    public UserLoginHandler(UserHandler user, Context activity) {
        super(activity);
        this.user = user;
        loadErrorMap();
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
    protected Object doInBackground(Object[] params) {
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
           //System.err.println(inputToString(in)); Used for debug purposes
           String jsonToString = inputToString(in);
           in.close();
           JSONObject jsonResponse = new JSONObject(jsonToString);
           String result = jsonResponse.getString("result");
           Response resultEnum = Response.valueOf(result);
           switch (resultEnum) {
               case OK:
                   return jsonToString;
               case DATABASE_ERROR:
                   return AUTH_DB_ERROR;
               default:
                   return AUTH_FAILED;
           }
       } catch (SocketTimeoutException e) {
           System.err.println(e);
           return CONN_TIMEDOUT;
       } catch (ConnectException e) {
           System.err.println(e);
           return CONN_REFUSED;
       } catch (MalformedURLException e) {
           System.err.println(e);
           return CONN_BAD_URL;
       } catch (IOException e) {
           System.err.println(e);
           return CONN_GENERIC_IO_ERROR;
       } catch (IllegalArgumentException e) {
           System.err.println(e);
           return CONN_GENERIC_ERROR;
       } catch (JSONException e) {
           System.err.println(e);
           return CONN_GENERIC_ERROR;
       } catch (Exception e) {
           System.err.println(e);
           return CONN_GENERIC_ERROR;
       }
    }
}
