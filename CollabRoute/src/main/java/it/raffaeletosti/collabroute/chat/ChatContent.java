package it.raffaeletosti.collabroute.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 */
public class ChatContent {

    /**
     * An array of sample (ChatLine) items.
     */
    public static List<ChatItem> ITEMS = new ArrayList<ChatItem>();

    private static int counter = 0;
    /**
     * A map of sample (ChatLine) items, by ID.
     */
    public static Map<String, ChatItem> ITEM_MAP = new HashMap<String, ChatItem>();


    public static void addItem(ChatItem item) {
        String cont = String.valueOf(counter++);
        item.id = cont;
        ITEM_MAP.put(cont, item);
        ITEMS.add(item);
    }

    public static void deleteItem(String id) {
        if (!ITEM_MAP.isEmpty()) {
            ChatItem toDelete = ITEM_MAP.get(id);
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
    /**
     * A Chat item representing a piece of content.
     */
    public static class ChatItem {
        public String id;
        public String userName;
        public String text;


        public ChatItem(String userName, String text) {
            this.userName = userName;
            this.text = text;


        }

        @Override
        public String toString() {
            return userName;

        }
    }
}
