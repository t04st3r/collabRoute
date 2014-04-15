package it.raffaeletosti.collabroute.travels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 */
public class TravelContent {

    /**
     * An array of sample (Travel) items.
     */
    public static List<TravelItem> ITEMS = new ArrayList<TravelItem>();

    /**
     * A map of sample (Travel) items, by ID.
     */
    public static Map<String, TravelItem> ITEM_MAP = new HashMap<String, TravelItem>();


    public static void addItem(TravelItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static void cleanList(){
        if(ITEM_MAP != null)
            ITEM_MAP.clear();
        if(ITEMS != null)
            ITEMS.clear();
    }
    /**
     * A Travel item representing a piece of content.
     */
    public static class TravelItem {
        public String id;
        public String name;
        public String description;


        public TravelItem(String id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;


        }

        @Override
        public String toString() {
            return name;

        }
    }
}
