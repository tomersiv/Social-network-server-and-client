package bgu.spl.net.api.bidi;

public class LoginMessage implements Message {
    private String username;
    private String password;

    public LoginMessage(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }


}
