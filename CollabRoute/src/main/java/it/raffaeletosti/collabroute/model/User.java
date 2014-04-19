package it.raffaeletosti.collabroute.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by raffaele on 27/03/14.
 */
public class User implements Parcelable{
    protected String eMail;
    protected int id;
    protected String name;

    public String getEMail() {
        return eMail;
    }

    public void setEMail(String eMail) {
        this.eMail = eMail;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(eMail);
        parcel.writeString(name);
        parcel.writeInt(id);
    }
}
