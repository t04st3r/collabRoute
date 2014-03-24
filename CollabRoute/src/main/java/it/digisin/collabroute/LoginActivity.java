package it.digisin.collabroute;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;


public class LoginActivity extends Activity {

    private static UserHandler User = null;

    EditText mailField;
    EditText passField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mailField = (EditText) findViewById(R.id.emailLogin);
        passField = (EditText) findViewById(R.id.passwordLogin);

        final Button loginButton = (Button) findViewById(R.id.buttonLogin);
        final Button registrationButton = (Button) findViewById(R.id.buttonSignIn);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
            }
        });

        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegistration();
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

    public void doLogin() {

        final Editable mailEdit = mailField.getText();
        final Editable passEdit = passField.getText();
        String mail = mailEdit.toString();
        String passwd = passEdit.toString();

        if (TextUtils.isEmpty(mailEdit) || TextUtils.isEmpty(passEdit) || !EmailValidator.validate(mail)) {
            Toast.makeText(LoginActivity.this, "Email or Password missing or incorrect", Toast.LENGTH_SHORT).show();
            return;
        }
        if (User == null) {
            User = UserHandler.create(mail, passwd);
        } else {
            User.setEMail(mail);
            User.setPassword(passwd);
        }
        UserLoginHandler login = new UserLoginHandler(User, LoginActivity.this); //extend AsyncTask and run with a separate thread
        login.execute(); //start the thread
        Object result = null;
        try {
            result = login.get();
        } catch (InterruptedException e) {
            System.err.println(e);
        } catch (ExecutionException e) {
            System.err.println(e);
        }
        if (result instanceof Integer) {
            int resultInt = ((Integer) result).intValue();
            switch (resultInt) {
                case UserLoginHandler.AUTH_FAILED:
                    Toast.makeText(LoginActivity.this, UserLoginHandler.errors.get(UserLoginHandler.AUTH_FAILED), Toast.LENGTH_SHORT).show();
                    return;
                case UserLoginHandler.CONN_REFUSED:
                    Toast.makeText(LoginActivity.this, UserLoginHandler.errors.get(UserLoginHandler.CONN_REFUSED), Toast.LENGTH_SHORT).show();
                    return;
                case UserLoginHandler.CONN_BAD_URL:
                    Toast.makeText(LoginActivity.this, UserLoginHandler.errors.get(UserLoginHandler.CONN_BAD_URL), Toast.LENGTH_SHORT).show();
                    return;
                case UserLoginHandler.CONN_GENERIC_IO_ERROR:
                    Toast.makeText(LoginActivity.this, UserLoginHandler.errors.get(UserLoginHandler.CONN_GENERIC_IO_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case UserLoginHandler.CONN_GENERIC_ERROR:
                    Toast.makeText(LoginActivity.this, UserLoginHandler.errors.get(UserLoginHandler.CONN_GENERIC_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case UserLoginHandler.CONN_TIMEDOUT:
                    Toast.makeText(LoginActivity.this, UserLoginHandler.errors.get(UserLoginHandler.CONN_TIMEDOUT), Toast.LENGTH_SHORT).show();
                    return;
                case UserLoginHandler.AUTH_DB_ERROR:
                    Toast.makeText(LoginActivity.this, UserLoginHandler.errors.get(UserLoginHandler.AUTH_DB_ERROR), Toast.LENGTH_SHORT).show();
                    return;
            }
        }

        //TODO should go to another activity once sucessfully logged in and update User data

        try {
            JSONObject response = new JSONObject((String) result);
            User.setName(response.getString("name"));
            User.setToken(response.getString("token"));
            User.setId(response.getInt("id"));
            System.err.println(User.getId() + " " + User.getName() + " " + User.getToken());
        } catch (JSONException e) {
            System.err.println(e);
        }
    }


    public void goToRegistration() {
        Intent registrationIntent = new Intent(getApplication(), RegistrationActivity.class);
        startActivity(registrationIntent);
    }
}
