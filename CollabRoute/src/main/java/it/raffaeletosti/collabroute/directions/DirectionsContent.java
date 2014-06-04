package it.raffaeletosti.collabroute.directions;

import android.graphics.Path;
import android.text.Layout;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by raffaele on 04/06/14.
 */
public class DirectionsContent {

    public static List<DirectionsItem> ITEMS = new ArrayList<DirectionsItem>();

    public static Map<String, DirectionsItem> ITEM_MAP = new HashMap<String, DirectionsItem>();

    public static void addItem(DirectionsItem item) {
        ITEM_MAP.put(item.id, item);
        ITEMS.add(item);
    }

    public static boolean isEmpty(){
        return ITEM_MAP.isEmpty();
    }

    public static void cleanList(){
        if(ITEM_MAP != null)
            ITEM_MAP.clear();
        if(ITEMS != null)
            ITEMS.clear();
    }


    public static class DirectionsItem {

        public String id;
        public String HTMLInstructions;
        public String duration;
        public LatLng endLocation;
        public LatLng startLocation;
        public String travelMode;
        public String distance;

        public DirectionsItem(String id, String HTMLInstructions, String duration, LatLng endLocation, LatLng startLocation, String travelMode, String distance) {
            this.id = id;
            this.HTMLInstructions = HTMLInstructions;
            this.duration = duration;
            this.endLocation = endLocation;
            this.startLocation = startLocation;
            this.travelMode = travelMode;
            this.distance = distance;
        }
    }
}
