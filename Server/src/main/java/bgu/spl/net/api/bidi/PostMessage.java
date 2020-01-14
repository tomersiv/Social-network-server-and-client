package bgu.spl.net.api.bidi;

public class PostMessage implements Message {
    private String content;
    private long time;
    public PostMessage(String content,long time) {
        this.content = content;
        this.time=time;
    }

    public String getContent() {
        return content;
    }

    public long getTime() {
        return time;
    }
}
