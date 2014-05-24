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

    public static boolean nobodySelected() {
        if (!ITEM_MAP.isEmpty()) {
            for (RoutesContent.RoutesItem current : ITEMS) {
                if (current.isSelected)
                    return false;

            }
        }
        return true;
    }

    public static boolean isEmpty(){
        return ITEM_MAP.isEmpty();
    }

    public static void selectCurrent(String id){
        if(!ITEM_MAP.isEmpty()){
            for(RoutesItem current : ITEMS){
                if(!current.id.equals(id)){
                    current.isSelected = false;
                }else{
                    current.isSelected = true;
                }
            }
            for(String current : ITEM_MAP.keySet()){
                RoutesItem item = ITEM_MAP.get(current);
                if(!current.equals(id)){
                    item.isSelected = false;
                }else{
                    item.isSelected = true;
                }
            }
        }
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
        public boolean isSelected;


        public RoutesItem(String id, String address, String creator, String latitude, String longitude, boolean isSelected){
            this.id = id;
            this.address = address;
            this.creator = creator;
            this.latitude = latitude;
            this.longitude = longitude;
            this.isSelected = isSelected;
        }
    }
}
