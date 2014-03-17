package it.digisin.collabroute;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
/**
 * Created by raffaele on 17/03/14.
 */
public class JsonToMap {

    private String json;

    public JsonToMap(String json) {
        this.json = json;
    }

    HashMap<String,String> getMap(){
        Map<String,String> map;
        ObjectMapper mapper = new ObjectMapper();
         try {
            //convert JSON string to Map
            map = mapper.readValue(json,
                    new TypeReference<HashMap<String,String>>(){});
         return (HashMap)map;
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }
    }
}
