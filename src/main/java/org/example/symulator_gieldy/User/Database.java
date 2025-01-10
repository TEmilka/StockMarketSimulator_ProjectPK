package org.example.symulator_gieldy.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static final String DATABASE_URL = "jdbc:sqlite:database/database.db";

    public static Connection connect() {
        try {
            return DriverManager.getConnection(DATABASE_URL);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Nie udało się połączyć z bazą danych.");
        }
    }
}
