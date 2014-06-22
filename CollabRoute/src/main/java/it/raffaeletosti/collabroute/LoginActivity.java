package it.raffaeletosti.collabroute;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import it.raffaeletosti.collabroute.connection.ConnectionHandler;
import it.raffaeletosti.collabroute.connection.EmailValidator;
import it.raffaeletosti.collabroute.connection.UserLoginHandler;
import it.raffaeletosti.collabroute.model.UserHandler;


public class LoginActivity extends Activity {

    private static UserHandler User = null;
    public final static String PARCELABLE_KEY = "it.raffaeletosti.collabroute.parcelable";

    private enum ResponseMSG {OK, AUTH_FAILED, USER_NOT_CONFIRMED, EMAIL_SEND_ERROR, EMAIL_NOT_FOUND, WRONG_CODE, CONFIRM_MAIL_ERROR, DATABASE_ERROR, CONN_TIMEDOUT, CONN_REFUSED, CONN_BAD_URL, CONN_GENERIC_IO_ERROR, CONN_GENERIC_ERROR}

    EditText mailField;
    EditText passField;
    Dialog confirmDialog;
    Dialog exitDialog;
    EditText codeField;
    EditText newPasswd;
    String code;
    AlertDialog recoveryDialog;
    Dialog codeRecoveryDialog;
    String eMailAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mailField = (EditText) findViewById(R.id.emailLogin);
        passField = (EditText) findViewById(R.id.passwordLogin);

        final Button loginButton = (Button) findViewById(R.id.buttonLogin);
        final Button registrationButton = (Button) findViewById(R.id.buttonSignIn);
        final Button recovery = (Button) findViewById(R.id.buttonRecovery);


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

        recovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRecoveryDialog();
            }
        });
    }

    private void createRecoveryDialog() {
        if (recoveryDialog == null) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getString(R.string.recovery_alert_title));
            alertDialogBuilder.setMessage(getString(R.string.recovery_alert_message));
            final EditText mailField = new EditText(this);
            alertDialogBuilder.setView(mailField);
            alertDialogBuilder.setPositiveButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    String mailFromTextView = mailField.getText().toString();
                    mailField.setText("");
                    if (!EmailValidator.validate(mailFromTextView)) {
                        showToastWrongEmail();
                    } else {
                        sendRecoveryRequest(mailFromTextView);
                    }
                }
            });
            alertDialogBuilder.setNegativeButton(getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    recoveryDialog.dismiss();
                }
            });
            recoveryDialog = alertDialogBuilder.create();
        }
        recoveryDialog.show();
    }

    private void showToastWrongEmail() {
        Toast.makeText(this, getString(R.string.recovery_alert_wrong_email), Toast.LENGTH_SHORT).show();
    }

    private void sendRecoveryRequest(String mail) {
        eMailAddress = mail;
        UserLoginHandler recoveryRequest = new UserLoginHandler(this, mail);
        recoveryRequest.execute("recovery");
        recoveryDialog.dismiss();
    }

    public void doLogin() {

        String mail = mailField.getText().toString();
        String passwd = passField.getText().toString();

        if (TextUtils.isEmpty(mailField.getText()) || TextUtils.isEmpty(passField.getText()) || !EmailValidator.validate(mail)) {
            Toast.makeText(LoginActivity.this, "Email or Password missing or incorrect", Toast.LENGTH_SHORT).show();
            return;
        }
        if (User == null)
            User = new UserHandler();
        User.setEMail(mail);
        User.setPassword(passwd);
        UserLoginHandler login = new UserLoginHandler(this, User); //extend AsyncTask and run with a separate thread
        login.execute("login"); //start the thread

    }

    public void checkCredentials(JSONObject response) {
        try {
            String resultString = response.getString("result");
            ResponseMSG responseEnum = ResponseMSG.valueOf(resultString);
            switch (responseEnum) {
                case AUTH_FAILED:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.AUTH_FAILED), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_REFUSED:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_REFUSED), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_BAD_URL:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_BAD_URL), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_IO_ERROR:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_GENERIC_IO_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_ERROR:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_GENERIC_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_TIMEDOUT:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_TIMEDOUT), Toast.LENGTH_SHORT).show();
                    return;
                case DATABASE_ERROR:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.DB_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case USER_NOT_CONFIRMED:
                    code = response.getString("code");
                    createConfirmDialog();
                    confirmDialog.show();
                    return;
                case OK:
                    User.setName(response.getString("name"));
                    User.setToken(response.getString("token"));
                    User.setId(response.getInt("id"));
                    Intent travelListIntent = new Intent(getApplication(), travelListActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(PARCELABLE_KEY, User);
                    travelListIntent.putExtras(bundle);
                    startActivity(travelListIntent);
                    Toast.makeText(this, String.format(ConnectionHandler.errors.get(UserLoginHandler.OK), User.getName()), Toast.LENGTH_SHORT).show();
                    finish();
            }
        } catch (JSONException e) {
            System.err.println(e);
        }
    }

    public void goToRegistration() {
        Intent registrationIntent = new Intent(getApplication(), RegistrationActivity.class);
        startActivity(registrationIntent);
    }

    public void createConfirmDialog() {
        confirmDialog = new Dialog(this);
        confirmDialog.setContentView(R.layout.confirm_dialog);
        confirmDialog.setTitle(this.getString(R.string.dialog_confirm_title));

        final TextView dialogMessage = (TextView) confirmDialog.findViewById(R.id.confirm_text);
        final Button sendCode = (Button) confirmDialog.findViewById(R.id.dialogSedCode);
        final Button checkMail = (Button) confirmDialog.findViewById(R.id.dialogCheckMail);
        codeField = (EditText) confirmDialog.findViewById(R.id.dialogVeriCode);

        String dialogText = String.format(this.getString(R.string.registration_email_sent), User.getEMail());
        dialogMessage.setText(dialogText);

        sendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCode(codeField.getText().toString());
            }
        });

        checkMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkMail();
            }
        });
    }

    public void createExitDialog() {
        exitDialog = new Dialog(this);
        exitDialog.setContentView(R.layout.exit_dialog);
        exitDialog.setTitle(getString(R.string.exit_title));
        final Button exitOk = (Button) exitDialog.findViewById(R.id.exitOk);
        final Button exitCancel = (Button) exitDialog.findViewById(R.id.exitCancel);
        exitOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeApplication();
            }
        });
        exitCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitDialog.dismiss();
            }
        });
    }

    private void closeApplication() {
        exitDialog.dismiss();
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
        finish();
        System.exit(0);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void checkCode(String codeWritten) {
        if (!code.equals(codeWritten)) {
            Toast.makeText(LoginActivity.this, this.getString(R.string.registration_wrong_code), Toast.LENGTH_SHORT).show();
            return;
        }
        confirmDialog.dismiss();
        UserLoginHandler login = new UserLoginHandler(this, User);
        login.execute("confirm");//extend AsyncTask and run with a separate thread
    }

    @Override
    public void onBackPressed() {
        createExitDialog();
        exitDialog.show();
    }

    public void checkMail() {
        final Intent intent = getPackageManager().getLaunchIntentForPackage("com.android.email");
        if (intent != null) {
            startActivity(intent);
        } else {
            Toast.makeText(LoginActivity.this, this.getString(R.string.registration_mail_not_configured), Toast.LENGTH_SHORT).show();
        }
    }

    public void confirmationResponse(JSONObject response) {
        try {
            String resultString = response.getString("result");
            ResponseMSG responseEnum = ResponseMSG.valueOf(resultString);
            switch (responseEnum) {
                case CONN_REFUSED:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_REFUSED), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_BAD_URL:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_BAD_URL), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_IO_ERROR:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_GENERIC_IO_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_ERROR:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_GENERIC_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_TIMEDOUT:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_TIMEDOUT), Toast.LENGTH_SHORT).show();
                    return;
                case DATABASE_ERROR:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.DB_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case EMAIL_NOT_FOUND:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(UserLoginHandler.EMAIL_NOT_FOUND), Toast.LENGTH_SHORT).show();
                    return;
                case OK:
                    User.setId(Integer.parseInt(response.getString("id")));
                    User.setEMail(response.getString("mail"));
                    User.setName(response.getString("name"));
                    Toast.makeText(LoginActivity.this, String.format(ConnectionHandler.errors.get(ConnectionHandler.OK), User.getName()), Toast.LENGTH_SHORT).show();
                    mailField.setText(User.getEMail());
            }
        } catch (JSONException e) {
            System.err.println(e);
        }
    }

    public void handleResponseRecoveryRequest(JSONObject response) {
        String resultString = null;
        try {
            resultString = response.getString("result");
            ResponseMSG responseEnum = ResponseMSG.valueOf(resultString);
            switch (responseEnum) {
                case CONN_REFUSED:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_REFUSED), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_BAD_URL:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_BAD_URL), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_IO_ERROR:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_GENERIC_IO_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_ERROR:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_GENERIC_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_TIMEDOUT:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_TIMEDOUT), Toast.LENGTH_SHORT).show();
                    return;
                case DATABASE_ERROR:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.DB_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case EMAIL_NOT_FOUND:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(UserLoginHandler.EMAIL_NOT_FOUND), Toast.LENGTH_SHORT).show();
                    return;
                case EMAIL_SEND_ERROR:
                    Toast.makeText(LoginActivity.this, getString(R.string.recovery_email_send_error), Toast.LENGTH_SHORT).show();
                    return;
                case OK:
                    createCodeVerificationRecoveryDialog();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void createCodeVerificationRecoveryDialog() {
        if (codeRecoveryDialog == null) {
            codeRecoveryDialog = new Dialog(this);
            codeRecoveryDialog.setContentView(R.layout.change_passwd_dialog);
            codeRecoveryDialog.setTitle(getString(R.string.recovery_alert_dialog_code_title));
            TextView message = (TextView) codeRecoveryDialog.findViewById(R.id.recovery_dialog_message);
            codeField = (EditText) codeRecoveryDialog.findViewById(R.id.recovery_dialog_code);
            newPasswd = (EditText) codeRecoveryDialog.findViewById(R.id.recovery_dialog_passwd);
            Button updatePass = (Button) codeRecoveryDialog.findViewById(R.id.send_new_password);
            Button checkMail = (Button) codeRecoveryDialog.findViewById(R.id.check_mail);
            message.setText(String.format(getString(R.string.recovery_email_sent), eMailAddress));
            checkMail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkMail();
                }
            });
            updatePass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String passFromTextView = (newPasswd.getText()).toString();
                    String codeFromTextView = (codeField.getText()).toString();
                    if(passFromTextView.equals("") || codeFromTextView.equals("")){
                        Toast.makeText(LoginActivity.this, getString(R.string.recovery_dialog_empty_pass_or_code), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    newPasswd.setText("");
                    codeField.setText("");
                    int code = Integer.parseInt(codeFromTextView);
                    UserHandler user = new UserHandler();
                    user.setPassword(passFromTextView);
                    String md5Pass = user.getPassword();
                    UserLoginHandler sendPassAndCode = new UserLoginHandler(LoginActivity.this, code, md5Pass, eMailAddress);
                    sendPassAndCode.execute("sendPass");
                }
            });
        }
        codeRecoveryDialog.show();
    }

    public void handleRecoveryPasswordResponse(JSONObject response) {
        String resultString = null;
        try {
            resultString = response.getString("result");
            ResponseMSG responseEnum = ResponseMSG.valueOf(resultString);
            switch (responseEnum) {
                case CONN_REFUSED:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_REFUSED), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_BAD_URL:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_BAD_URL), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_IO_ERROR:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_GENERIC_IO_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_ERROR:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_GENERIC_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_TIMEDOUT:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_TIMEDOUT), Toast.LENGTH_SHORT).show();
                    return;
                case DATABASE_ERROR:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(ConnectionHandler.DB_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case EMAIL_NOT_FOUND:
                    Toast.makeText(LoginActivity.this, ConnectionHandler.errors.get(UserLoginHandler.EMAIL_NOT_FOUND), Toast.LENGTH_SHORT).show();
                    return;
                case WRONG_CODE:
                    Toast.makeText(LoginActivity.this, getString(R.string.recovery_dialog_wrong_code), Toast.LENGTH_SHORT).show();
                    return;
                case OK:
                    codeRecoveryDialog.dismiss();
                    Toast.makeText(LoginActivity.this, getString(R.string.recovery_dialog_password_changed), Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}