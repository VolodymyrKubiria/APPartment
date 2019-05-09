package com.unison.appartment.model;

import java.util.Map;

public class UserHome {

    public final static String ROLE_OWNER = "Proprietario";
    public final static String ROLE_MASTER = "Leader";
    public final static String ROLE_SLAVE = "Collaboratore";

    private String homeName;
    private String role;

    public UserHome(String homeName, String role) {
        this.homeName = homeName;
        this.role = role;
    }

    public String getHomeName() {
        return homeName;
    }

    public void setHomeName(String homeName) {
        this.homeName = homeName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
