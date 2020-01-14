package bgu.spl.net.api.bidi;

import java.util.List;

public class AckFollowMessage extends AckMessage {
    private short numOfUsrs;
    private List<String> userList;
    public AckFollowMessage(short msgOpcode, short numOfUsers, List<String> userList) {
        super(msgOpcode);
        this.numOfUsrs=numOfUsers;
        this.userList=userList;
    }

    public short getNumOfUsrs() {
        return numOfUsrs;
    }

    public List<String> getUserList() {
        return userList;
    }
}
