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

    public static Map<Integer,String> errors = null;

    public enum Response {OK, EMAIL_SEND_ERROR, DATABASE_ERROR, EMAIL_EXISTS_ERROR, EMAIL_NOT_FOUND;}


    public UserRegistrationHandler(Context activity, UserHandler newbie) {
        super(activity);
        this.newbie = newbie;
        if(errors == null) {
            loadErrorMap();
        }
    }

    private void loadErrorMap(){
        if(errors == null) {
            errors = new HashMap<Integer, String>();
            errors.put(CONN_TIMEDOUT, "Connection Timed Out");
            errors.put(CONN_REFUSED, "Connection Refused");
            errors.put(CONN_BAD_URL, "Bad Url");
            errors.put(CONN_GENERIC_IO_ERROR, "I/O Error");
            errors.put(CONN_GENERIC_ERROR, "Generic Connection Error");
            errors.put(EMAIL_SEND_ERROR , "Confirmation Email Forward Failure");
            errors.put(EMAIL_EXISTS_ERROR , "Email Address already registered, use another one");
            errors.put(DB_ERROR , "Database Error");
            errors.put(EMAIL_NOT_FOUND , "Mail not found while confirm registration");
            errors.put(OK , "Welcome on board ");
        }
    }

    @Override
    protected Object doInBackground(String... params) {
        if(params[0] == "registration"){
            return sendRegistrationData();
        }
        return sendConfirmation();
    }

    protected Object sendRegistrationData(){
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
            String urlParam = "name="+newbie.getName()+"&mail="+newbie.getEMail()+"&pass="+newbie.getPassword();
            DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
            printout.writeBytes(urlParam);
            printout.flush();
            printout.close();
            InputStream in = urlConnection.getInputStream();
            String jsonToString = inputToString(in);
            JSONObject jsonResponse = new JSONObject(jsonToString);
            String result = jsonResponse.getString("result");
            System.err.println("response: "+result);
            Response resultEnum = Response.valueOf(result);
            in.close();
            switch (resultEnum) {
                case OK:
                    return jsonToString;
                case EMAIL_EXISTS_ERROR:
                    return EMAIL_EXISTS_ERROR;
                case EMAIL_SEND_ERROR:
                    return EMAIL_SEND_ERROR;
                default:
                    return DB_ERROR;
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

    protected Integer sendConfirmation(){
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
            String urlParam = "mail="+newbie.getEMail();
            DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
            printout.writeBytes(urlParam);
            printout.flush();
            printout.close();
            InputStream in = urlConnection.getInputStream();
            String jsonToString = inputToString(in);
            in.close();
            JSONObject jsonResponse = new JSONObject(jsonToString);
            String result = jsonResponse.getString("result");
            System.err.println("response: "+result);
            Response resultEnum = Response.valueOf(result);
            switch (resultEnum) {
                case OK:
                    return OK;
                case EMAIL_NOT_FOUND:
                    return EMAIL_NOT_FOUND;
                default:
                    return DB_ERROR;
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