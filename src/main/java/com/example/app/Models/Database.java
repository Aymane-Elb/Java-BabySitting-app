package com.example.app.Models;

import java.sql.*;
import com.example.app.Models.hashPasswd.*;

public class Database {

    private static final String DB_URL = "jdbc:sqlite:C:/java babysitting app/aPP/src/main/resources/DB/BabySittingDB.db";

    public static Connection connect() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.out.println("SQLite connection failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return connection;
    }

    public static void insertUser(String username, String accountType, String email, String phone, String adress, String passwd) {
        String hashedPassword = hashPasswd.hashPassword(passwd);
        String sql = "INSERT INTO users(username, role, email, phone, adress, password) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect(); PreparedStatement injection = conn.prepareStatement(sql)) {
            injection.setString(1, username);
            injection.setString(2, accountType);
            injection.setString(3, email);
            injection.setString(4, phone);
            injection.setString(5, adress);
            injection.setString(6, hashedPassword);
            injection.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Insert failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}