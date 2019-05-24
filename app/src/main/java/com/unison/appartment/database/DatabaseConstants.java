package com.unison.appartment.database;

public class DatabaseConstants {
    // Generale
    public final static String ROOT = "/";
    public final static String SEPARATOR = "/";

    // Case
    public final static String HOMES = "homes";
    public final static String HOMES_HOMENAME_PASSWORD = "password";

    // Utenti di una casa
    public final static String HOMEUSERS = "home-users";

    // Premi
    public final static String REWARDS = "rewards";
    public static final String REWARDS_HOMENAME_REWARDID_NAME = "name";
    public static final String REWARDS_HOMENAME_REWARDID_RESERVATIONID = "reservation-id";
    public static final String REWARDS_HOMENAME_REWARDID_RESERVATIONNAME = "reservation-name";

    // UncompletedTask
    public final static String UNCOMPLETEDTASKS = "uncompleted-tasks";
    public final static String UNCOMPLETEDTASKS_HOMENAME_TASKID_CREATIONDATE = "creation-date";

    // Case di un utente
    public final static String USERHOMES = "user-homes";

    // Utenti
    public final static String USERS = "users";
    public final static String USERS_UID_NAME = "name";
}
