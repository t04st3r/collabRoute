package it.digisin.collabroute.model;

import java.util.HashMap;

/**
 * Created by raffaele on 27/03/14.
 */
public class Travel {

    private String name;
    private int id;
    private User admin;
    private static HashMap<String, User> people;
    private static HashMap<String, MeetingPoint> routes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
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
}
