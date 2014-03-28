package it.digisin.collabroute.model;

/**
 * Created by raffaele on 27/03/14.
 */
public class User {
    protected String eMail;
    protected int id;
    protected String name;

    public String getEMail() {
        return eMail;
    }

    public void setEMail(String eMail) {
        this.eMail = eMail;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
