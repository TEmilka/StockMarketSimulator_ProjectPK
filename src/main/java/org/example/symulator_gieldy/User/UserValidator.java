package org.example.symulator_gieldy.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserValidator {
    public static User validateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection connection = Database.connect();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    int userId = resultSet.getInt("id");
                    String userName = resultSet.getString("username");
                    String userPassword = resultSet.getString("password");

                    // Zwróć użytkownika, jeśli znaleziono dopasowanie
                    User user = new User(userName, userPassword);
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Brak użytkownika z takim loginem i hasłem
    }
}
