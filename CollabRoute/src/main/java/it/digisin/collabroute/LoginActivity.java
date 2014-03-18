package it.digisin.collabroute;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;



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
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = mailEdit.toString();
                String passwd = passEdit.toString();
                EmailValidator validator = new EmailValidator();
                if (mail.equals("") || passwd.equals("") || !validator.validate(mail)) {
                    Toast.makeText(context, "Email or Password missing or incorrect", Toast.LENGTH_SHORT).show();
                } else {
                    if (User == null) {
                        User = new UserHandler(mail, passwd);
                    } else {
                        User.setEMail(mail);
                        User.setPassword(passwd);
                    }
                     UserLoginHandler login = new UserLoginHandler(User, SERVER_ADDRESS, SERVER_PORT, getApplicationContext()); //extend AsyncTask and run with a separate thread
                     login.execute(); //start the thread
                     Object result = null;
                    try {
                         result = login.get();
                    } catch (InterruptedException e) {
                        System.err.println(e);
                    } catch (ExecutionException e) {
                        System.err.println(e);
                    }
                    if(result != null) {
                        if (result instanceof Integer) {
                            int resultInt = ((Integer) result).intValue();
                            switch (resultInt) {
                                case UserLoginHandler.AUTH_FAILED:
                                    Toast.makeText(context, UserLoginHandler.errors.get(UserLoginHandler.AUTH_FAILED), Toast.LENGTH_SHORT).show();
                                    break;
                                case UserLoginHandler.CONN_REFUSED:
                                    Toast.makeText(context, UserLoginHandler.errors.get(UserLoginHandler.CONN_REFUSED), Toast.LENGTH_SHORT).show();
                                    break;
                                case UserLoginHandler.CONN_BAD_URL:
                                    Toast.makeText(context, UserLoginHandler.errors.get(UserLoginHandler.CONN_BAD_URL), Toast.LENGTH_SHORT).show();
                                    break;
                                case UserLoginHandler.CONN_GENERIC_IO_ERROR:
                                    Toast.makeText(context, UserLoginHandler.errors.get(UserLoginHandler.CONN_GENERIC_IO_ERROR), Toast.LENGTH_SHORT).show();
                                    break;
                                case UserLoginHandler.CONN_GENERIC_ERROR:
                                    Toast.makeText(context, UserLoginHandler.errors.get(UserLoginHandler.CONN_GENERIC_ERROR), Toast.LENGTH_SHORT).show();
                                    break;
                                case UserLoginHandler.CONN_TIMEDOUT:
                                    Toast.makeText(context, UserLoginHandler.errors.get(UserLoginHandler.CONN_TIMEDOUT), Toast.LENGTH_SHORT).show();
                                    break;
                                case UserLoginHandler.AUTH_DB_ERROR:
                                    Toast.makeText(context, UserLoginHandler.errors.get(UserLoginHandler.AUTH_DB_ERROR), Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        } else {
                            //TODO should go to another activity once sucessfully logged in
                            JsonToMap json = new JsonToMap((String) result);
                            HashMap resultMap = json.getMap();
                            Iterator<String> it = resultMap.keySet().iterator();
                            while (it.hasNext()) {
                                String key = it.next();
                                String value = (String)resultMap.get(key);
                                System.err.println(key+" : "+value);

                            }


                        }
                    }
                }
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
