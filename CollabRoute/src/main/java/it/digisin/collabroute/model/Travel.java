package it.digisin.collabroute.model;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by raffaele on 27/03/14.
 */
public class Travel {

    private String name;
    private int id;
    private User admin;
    private String description;
    private static HashMap<String, User> people;
    private static HashMap<String, MeetingPoint> routes;

    public String getName() {return name;}

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

    public static HashMap<String, User> getPeople() {
        return people;
    }

    public static void setPeople(HashMap<String, User> people) {
        Travel.people = people;
    }

    public static HashMap<String, MeetingPoint> getRoutes() {
        return routes;
    }

    public static void setRoutes(HashMap<String, MeetingPoint> routes) {
        Travel.routes = routes;
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

    public void insertUser(User user){
        if(people == null){
            people = new HashMap<String, User>();
        }
        people.put(String.valueOf(user.getId()), user);
    }

    public String getUsersName(){
        if(people != null){
            String users = new String();
            Iterator<String> iterator = people.keySet().iterator();
            while(iterator.hasNext()){
                String current = iterator.next();
                users += people.get(current).getName()+" ";
            }
            return users;
        }
        return null;
    }
    //TODO methods for remove or update user in the hashmap and methods for insert, remove, update routes hashmap
}
