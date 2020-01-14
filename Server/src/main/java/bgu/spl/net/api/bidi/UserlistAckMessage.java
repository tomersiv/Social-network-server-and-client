package bgu.spl.net.api.bidi;

import java.util.List;

public class UserlistAckMessage extends AckMessage {
    private  short numOfUsers;
    private List<String> users;
    public UserlistAckMessage(short msgOpcode, short numOfUsers, List<String> users) {
        super(msgOpcode);
        this.numOfUsers=numOfUsers;
        this.users=users;
    }

    public short getNumOfUsers() {
        return numOfUsers;
    }

    public List<String> getUsers() {
        return users;
    }
}
