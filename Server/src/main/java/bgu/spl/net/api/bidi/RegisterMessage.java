package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.bidi.BlockingConnectionHandler;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.HashMap;

public class RegisterMessage implements Message {
 private String username;
 private String password;

    public RegisterMessage(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public Message process(){
        Databases databases=Databases.getInstance();
        if(databases.getUsers().containsKey(username))
            return new ErrorMessage((short)1);
        else {
            User user=new User(username,password);
            databases.getUsers().put(username,user);
            return new AckMessage((short)1);
        }


    }
}
