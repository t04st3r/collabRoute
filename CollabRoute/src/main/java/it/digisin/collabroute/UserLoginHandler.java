package it.digisin.collabroute;


import android.os.AsyncTask;
import android.util.Log;
import android.content.Context;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.*;

/**
 * Created by raffaele on 12/03/14.
 */
public class UserLoginHandler extends AsyncTask{

    public static final String TAG_LOG = UserLoginHandler.class.getName();
    private UserHandler user;
    private String serverUrl;
    private int serverPort;
    private Context activity;


    public UserLoginHandler(UserHandler user, String serverUrl, int serverPort, Context activity) {
        this.user = user;
        this.serverUrl = serverUrl;
        this.serverPort = serverPort;
        this.activity = activity;
    }
    //set in a string the result of an InputStream
    private String inputToString(InputStream input) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        input.close();
        return sb.toString();
    }
    //Load Self-Signed Certificate
    private void loadCertificate() {
        InputStream inputStream = activity.getResources().openRawResource(R.raw.collabcert);

    }

    @Override
    protected Object doInBackground(Object[] params) {
        try {
            String urlString = "https://" + serverUrl + ":" + serverPort + "/auth/" + user.getEMail() + "/" + user.getPassword();
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            InputStream in = urlConnection.getInputStream();
            String response = inputToString(in);
            Log.e(TAG_LOG, urlString);
        } catch (NullPointerException e) {
            System.err.println(e);
        } catch (MalformedURLException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        } catch (Exception e) {
            System.err.println(e);
        }
        return null;
    }
}
