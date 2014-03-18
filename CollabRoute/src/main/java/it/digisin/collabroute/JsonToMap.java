package it.digisin.collabroute;

import android.util.JsonReader;

import org.json.JSONObject;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by raffaele on 17/03/14.
 */
public class JsonToMap {

    private String jsonString;

    public JsonToMap(String jsonString) {
        this.jsonString = jsonString;
    }

    HashMap<String, String> getMap() {
        try {
            Map<String, String> map = new HashMap<String, String>();
            JSONObject json = new JSONObject(this.jsonString);
            Iterator keys = json.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                map.put(key, json.getString(key));
            }
            return (HashMap) map;
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }
    }
}
