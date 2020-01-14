package bgu.spl.net.api.bidi;

public class StatMessage implements Message {
    private String username;

    public StatMessage(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
