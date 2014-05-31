package it.raffaeletosti.collabroute.routes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by raffaele on 22/05/14.
 */
public class RoutesContent {

    public static List<RoutesItem> ITEMS = new ArrayList<RoutesItem>();

    public static Map<String, RoutesItem> ITEM_MAP = new HashMap<String, RoutesItem>();

    public static void addItem(RoutesItem item) {
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

    public static class RoutesItem {
        public String id;
        public String address;
        public String creator;
        public String latitude;
        public String longitude;


        public RoutesItem(String id, String address, String creator, String latitude, String longitude){
            this.id = id;
            this.address = address;
            this.creator = creator;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
