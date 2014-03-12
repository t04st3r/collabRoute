package it.digisin.collabroute;

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
        
    }
}
