package it.digisin.collabroute;

import android.app.Activity;
import android.util.Log;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

/**
 * Created by raffaele on 12/03/14.
 */
public class UserLoginHandler {
    private UserHandler user;
    private String serverUrl;
    private int serverPort;

    public UserLoginHandler(UserHandler user, String serverUrl, int serverPort) {
        this.user = user;
        this.serverUrl = serverUrl;
        this.serverPort = serverPort;
    }

    public boolean logIn(){
      try {
            URL url = new URL("https://"+serverUrl+":"+serverPort+"/auth/"+user.getEMail()+"/"+user.getPassword());
            HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
            String response = con.getResponseMessage();
            Log.e(LoginActivity.TAG_LOG , response);
      } catch (MalformedURLException e) {
            Log.e(LoginActivity.TAG_LOG, e.getMessage());
      } catch (IOException e) {
          Log.e(LoginActivity.TAG_LOG, e.getMessage());
      } catch (Exception e){
          e.printStackTrace();
      }
      return true;
    }
}
