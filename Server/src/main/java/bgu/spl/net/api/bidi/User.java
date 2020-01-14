package bgu.spl.net.api.bidi;

public class User {
    private String username;
    private String password;
    private long logoutTime;
    private boolean isUserLoggedin;
    private int connectionID;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public User(){

    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
