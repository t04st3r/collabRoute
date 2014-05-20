package it.raffaeletosti.collabroute.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by raffaele on 27/03/14.
 */
public class Travel {

    private String name;
    private int id;
    private User admin;
    private String description;
    private HashMap<String, User> people;
    private HashMap<String, MeetingPoint> routes;


    public Travel() {
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public HashMap<String, User> getPeople() {
        return people;
    }

    public void setPeople(HashMap<String, User> people) {
        this.people = people;
    }

    public HashMap<String, MeetingPoint> getRoutes() {
        return routes;
    }

    public void setRoutes(HashMap<String, MeetingPoint> routes) {
        this.routes = routes;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getAdmin() {
        return admin;
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }

    public void insertUser(User user) {
        if (people == null) {
            people = new HashMap<String, User>();
        }
        people.put(String.valueOf(user.getId()), user);
    }

    public HashMap<String, User> cloneUsersMap(){
        HashMap<String, User> toReturn = new HashMap<String, User>();
        for(String current : people.keySet()){
            toReturn.put(current, people.get(current));
        }
        return toReturn;
    }



    public String getUsersName() {
        if (people != null) {
            String users = "";
            for (String current : people.keySet()) {
                users += people.get(current).getName() + " ";
            }
            return users;
        }
        return null;
    }

    public void insertRoute(MeetingPoint route) {
        if (routes == null) {
            routes = new HashMap<String, MeetingPoint>();
        }
        routes.put(String.valueOf(route.getId()), route);
    }


    public String toJsonString() {
        JSONObject travel = new JSONObject();
        try {
            travel.put("name", name);
            travel.put("id", id);
            travel.put("description", description);
            JSONObject adminUser = new JSONObject();
            adminUser.put("id", admin.getId());
            adminUser.put("name", admin.getName());
            adminUser.put("eMail", admin.getEMail());
            travel.put("admin", adminUser);
            JSONArray peopleArray = new JSONArray();
            for (String current : people.keySet()) {
                JSONObject user = new JSONObject();
                user.put("id", people.get(current).getId());
                user.put("name", people.get(current).getName());
                user.put("eMail", people.get(current).getEMail());
                peopleArray.put(user);

            }
            travel.put("people", peopleArray);
            if (routes != null) {
                JSONArray routesArray = new JSONArray();
                for (String current : routes.keySet()) {
                    JSONObject route = new JSONObject();
                    route.put("id", routes.get(current).getId());
                    route.put("address", routes.get(current).getAddress());
                    route.put("latitude", routes.get(current).getLatitude());
                    route.put("longitude", routes.get(current).getLongitude());
                    routesArray.put(route);
                }
                travel.put("routes", routesArray);
            }
            return travel.toString();

        } catch (JSONException e) {
            System.err.println(e);
            return null;
        }
    }

    public void createFromJSONString(String json) {
        if (json != null) {
            try {
                JSONObject object = new JSONObject(json);
                name = object.getString("name");
                id = object.getInt("id");
                description = object.getString("description");
                JSONObject adminObj = (JSONObject) object.get("admin");
                admin = new User();
                admin.setId(adminObj.getInt("id"));
                admin.setEMail(adminObj.getString("eMail"));
                admin.setName(adminObj.getString("name"));
                people = new HashMap<String, User>();
                JSONArray users = object.getJSONArray("people");
                int length = users.length();
                for(int i = 0; i < length; i++){
                    JSONObject userObj = users.getJSONObject(i);
                    User newUser = new User();
                    newUser.setId(userObj.getInt("id"));
                    newUser.setName(userObj.getString("name"));
                    newUser.setEMail(userObj.getString("eMail"));
                    people.put(String.valueOf(newUser.getId()), newUser);
                }
                routes = new HashMap<String, MeetingPoint>();
                if(object.has("routes")){
                    JSONArray routesArray = object.getJSONArray("routes");
                    length = routesArray.length();
                    for(int i = 0; i < length; i++){
                        JSONObject currentRoute = routesArray.getJSONObject(i);
                        MeetingPoint current = new MeetingPoint();
                        current.setId(currentRoute.getInt("id"));
                        current.setAddress(currentRoute.getString("address"));
                        current.setLatitude(currentRoute.getString("latitude"));
                        current.setLongitude(currentRoute.getString("longitude"));
                        routes.put(String.valueOf(current.getId()) , current);
                    }
                }
            } catch (JSONException e) {
                System.err.println(e);
            }
        }
    }

}
