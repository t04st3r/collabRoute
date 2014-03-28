package it.digisin.collabroute.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.security.MessageDigest;

/**
 * Created by raffaele on 12/03/14.
 */
public class UserHandler extends User implements Parcelable{ //useful for passing UserHandler obj through Activities


    private String password;
    private String token;


    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
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
            } catch (Exception e) {System.err.println(e);}
        }
    }

    public void setToken(String token) {
        this.token = token;
    }


    public static final Parcelable.Creator<UserHandler> CREATOR = new Creator<UserHandler>(){

        @Override
        public UserHandler createFromParcel(Parcel source) {
            UserHandler user = new UserHandler();
            user.eMail = source.readString();
            user.password = source.readString();
            user.name = source.readString();
            user.token = source.readString();
            user.id  = source.readInt();
        return user;
        }

        @Override
        public UserHandler[] newArray(int size) {
            return new UserHandler[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(eMail);
        parcel.writeString(password);
        parcel.writeString(name);
        parcel.writeString(token);
        parcel.writeInt(id);
    }
}