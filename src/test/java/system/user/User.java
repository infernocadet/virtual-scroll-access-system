package system.user;

import java.util.Objects;

public class User {

    private String userID; // users should be able to change this id? can generate one on creation
    private String username;
    private String password;
    private Role role;
    private String fullName;
    private String email;
    private String phoneNo;

    public User(String username, String password, Role role) {
        if (username == null || password == null || role == null) {
            throw new NullPointerException("Username, password, and role cannot be null");
        }
        this.username = username;
        this.password = password;
        this.role = role;
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username) &&
                Objects.equals(password, user.password) &&
                role == user.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, role);
    }
}
