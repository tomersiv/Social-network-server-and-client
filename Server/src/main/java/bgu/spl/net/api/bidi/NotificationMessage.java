package bgu.spl.net.api.bidi;

public class NotificationMessage implements Message {
    private char notoficationType;
    private String username;
    private String content;

    public NotificationMessage(char notoficationType, String username, String content) {
        this.notoficationType = notoficationType;
        this.username = username;
        this.content = content;
    }

    public char getNotoficationType() {
        return notoficationType;
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }
}
