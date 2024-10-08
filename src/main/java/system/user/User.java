package system.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Random;

public class User {

    private String userID; // users should be able to change this id? can generate one on creation
    private final String username;
    private String password;
    private final Role role;
    private String fullName;
    private String email;
    private String phoneNo;

    public User(@JsonProperty("username") String username, @JsonProperty("password") String password, @JsonProperty("role") Role role) {
        if (username == null || password == null || role == null) {
            throw new NullPointerException("Username, password, and role cannot be null");
        }
        this.username = username;
        this.password = password;
        this.role = role;
        this.userID = generateID(username);
    }

    // GETTERS
    public String getUsername() {return username;}
    public String getPassword() {return password;}
    public Role getRole() {return role;}
    public String getUserID() {return userID;}
    public String getFullName() {return Objects.requireNonNullElse(this.fullName, "N/A");}
    public String getEmail() {return Objects.requireNonNullElse(this.email, "N/A");}
    public String getPhoneNo() {return Objects.requireNonNullElse(this.phoneNo, "N/A");}

    // SETTERS - assuming username cannot be changed.
    public void setPassword(String password){this.password = password;} // users can change pw
    public void setUserID(String userID){this.userID = userID;}
    public void setFullName(String fullName){this.fullName = fullName;}
    public void setEmail(String email){this.email = email;}
    public void setPhoneNo(String phoneNo){this.phoneNo = phoneNo;}

    /**
     * Creates an 8-char UID based on username and random 4 digit number
     * @return String UID
     */
    private String generateID(String username){
        String nameID;
        if (username.length() >= 4){
            nameID = username.substring(0, 3);
        } else {
            nameID = username;
        }
        return nameID + String.format("%04d", new Random().nextInt(10000));

    }
}
