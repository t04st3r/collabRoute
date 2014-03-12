package it.digisin.collabroute;

import java.security.MessageDigest;

/**
 * Created by raffaele on 12/03/14.
 */
public class UserHandler {
    private String eMail;
    private String password;
    private String token;
    private String name;
    private int id;

    public UserHandler(String eMail, String password) {
        this.eMail = eMail;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++)
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            this.password = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getEMail() {
        return eMail;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setEMail(String eMail) {
        this.eMail = eMail;
    }

    public void setPassword(String password) {
        if (password != null) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(password.getBytes());
                byte[] bytes = md.digest();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < bytes.length; i++)
                    sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
                this.password = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }
}
