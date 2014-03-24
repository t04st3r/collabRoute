package it.digisin.collabroute;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.JsonReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.net.ssl.SSLContext;

/**
 * Created by raffaele on 19/03/14.
 */
public abstract class ConnectionHandler extends AsyncTask <String, Void, Object> {


    protected static String serverUrl;
    protected static int serverPort;
    protected Context activity;
    protected static SSLContext context = null;
    ProgressDialog dialog;

    protected ConnectionHandler(Context activity) {
       this.activity = activity;
       this.dialog = new ProgressDialog(activity);

        if(serverUrl == null){
            //load JSON configuration file
            loadConfiguration();
        }

        if (context == null) {
            //load the 2048 bit SSL server certificate
            ServerCertificateLoader loader = new ServerCertificateLoader(activity);
            context = loader.load();
        }
    }

    protected abstract Object doInBackground(String... param);

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
            JsonReader conf = new JsonReader(new InputStreamReader(activity.getResources().openRawResource(R.raw.config)));
            conf.beginObject();
            while (conf.hasNext()) {
                String nextValue = conf.nextName();
                if (nextValue.equals("SERVER_ADDRESS")) {
                    serverUrl = conf.nextString();
                }
                if (nextValue.equals("SERVER_PORT")) {
                    serverPort = conf.nextInt();
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Missing JSON config file: " + e);
        } catch (Exception e) {
            System.err.println(e);
        }
    }


}