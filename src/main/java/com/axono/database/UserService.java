package com.axono.database;

import java.sql.SQLException;
import java.util.Map;

public class UserService {

    private DatabaseHelper db;

    public UserService(DatabaseHelper db) {
        this.db = db;
    }

    public String getUserFullName(int userId) throws SQLException {
        Map<String, Object> user = db.getById("user", userId);

        if (user == null) {
            return null;
        }

        String firstName = (String) user.get("first_name");
        String lastName = (String) user.get("last_name");

        return firstName + " " + lastName;
    }

    public Map<String, Object> getUserUniversity(int userId) throws SQLException {
        Map<String, Object> user = db.getById("user", userId);

        if (user == null) {
            return null;
        }

        int universityId = (int) user.get("university_id");

        return db.getById("university", universityId);
    }
}