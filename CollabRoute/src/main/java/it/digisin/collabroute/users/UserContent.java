package it.digisin.collabroute.users;

import java.util.ArrayList;
import java.util.HashMap;
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

        public static void cleanList(){
            if(ITEM_MAP != null)
                ITEM_MAP.clear();
            if(ITEMS != null)
                ITEMS.clear();
        }
        /**
         * A Travel item representing a piece of content.
         */
        public static class UserItem {
            public String id;
            public String name;
            public String email;


            public UserItem(String id, String name, String email) {
                this.id = id;
                this.name = name;
                this.email = email;


            }

            @Override
            public String toString() {
                return name;

            }
        }
    }


