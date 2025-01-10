package org.example.symulator_gieldy.User;

public class SessionUser {
    private static User loggedInUser;

    // Metoda do ustawienia zalogowanego użytkownika
    public static void setLoggedInUser(User user) {
        loggedInUser = user;
    }

    // Metoda do pobrania zalogowanego użytkownika
    public static User getLoggedInUser() {
        return loggedInUser;
    }

    // Metoda do sprawdzenia, czy użytkownik jest zalogowany
    public static boolean isLoggedIn() {
        return loggedInUser != null;
    }

    public static void logout() {
        loggedInUser = null;
    }
}
