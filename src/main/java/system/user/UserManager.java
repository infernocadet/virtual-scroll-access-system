package system.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UserManager {

    public ArrayList<User> users;

    public UserManager() {
        users = new ArrayList<>();
    }

    public void addUser(User user) {
        users.add(user);
        // TODO: Write new json
    }

    public void removeUser(User user) {
        users.remove(user);
        // TODO: Remove user from json
    }

    public void loadUsers() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File file = new File("src/main/resources/users.json");
            users.addAll(mapper.readValue(file, new TypeReference<List<User>>() {}));
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }
}
