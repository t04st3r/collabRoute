package it.digisin.collabroute;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.InputStreamReader;
import java.net.URL;


public class LoginActivity extends Activity {

    public static final String TAG_LOG = LoginActivity.class.getName();
    private String SERVER_ADDRESS;
    private int SERVER_PORT;
    private UserHandler User = null;
    private UserLoginHandler logIn = null;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        try {
            JsonReader conf = new JsonReader(new InputStreamReader(getResources().openRawResource(R.raw.config)));
            conf.beginObject();
            while (conf.hasNext()) {
                String nextValue = conf.nextName();
                if (nextValue.equals("SERVER_ADDRESS")) {
                    SERVER_ADDRESS = conf.nextString();
                }
                if (nextValue.equals("SERVER_PORT")) {
                    SERVER_PORT = conf.nextInt();
                }
            }
        } catch (Exception e) {
            Log.e(TAG_LOG, e.getMessage());
        }
        final EditText mailField = (EditText) findViewById(R.id.emailLogin);
        final Editable mailEdit = mailField.getText();
        final EditText passField = (EditText) findViewById(R.id.passwordLogin);
        final Editable passEdit = passField.getText();
        final Button loginButton = (Button) findViewById(R.id.buttonLogin);
        final Context context = getApplicationContext();
        final int duration = Toast.LENGTH_SHORT;
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = mailEdit.toString();
                String passwd = passEdit.toString();
                if (User == null) {
                    User = new UserHandler(mail, passwd);
                } else {
                    User.setEMail(mail);
                    User.setPassword(passwd);
                }
                String text;
                if (logIn == null) {
                    logIn = new UserLoginHandler(User, SERVER_ADDRESS, SERVER_PORT);
                }
                if (logIn.logIn()) {
                    text = "Successfully connected!";
                }
                else{
                    text = "Error on logging in";
                }
                Toast.makeText(context, text, duration).show();

            }
        });
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
