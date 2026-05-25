package com.axono.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private DatabaseHelper databaseHelper;
    private UserService userService;
    private Connection testConnection;

    @BeforeEach
    void setup() throws Exception {
        String testDbUrl = "jdbc:sqlite::memory:";
        testConnection = DriverManager.getConnection(testDbUrl);

        try (Statement statement = testConnection.createStatement()) {
            statement.execute(
                    "CREATE TABLE user (id INTERGER PRIMARY KEY, first_name TEXT, last_name TEXT, university_id INTEGER)");
            statement.execute("CREATE TABLE university (id INTERGER PRIMARY KEY, name TEXT)");

            statement.execute(
                    "INSERT INTO user (id, first_name, last_name, university_id) VALUES (1, 'Joe', 'Bloggs', 1)");
            statement.execute("INSERT INTO university (id, name) VALUES (1, 'University of York')");
        }

        databaseHelper = new DatabaseHelper();

        Field connectionField = DatabaseHelper.class.getDeclaredField("connection");
        connectionField.setAccessible(true);

        Connection autoConnection = (Connection) connectionField.get(databaseHelper);
        if (autoConnection != null) {
            autoConnection.close();
        }

        connectionField.set(databaseHelper, testConnection);

        userService = new UserService(databaseHelper);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }

    @Test
    void sampleTest() {
        assertEquals(4, 2 + 2);
    }

    @Test
    void testGetUserFullNameFound() throws Exception {
        String fullName = userService.getUserFullName(1);
        assertEquals("Joe Bloggs", fullName);
    }

    @Test
    void testGetUserFullNameNotFound() throws Exception {
        String fullName = userService.getUserFullName(10);
        assertEquals("null null", fullName);
    }

    @Test
    void testGetUserUniversitySuccess() throws Exception {
        Map<String, Object> university = userService.getUserUniversity(1);

        assertNotNull(university);
        assertEquals("University of York", university.get("name"));
    }

}
