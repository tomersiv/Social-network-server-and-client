package bgu.spl.net.api.bidi;

public class StatAckMessage extends AckMessage {
    private short numPosts;
    private short numfollowers;
    private short numFolowing;
    public StatAckMessage(short msgOpcode,short numPosts,short numFollowers,short numFollowing) {
        super(msgOpcode);
        this.numPosts=numPosts;
        this.numfollowers=numFollowers;
        this.numFolowing=numFollowing;
    }

    public short getNumPosts() {
        return numPosts;
    }

    public short getNumfollowers() {
        return numfollowers;
    }

    public short getNumFolowing() {
        return numFolowing;
    }
}
