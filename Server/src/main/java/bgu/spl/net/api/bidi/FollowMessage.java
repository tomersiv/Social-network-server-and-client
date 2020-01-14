package bgu.spl.net.api.bidi;

import java.util.List;

public class FollowMessage implements Message{
    private int parameter;
    private int NumofUsers;
    private List<String> UsersList;

    public FollowMessage(int parameter, int numofUsers, List<String> usersList) {
        this.parameter = parameter;
        NumofUsers = numofUsers;
        UsersList = usersList;
    }

    public int getParameter() {
        return parameter;
    }

    public int getNumofUsers() {
        return NumofUsers;
    }

    public List<String> getUsersList() {
        return UsersList;
    }
}
