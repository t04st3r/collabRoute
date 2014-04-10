package it.digisin.collabroute.users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by raffaele on 10/04/14.
 */
public class UserContent {


    /**
     * An array of sample (Travel) items.
     */
    public static List<UserItem> ITEMS = new ArrayList<UserItem>();

    /**
     * A map of sample (Travel) items, by ID.
     */
    public static Map<String, UserItem> ITEM_MAP = new HashMap<String, UserItem>();


    public static void addItem(UserItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static void deleteItem(String id) {
        if (!ITEM_MAP.isEmpty()) {
            UserItem toDelete = ITEM_MAP.get(id);
            ITEMS.remove(toDelete);
            ITEM_MAP.remove(id);
        }
    }
    public static boolean isInTheList(String id){
        if(!ITEM_MAP.isEmpty()){
            return ITEM_MAP.containsKey(id);
        }
        return false;
    }

    public static void cleanList() {
        if (ITEM_MAP != null)
            ITEM_MAP.clear();
        if (ITEMS != null)
            ITEMS.clear();
    }

    public static UserItem[] getSelected() {
        if (ITEM_MAP != null) {
            Iterator<String> iterator = ITEM_MAP.keySet().iterator();
            UserItem[] selectedId = new UserItem[ITEM_MAP.size()];
            int index = 0;
            while (iterator.hasNext()) {
                String current = iterator.next();
                if(ITEM_MAP.get(current).selected)
                    selectedId[index++] = ITEM_MAP.get(current);
            }
            if(index == 0){
                return null;
            }
            UserItem[] toReturn = new UserItem[index];
            for(int i = 0; i < index; i++){
                toReturn[i] = selectedId[i];
            }
            return toReturn;
        }
        return null;
    }
    /**
     * A Travel item representing a piece of content.
     */
    public static class UserItem {
        public String id;
        public String name;
        public String email;
        public boolean selected;


        public UserItem(String id, String name, String email, boolean selected) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.selected = selected;
        }

        @Override
        public String toString() {
            return name;

        }
    }
}


