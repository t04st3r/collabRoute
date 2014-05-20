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
    protected double latitude = 0;
    protected double longitude = 0;
    protected String address = "unknown";

    public User(){};

    public User(int id, String name, String eMail, double latitude, double longitude, String address){
        this.id = id;
        this.name = name;
        this.eMail = eMail;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEMail() {
        return eMail;
    }

    public void setEMail(String eMail) {
        this.eMail = eMail;
    }

    public int getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
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

    public boolean isConnected(){
        return latitude != 0.0d && longitude != 0.0d;
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
