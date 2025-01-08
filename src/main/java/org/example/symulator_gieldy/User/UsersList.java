package org.example.symulator_gieldy.User;

import java.util.ArrayList;

public class UsersList {

    private ArrayList<User> users = new ArrayList<>();
    private User loggedUser;

    private UsersList(){}
    public static UsersList getInstance(){
        return UserListHolder.INSTANCE;
    }

    public void add(User user){
        users.add(user);
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public boolean validateUser(String username, String password) {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                loggedUser = user;
                return true;
            }
        }
        return false;
    }
    public User getLoggedUser() {
        return loggedUser;
    }

    private static class UserListHolder{
        private static final UsersList INSTANCE = new UsersList();
    }
}
