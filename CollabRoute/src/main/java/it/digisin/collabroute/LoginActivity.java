package it.digisin.collabroute;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONObject;

import android.util.JsonReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.util.Log;

import java.io.InputStreamReader;
import java.net.URL;


public class LoginActivity extends Activity {

    private static final String TAG_LOG = LoginActivity.class.getName();
    private UserHandler User;
    private String SERVER_ADDRESS;
    private int SERVER_PORT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        try {
            JsonReader conf = new JsonReader(new InputStreamReader(getResources().openRawResource(R.raw.config)));
            conf.beginObject();
            while(conf.hasNext()) {
                String nextValue = conf.nextName();
               if (nextValue.equals("SERVER_ADDRESS")) {
                    SERVER_ADDRESS = conf.nextString();
                }
                if (nextValue.equals("SERVER_PORT")) {
                    SERVER_PORT = conf.nextInt();
                }
            }
            Log.e(TAG_LOG , SERVER_ADDRESS+SERVER_PORT);
        } catch (Exception e) {
            Log.e(TAG_LOG, "Mannaggia a dio "+e.getMessage());

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
