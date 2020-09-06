package users;

import java.io.Serializable;

public class UsersDTO implements Serializable {
    private String username;
    private String password;
    private String fullName;
    private boolean admin;
    private boolean active;

    public UsersDTO() {
    }

    public UsersDTO(String username, String password, String fullName, boolean admin, boolean active) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.admin = admin;
        this.active = active;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "UsersDTO{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", fullName='" + fullName + '\'' +
                ", admin=" + admin +
                ", active=" + active +
                '}';
    }
}
