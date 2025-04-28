package org.example;

import java.sql.Connection;


import java.sql.*;

public class DCconnection {
    private static final String URL = "jdbc:mysql://localhost:3306/game_db?" +
            "useSSL=false&" +
            "allowPublicKeyRetrieval=true&" +
            "autoReconnect=true";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    public static Connection connect() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Database connection established");
            return conn;
        } catch (SQLException e) {
            System.err.println("Database connection failed");
            throw e;
        }
    }
}