package it.raffaeletosti.collabroute.connection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.JsonReader;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import it.raffaeletosti.collabroute.R;
import it.raffaeletosti.collabroute.ServerCertificateLoader;

/**
 * Created by raffaele on 19/03/14.
 */
public abstract class ConnectionHandler extends AsyncTask <String, Void, Object> {

    /*Connection Errors */
    public static final int CONN_TIMEDOUT = 2;
    public static final int CONN_REFUSED = 3;
    public static final int CONN_BAD_URL = 4;
    public static final int CONN_GENERIC_IO_ERROR = 5;
    public static final int CONN_GENERIC_ERROR = 6;
    public static final int EMAIL_SEND_ERROR = 7;
    public static final int EMAIL_NOT_FOUND = 8;
    public static final int EMAIL_EXISTS_ERROR = 9;
    public static final int AUTH_FAILED = -1;
    public static final int DB_ERROR = 0;
    public static final int OK = 1;
    public static final int WRONG_TOKEN = 10;

    protected static String serverUrl;
    protected static int serverPort;
    protected Activity activity;
    protected static SSLContext context = null;
    public static ProgressDialog dialog;

    public static Map<Integer, String> errors = null;

    protected ConnectionHandler(Activity activity) {
       this.activity = activity;
       dialog = new ProgressDialog(activity);

        /*initialize socket */
        if(serverUrl == null){
            //load JSON configuration file
            loadConfiguration();
        }
        /*initialize SSL context */
        if(context == null) {
            //load the 2048 bit SSL server certificate
            ServerCertificateLoader loader = new ServerCertificateLoader(activity);
            context = loader.load();
        }
        /*initialize errors */
        if(errors == null){
               loadErrorMap();
        }
    }

    private void loadErrorMap() {
            errors = new HashMap<Integer, String>();
            errors.put(CONN_TIMEDOUT, activity.getString(R.string.error_connectionTimedOut));
            errors.put(CONN_REFUSED, activity.getString(R.string.error_connectionRefused));
            errors.put(CONN_BAD_URL,activity.getString(R.string.error_connectionBadUrl));
            errors.put(CONN_GENERIC_IO_ERROR, activity.getString(R.string.error_connectionIOError));
            errors.put(CONN_GENERIC_ERROR, activity.getString(R.string.error_connectionError));
            errors.put(EMAIL_SEND_ERROR, activity.getString(R.string.error_mailForward));
            errors.put(EMAIL_EXISTS_ERROR,activity.getString(R.string.error_mailExists));
            errors.put(DB_ERROR, activity.getString(R.string.error_databaseError));
            errors.put(EMAIL_NOT_FOUND, activity.getString(R.string.error_mailNotFound));
            errors.put(OK, activity.getString(R.string.login_success));
            errors.put(AUTH_FAILED,activity.getString(R.string.error_authError));
            errors.put(WRONG_TOKEN, activity.getString(R.string.error_wrongToken));
        }


    protected abstract Object doInBackground(String... param); //implement in subclasses

    //parse in a string data from an InputStream
    public String inputToString(InputStream input) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        input.close();
        return sb.toString();
    }

    public void loadConfiguration() {
        try {
            InputStream input = activity.getResources().openRawResource(R.raw.config);
            String jsonString = inputToString(input);
            JSONObject object = new JSONObject(jsonString);
            serverUrl = object.getString("SERVER_ADDRESS");
            serverPort = object.getInt("SERVER_PORT");
        } catch (FileNotFoundException e) {
            System.err.println("Missing JSON config file: " + e);
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}