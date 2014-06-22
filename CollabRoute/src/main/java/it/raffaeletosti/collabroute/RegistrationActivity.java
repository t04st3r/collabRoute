package it.raffaeletosti.collabroute;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import it.raffaeletosti.collabroute.connection.ConnectionHandler;
import it.raffaeletosti.collabroute.connection.EmailValidator;
import it.raffaeletosti.collabroute.connection.UserRegistrationHandler;
import it.raffaeletosti.collabroute.model.UserHandler;


public class RegistrationActivity extends Activity {


    protected UserRegistrationHandler connection;

    private int regCode;
    private EditText mailText;
    private EditText passText;
    private EditText userText;
    private EditText codeText;
    private Button signIn;
    private Button mailCheck;
    private Button completeReg;
    private UserHandler newbie;

    private enum ResponseMSG {OK, EMAIL_SEND_ERROR, DATABASE_ERROR, EMAIL_EXISTS_ERROR, EMAIL_NOT_FOUND, CONN_TIMEDOUT, CONN_REFUSED, CONN_BAD_URL, CONN_GENERIC_IO_ERROR, CONN_GENERIC_ERROR}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        //initialize Registration Activity components
        mailText = (EditText) findViewById(R.id.emailReg);
        passText = (EditText) findViewById(R.id.passReg);
        userText = (EditText) findViewById(R.id.userReg);
        codeText = (EditText) findViewById(R.id.veriCode);
        codeText.setFocusable(false); //disable verification field
        signIn = (Button) findViewById(R.id.buttonReg);
        mailCheck = (Button) findViewById(R.id.buttonCheckMail);
        completeReg = (Button) findViewById(R.id.buttonCompleteReg);

        signIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        completeReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completeRegistration();
            }
        });

        mailCheck.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                checkMail();
            }
        });
    }

    public void completeRegistration() {
        String code = codeText.getText().toString();
        int codeInserted = 0;
        boolean isNumeric = false;
        try {
            codeInserted = Integer.parseInt(code);
            isNumeric = true;
        } catch (NumberFormatException e) {
            System.err.println(e);
        }
        if (!isNumeric) {
            Toast.makeText(RegistrationActivity.this, this.getString(R.string.numeric_exception), Toast.LENGTH_SHORT).show();
            return;
        }
        if (codeInserted != regCode) {
            Toast.makeText(RegistrationActivity.this, this.getString(R.string.registration_wrong_code), Toast.LENGTH_SHORT).show();
            return;
        }
        connection = new UserRegistrationHandler(this, newbie);
        connection.execute("confirm"); //run AsyncTask thread
    }

    public void checkConfirmation(Object result) {
        try {
            String resultString = ((JSONObject) result).getString("result");
            ResponseMSG response = ResponseMSG.valueOf(resultString);
            switch (response) {
                case EMAIL_NOT_FOUND:
                    Toast.makeText(RegistrationActivity.this, ConnectionHandler.errors.get(UserRegistrationHandler.EMAIL_NOT_FOUND), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_REFUSED:
                    Toast.makeText(RegistrationActivity.this, ConnectionHandler.errors.get(UserRegistrationHandler.CONN_REFUSED), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_BAD_URL:
                    Toast.makeText(RegistrationActivity.this, ConnectionHandler.errors.get(UserRegistrationHandler.CONN_BAD_URL), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_IO_ERROR:
                    Toast.makeText(RegistrationActivity.this, ConnectionHandler.errors.get(UserRegistrationHandler.CONN_GENERIC_IO_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_ERROR:
                    Toast.makeText(RegistrationActivity.this, ConnectionHandler.errors.get(UserRegistrationHandler.CONN_GENERIC_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_TIMEDOUT:
                    Toast.makeText(RegistrationActivity.this, ConnectionHandler.errors.get(UserRegistrationHandler.CONN_TIMEDOUT), Toast.LENGTH_SHORT).show();
                    return;
                case DATABASE_ERROR:
                    Toast.makeText(RegistrationActivity.this, ConnectionHandler.errors.get(UserRegistrationHandler.DB_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case OK:
                    Toast.makeText(RegistrationActivity.this, String.format(ConnectionHandler.errors.get(UserRegistrationHandler.OK), newbie.getName()), Toast.LENGTH_LONG).show();
            }
            comeBack();
        } catch (JSONException e) {
            System.err.println(e);
        }
    }

    public void signIn() {
        Editable mail = mailText.getText();
        Editable pass = passText.getText();
        Editable user = userText.getText();
        String mailString = mail.toString();
        String passString = pass.toString();
        if (TextUtils.isEmpty(mail) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(user) || !EmailValidator.validate(mailString)) {
            Toast.makeText(RegistrationActivity.this, this.getString(R.string.registration_user_mail_name_error), Toast.LENGTH_SHORT).show();
            return;
        }
        String userString = user.toString().substring(0,1).toUpperCase()+user.toString().substring(1);
        newbie = new UserHandler();
        newbie.setEMail(mailString);
        newbie.setPassword(passString);
        newbie.setName(userString);
        connection = new UserRegistrationHandler(this, newbie);
        connection.execute("registration"); //start the thread
    }

    public void checkResponse(Object result) {
        try {
            String resultString = ((JSONObject) result).getString("result");
            ResponseMSG response = ResponseMSG.valueOf(resultString);
            switch (response) {
                case EMAIL_EXISTS_ERROR:
                    Toast.makeText(RegistrationActivity.this, ConnectionHandler.errors.get(UserRegistrationHandler.EMAIL_EXISTS_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case EMAIL_SEND_ERROR:
                    Toast.makeText(RegistrationActivity.this, ConnectionHandler.errors.get(UserRegistrationHandler.EMAIL_SEND_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_REFUSED:
                    Toast.makeText(RegistrationActivity.this, ConnectionHandler.errors.get(UserRegistrationHandler.CONN_REFUSED), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_BAD_URL:
                    Toast.makeText(RegistrationActivity.this, ConnectionHandler.errors.get(UserRegistrationHandler.CONN_BAD_URL), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_IO_ERROR:
                    Toast.makeText(RegistrationActivity.this, ConnectionHandler.errors.get(UserRegistrationHandler.CONN_GENERIC_IO_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_ERROR:
                    Toast.makeText(RegistrationActivity.this, ConnectionHandler.errors.get(UserRegistrationHandler.CONN_GENERIC_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_TIMEDOUT:
                    Toast.makeText(RegistrationActivity.this, ConnectionHandler.errors.get(UserRegistrationHandler.CONN_TIMEDOUT), Toast.LENGTH_SHORT).show();
                    return;
                case DATABASE_ERROR:
                    Toast.makeText(RegistrationActivity.this, ConnectionHandler.errors.get(UserRegistrationHandler.DB_ERROR), Toast.LENGTH_SHORT).show();
                    return;
            }
            String successText = String.format(this.getString(R.string.registration_email_sent), newbie.getEMail());
            Toast.makeText(RegistrationActivity.this, successText, Toast.LENGTH_LONG).show();
            String codeString = ((JSONObject) result).getString("code");
            int code = Integer.parseInt(codeString);
            regCode = code;
            enableButtons();

        } catch (JSONException e) {
            e.printStackTrace();
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
    public void checkMail() {
        final Intent intent = getPackageManager().getLaunchIntentForPackage("com.android.email");
        if(intent != null) {
            startActivity(intent);
        }else{
            Toast.makeText(RegistrationActivity.this, this.getString(R.string.registration_mail_not_configured), Toast.LENGTH_SHORT).show();
        }
    }
}