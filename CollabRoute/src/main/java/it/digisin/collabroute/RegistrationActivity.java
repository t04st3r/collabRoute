package it.digisin.collabroute;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
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


public class RegistrationActivity extends Activity {





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        final EditText mailField = (EditText) findViewById(R.id.emailReg);
        final Editable mailEdit = mailField.getText();
        final EditText passField = (EditText) findViewById(R.id.passReg);
        final Editable passEdit = passField.getText();
        final EditText userField = (EditText) findViewById(R.id.userReg);
        final Editable userEdit = userField.getText();
        final EditText codeField = (EditText) findViewById(R.id.veriCode);
        final Editable codeEdit = codeField.getText();
        codeField.setKeyListener(null); //disable
        final Button signInButton = (Button) findViewById(R.id.buttonReg);
        final Button checkMailButton = (Button) findViewById(R.id.buttonCheckMail);
        final Button completeReg = (Button) findViewById(R.id.buttonCompleteReg);
        final Resources res = getResources();
        setSignInButton(signInButton, mailEdit, passEdit, userEdit, res);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.registration, menu);
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

    public void setSignInButton(Button signInButton, final Editable mailEdit, final Editable passEdit, final Editable userEdit, final Resources res) {
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = mailEdit.toString();
                String passwd = passEdit.toString();
                String user = userEdit.toString();
                Context context = getApplication();
                EmailValidator validator = new EmailValidator();
                if (mail.equals("") || passwd.equals("") || user.equals("") || !validator.validate(mail)) {
                    Toast.makeText(context, "Email, Username or Password missing or incorrect", Toast.LENGTH_SHORT).show();
                } else {
                    UserHandler newbie = new UserHandler(mail, passwd);
                    newbie.setName(user);
                    UserRegistrationHandler registration = new UserRegistrationHandler(getApplicationContext(), newbie); //extend AsyncTask and run with a separate thread
                    registration.execute(); //start the thread
                    Object result = null;
                    try {
                        result = registration.get();
                    } catch (InterruptedException e) {
                        System.err.println(e);
                    } catch (ExecutionException e) {
                        System.err.println(e);
                    }
                    if (result instanceof Integer) {
                        int resultInt = ((Integer) result).intValue();
                        switch (resultInt) {
                            case UserRegistrationHandler.EMAIL_EXISTS_ERROR:
                                Toast.makeText(context, UserRegistrationHandler.errors.get(UserRegistrationHandler.EMAIL_EXISTS_ERROR), Toast.LENGTH_SHORT).show();
                                break;
                            case UserRegistrationHandler.EMAIL_SEND_ERROR:
                                Toast.makeText(context, UserRegistrationHandler.errors.get(UserRegistrationHandler.EMAIL_SEND_ERROR), Toast.LENGTH_SHORT).show();
                                break;
                            case UserRegistrationHandler.CONN_REFUSED:
                                Toast.makeText(context, UserRegistrationHandler.errors.get(UserRegistrationHandler.CONN_REFUSED), Toast.LENGTH_SHORT).show();
                                break;
                            case UserRegistrationHandler.CONN_BAD_URL:
                                Toast.makeText(context, UserRegistrationHandler.errors.get(UserRegistrationHandler.CONN_BAD_URL), Toast.LENGTH_SHORT).show();
                                break;
                            case UserRegistrationHandler.CONN_GENERIC_IO_ERROR:
                                Toast.makeText(context, UserRegistrationHandler.errors.get(UserRegistrationHandler.CONN_GENERIC_IO_ERROR), Toast.LENGTH_SHORT).show();
                                break;
                            case UserRegistrationHandler.CONN_GENERIC_ERROR:
                                Toast.makeText(context, UserRegistrationHandler.errors.get(UserRegistrationHandler.CONN_GENERIC_ERROR), Toast.LENGTH_SHORT).show();
                                break;
                            case UserRegistrationHandler.CONN_TIMEDOUT:
                                Toast.makeText(context, UserRegistrationHandler.errors.get(UserRegistrationHandler.CONN_TIMEDOUT), Toast.LENGTH_SHORT).show();
                                break;
                            case UserRegistrationHandler.DB_ERROR:
                                Toast.makeText(context, UserRegistrationHandler.errors.get(UserRegistrationHandler.DB_ERROR), Toast.LENGTH_SHORT).show();
                                break;
                        }
                    } else {
                        try {

                            String successText = String.format(res.getString(R.string.registration_email_sent), newbie.getName());
                            Toast.makeText(context, successText, Toast.LENGTH_LONG).show();
                            JSONObject response = new JSONObject((String) result);
                            String codeString = (String) response.getString("code");
                            int code = Integer.parseInt(codeString);

                        } catch (JSONException e) {
                            System.err.println(e);
                        }

                    }

                }
            }
        });
    }

}
