package system;

import system.user.UserManager;

public class Main {

    public static UserManager userManager;

    public static void main(String[] args) {
        userManager = new UserManager();
        userManager.loadUsers();
    }
}
