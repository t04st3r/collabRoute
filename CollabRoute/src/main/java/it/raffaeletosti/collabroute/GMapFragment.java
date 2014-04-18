package it.raffaeletosti.collabroute;


import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.LocationListener;


public class GMapFragment extends Fragment implements android.location.LocationListener{

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected static Activity activity;
    String lat;
    String provider;
    String latitude, longitude;
    protected boolean gps_enabled, network_enabled;

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    }


    public static GMapFragment newInstance(Activity travelActivity) {
        GMapFragment fragment = new GMapFragment();
        activity = travelActivity;
        Bundle args = new Bundle();
        return fragment;
    }
    public GMapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gmap, container, false);
    }

    @Override
    public void onLocationChanged(Location location) {
        System.err.println("LATITUDE: "+location.getLatitude()+"  LONGITUDE: "+location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}
