package bgu.spl.net.api.bidi;

public class PMmessage implements Message {
    private String username;
    private String content;
    private long time;
    public PMmessage(String username, String content,long time) {
        this.username = username;
        this.content = content;
        this.time=time;
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }

    public long getTime() {
        return time;
    }
}
