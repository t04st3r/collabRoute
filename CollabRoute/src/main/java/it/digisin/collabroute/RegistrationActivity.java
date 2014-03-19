package it.digisin.collabroute;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;


public class RegistrationActivity extends Activity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        final EditText mailField = (EditText) findViewById(R.id.emailReg);
        final Editable mailEdit =  mailField.getText();
        final EditText passField = (EditText) findViewById(R.id.passReg);
        final Editable passEdit = passField.getText();
        final EditText userField = (EditText) findViewById(R.id.userReg);
        final Editable userEdit = passField.getText();
        final Button signInButton = (Button) findViewById(R.id.buttonSignIn);
        final Button checkMailButton = (Button) findViewById(R.id.buttonCheckMail);
        final Button completeReg = (Button) findViewById(R.id.buttonCompleteReg);

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

    public void setSignInButton(Button signInButton, final Editable mailEdit, final Editable passEdit, final Editable userEdit){
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = mailEdit.toString();
                String passwd = passEdit.toString();
                String user = userEdit.toString();
                Context context = getApplication();
                EmailValidator validator = new EmailValidator();
                if (mail.equals("") || passwd.equals("") || user.equals("")|| !validator.validate(mail)) {
                    Toast.makeText(context, "Email, Username or Password missing or incorrect", Toast.LENGTH_SHORT).show();
                } else {

                    UserRegistrationHandler registration = new UserRegistrationHandler(getApplicationContext()); //extend AsyncTask and run with a separate thread
                    registration.execute(); //start the thread
                    Object result = null;
                    try {
                        result = registration.get();
                    } catch (InterruptedException e) {
                        System.err.println(e);
                    } catch (ExecutionException e) {
                        System.err.println(e);
                    }
                    if(result != null) {
                        //TODO handle the code confirmation process and get back to the login Activity
                    }
                }
            }
        });
    }

}
