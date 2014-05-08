package it.raffaeletosti.collabroute.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by raffaele on 04/05/14.
 */
public class UsersListContent {

    public static List<UsersListItem> ITEMS = new ArrayList<UsersListItem>();

    public static Map<String, UsersListItem> ITEM_MAP = new HashMap<String, UsersListItem>();

    public static void addItem(UsersListItem item) {
        ITEM_MAP.put(item.id, item);
        ITEMS.add(item);
    }

    public static void deleteItem(String id) {
        if (!ITEM_MAP.isEmpty()) {
            UsersListItem toDelete = ITEM_MAP.get(id);
            ITEMS.remove(toDelete);
            ITEM_MAP.remove(id);
        }
    }

    public static void cleanList(){
        if(ITEM_MAP != null)
            ITEM_MAP.clear();
        if(ITEMS != null)
            ITEMS.clear();
    }

    public static void deselectOtherChoices(String id){
        if(!ITEM_MAP.isEmpty()){
            for(UsersListItem current : ITEMS){
                if(!current.id.equals(id)){
                    current.isSelected = false;
                }
            }
            for(String current : ITEM_MAP.keySet()){
                if(!current.equals(id)){
                    UsersListItem item = ITEM_MAP.get(current);
                    item.isSelected = false;
                }
            }
        }
    }

    public static class UsersListItem {
        public String id;
        public String userName;
        public boolean isOnLine;
        public boolean isSelected;
        public boolean isAdministrator;


        public UsersListItem(String id, String userName, boolean isOnLine, boolean isSelected, boolean isAdministrator) {
            this.id  = id;
            this.userName = userName;
            this.isOnLine = isOnLine;
            this.isSelected = isSelected;
            this.isAdministrator = isAdministrator;


        }

        @Override
        public String toString() {
            return userName;

        }
    }

}
