package it.digisin.collabroute;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;


public class RegistrationActivity extends Activity {

    private int regCode;
    private Editable mail;
    private Editable pass;
    private Editable user;
    private Editable code;
    private EditText mailText;
    private EditText passText;
    private EditText userText;
    private EditText codeText;
    private Button signIn;
    private Button mailCheck;
    private Button completeReg;
    private Context context;
    Resources res;
    private UserHandler newbie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        //initialize Registration Activity components
        mailText = (EditText) findViewById(R.id.emailReg);
        mail = mailText.getText();
        passText = (EditText) findViewById(R.id.passReg);
        pass = passText.getText();
        userText = (EditText) findViewById(R.id.userReg);
        user = userText.getText();
        codeText = (EditText) findViewById(R.id.veriCode);
        codeText.setFocusable(false); //disable verification field
        code = codeText.getText();
        signIn = (Button) findViewById(R.id.buttonReg);
        mailCheck = (Button) findViewById(R.id.buttonCheckMail);
        completeReg = (Button) findViewById(R.id.buttonCompleteReg);
        res = getResources();
        context = getApplication();

        setSignInButton();
        setConfirmButton();


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

    public void setConfirmButton() {
        completeReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int codeInserted = 0;
                boolean isNumeric = false;
                try {
                    codeInserted = Integer.parseInt(code.toString());
                    isNumeric = true;
                } catch (NumberFormatException e) {
                    System.err.println(e);
                }
                if (!isNumeric) {
                    Toast.makeText(context, res.getString(R.string.numeric_exception), Toast.LENGTH_SHORT).show();
                } else {
                    UserRegistrationHandler confirm = new UserRegistrationHandler(context, newbie);
                    if (codeInserted != regCode) {
                        Toast.makeText(context, res.getString(R.string.registration_wrong_code), Toast.LENGTH_SHORT).show();
                    } else {
                        confirm.execute("confirm");
                        Object result = null;
                        try {
                            result = confirm.get();
                        } catch (InterruptedException e) {
                            System.err.println(e);
                        } catch (ExecutionException e) {
                            System.err.println(e);
                        }
                        int resultInt = ((Integer) result).intValue();
                        switch (resultInt) {
                            case UserRegistrationHandler.EMAIL_NOT_FOUND:
                                Toast.makeText(context, UserRegistrationHandler.errors.get(UserRegistrationHandler.EMAIL_NOT_FOUND), Toast.LENGTH_SHORT).show();
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
                            case UserRegistrationHandler.OK:
                                Toast.makeText(context, UserRegistrationHandler.errors.get(UserRegistrationHandler.OK+newbie.getName()), Toast.LENGTH_LONG).show();
                        }
                        comeBack();
                    }
                }
            }
        });
    }

    public void setSignInButton() {
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mailString = mail.toString();
                String passString = pass.toString();
                String userString = user.toString();
                Context context = getApplication();
                if (!checkRegField(mailString, passString, userString)) {
                    Toast.makeText(context, res.getString(R.string.registration_user_mail_name_error), Toast.LENGTH_SHORT).show();
                } else {
                    newbie = new UserHandler(mailString, passString);
                    newbie.setName(userString);
                    UserRegistrationHandler connection = new UserRegistrationHandler(getApplicationContext(), newbie); //extend AsyncTask and run with a separate thread
                    connection.execute("registration"); //start the thread
                    Object result = null;
                    try {
                        result = connection.get();
                    } catch (InterruptedException e) {
                        System.err.println(e);
                    } catch (ExecutionException e) {
                        System.err.println(e);
                    }
                    checkResponse(result, newbie);
                }
            }
        });
    }


    private boolean checkRegField(String mail, String pass, String name) {
        EmailValidator validator = new EmailValidator();
        if (mail.equals("") || pass.equals("") || name.equals("") || !validator.validate(mail))
            return false;
        return true;
    }

    private void checkResponse(Object result, UserHandler newbie) {
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
                String codeString = response.getString("code");
                int code = Integer.parseInt(codeString);
                System.err.println("code: " + code);
                regCode = code;
                enableButtons();

            } catch (JSONException e) {
                System.err.println(e);
            }
        }
    }

    private void enableButtons() {
        signIn.setEnabled(false);
        mailCheck.setEnabled(true);
        completeReg.setEnabled(true);
        mailText.setText("");
        userText.setText("");
        passText.setText("");
        mailText.setFocusable(false);
        userText.setFocusable(false);
        passText.setFocusable(false);
        codeText.setEnabled(true);
        codeText.setFocusableInTouchMode(true);
        codeText.setFocusable(true);
    }

    public void comeBack() {
        final Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, RESULT_OK);
        finish();
    }
}