package com.froze.firebase_auth;

/**
 * Created by froze on 2016-10-28.
 */


public class User {

    public String username;
    public String email;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

}
