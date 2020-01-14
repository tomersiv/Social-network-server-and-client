package bgu.spl.net.api;

import bgu.spl.net.api.bidi.*;
import bgu.spl.net.srv.bidi.BlockingConnectionHandler;
import bgu.spl.net.srv.bidi.ConnectionHandler;
import javafx.geometry.Pos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class BGSMessagingProtocol<T> implements BidiMessagingProtocol<Message> {
    private int connectionid;
    private BGSConnections connections;
    private boolean shouldTerminate;


    @Override
    public void start(int connectionId, Connections<Message> connections) {
        this.connectionid=connectionId;
        this.connections=(BGSConnections)connections;
        shouldTerminate=false;
    }

    @Override
    public  void  process(Message message) {
        Databases databases = Databases.getInstance();
        ConcurrentHashMap<Integer,String> UserNameByConnectionId=databases.getUserNameByConnectionId();
        String myUserName = UserNameByConnectionId.get(connectionid);
        //check for every message the instance of the message

            if (message instanceof RegisterMessage) {
                synchronized (((RegisterMessage) message).getUsername()) {
                    //check if the username already exists.
                    if (databases.getUsers().containsKey(((RegisterMessage) message).getUsername())) {
                        connections.send(connectionid, new ErrorMessage((short) 1));
                    } else {
                        //add new user to the database.
                        User user = new User(((RegisterMessage) message).getUsername(), ((RegisterMessage) message).getPassword());
                        if (databases.getUsers().putIfAbsent(user.getUsername(), user) != null) {
                            connections.send(connectionid, new ErrorMessage((short) 1));
                        }
                        else {
                            databases.getUserList().add(((RegisterMessage) message).getUsername());
                            connections.send(connectionid, new AckMessage((short) 1));
                        }
                    }
                }

        }

        if (message instanceof LoginMessage) {
            //check if this username exists
            if (!databases.getUsers().containsKey(((LoginMessage) message).getUsername()))
                connections.send(connectionid, new ErrorMessage((short) 2));
                //check if the password is correct.
            else if (!databases.getUsers().get(((LoginMessage) message).getUsername()).getPassword().equals(((LoginMessage) message).getPassword()))
                connections.send(connectionid, new ErrorMessage((short) 2));
                //check if the client is already connected.
            else if (databases.getUserNameByConnectionId().containsKey(connectionid))
                connections.send(connectionid, new ErrorMessage((short) 2));
            else if (databases.getUserNameByConnectionId().containsValue(((LoginMessage) message).getUsername()))
                connections.send(connectionid, new ErrorMessage((short) 2));
            else {
                //to prevent 2 clients trying log in to the same user in the same time.
                synchronized (((LoginMessage) message).getUsername()) {
                    UserNameByConnectionId.putIfAbsent(connectionid, ((LoginMessage) message).getUsername());
                    connections.send(connectionid, new AckMessage((short) 2));
                    Queue<Message> userMessages = databases.getUserMessages().get(((LoginMessage) message).getUsername());
                    //send messages that have been sent when client was logged out.
                    if (userMessages != null) {
                        while (!userMessages.isEmpty()) {
                            Message m = userMessages.remove();
                            if (m instanceof PMmessage)
                                connections.send(connectionid, new NotificationMessage('0', ((LoginMessage) message).getUsername(), ((PMmessage) m).getContent()));
                            if (m instanceof PostMessage)
                                connections.send(connectionid, new NotificationMessage('1', ((LoginMessage) message).getUsername(), ((PostMessage) m).getContent()));
                        }
                    }
                }
            }
        }

        if (message instanceof LogoutMessage) {
            String userName=UserNameByConnectionId.get(connectionid);
            if(userName==null)
                connections.send(connectionid,new ErrorMessage((short)3));
            else {
                //to prevent logout in the same time of getting pm,private messages.
                synchronized (databases.getUserNameByConnectionId().get(connectionid)) {
                    UserNameByConnectionId.remove(connectionid);
                    connections.send(connectionid, new AckMessage((short) 3));
                    connections.disconnect(connectionid);
                }
            }
        }

        if (message instanceof FollowMessage) {
            int followOrUnfollow = ((FollowMessage) message).getParameter();
            List<String> messageUserList = ((FollowMessage) message).getUsersList();
            int numOfUsers = ((FollowMessage) message).getNumofUsers();
            if (!UserNameByConnectionId.containsKey(connectionid))
                connections.send(connectionid, new ErrorMessage((short) 4));
            else {
                if (followOrUnfollow == 0) {
                    int numOfsuccessful = 0;
                    List<String> myFollowing = databases.getFollowingMap().get(myUserName);
                    List<String> successfulFollowing = new LinkedList<>();
                    if (myFollowing == null) {
                        myFollowing = new LinkedList<>();
                        databases.getFollowingMap().put(myUserName, myFollowing);
                    }
                        for (String username : messageUserList) {
                            if (!myFollowing.contains(username)) {
                                if(databases.getUsers().containsKey(username)) {
                                    myFollowing.add(username);
                                    successfulFollowing.add(username);
                                    numOfsuccessful++;
                                }
                            }
                        }
                        if (numOfsuccessful == 0)
                            connections.send(connectionid, new ErrorMessage((short) 4));
                        else
                            connections.send(connectionid, new AckFollowMessage((short) 4, (short)numOfsuccessful, successfulFollowing));
                    }
                }

                if (followOrUnfollow == 1) {
                    int numOfsuccessful = 0;
                    List<String> myFollowing = databases.getFollowingMap().get(myUserName);
                    List<String> successfulFollowing = new LinkedList<>();
                    if (myFollowing == null)
                        connections.send(connectionid, new ErrorMessage((short) 4));
                    else {
                        for (String username : messageUserList) {
                            if (myFollowing.contains(username)) {
                                myFollowing.remove(username);
                                successfulFollowing.add(username);
                                numOfsuccessful++;
                            }
                        }
                        if (numOfsuccessful == 0)
                            connections.send(connectionid, new ErrorMessage((short) 4));
                        else
                            connections.send(connectionid, new AckFollowMessage((short) 4, (short)numOfsuccessful, successfulFollowing));
                    }
                }

        }



        if(message instanceof PostMessage){
            //get the followers of the user
            List<String> following=databases.getFollowingMap().get(myUserName);
            ConcurrentHashMap<String,List<String>> followingMap=databases.getFollowingMap();
            //check if the user is loged in.
            if(!UserNameByConnectionId.containsKey(connectionid))
                connections.send(connectionid,new ErrorMessage((short) 5));
            else{
                //add 1 to the number of posts of this user.
                if(databases.getNumOfPosts().containsKey(myUserName)) {
                   Integer numOfPosts= databases.getNumOfPosts().get(myUserName);
                   numOfPosts++;
                }
                else {
                    databases.getNumOfPosts().put(myUserName, 1);
                }
                int index=0;
                String content=((PostMessage) message).getContent();
                List<String> tags=new LinkedList<>();
                while(index<content.length()){
                    if(content.charAt(index)=='@') {
                        if(content.indexOf(' ', index)!=-1) {
                            if(!tags.contains(content.substring(index + 1, content.indexOf(' ', index)))) {
                                tags.add(content.substring(index + 1, content.indexOf(' ', index)));
                                index = content.indexOf(' ', index);
                            }
                            else
                                index = content.indexOf(' ', index);
                        }
                        else {
                            if(!tags.contains(content.substring(index + 1)))
                                tags.add(content.substring(index + 1));
                            index=content.length();
                        }
                    }
                    else
                        index++;
                }
                //save the message for the @usernames.
                for(String s: tags){
                    int tagsConnectionID=-1;
                    tagsConnectionID = findConIdByUserName(s);
                    if(tagsConnectionID!=-1 ) {
                        //to prevent logout in the same time of getting pm,private messages.
                        synchronized (databases.getUserNameByConnectionId().get(tagsConnectionID)) {
                            connections.send(tagsConnectionID, new NotificationMessage('1', myUserName, ((PostMessage) message).getContent()));
                        }
                    }
                    else{
                        Queue<Message> messageQueue=databases.getUserMessages().get(s);
                        if(messageQueue==null) {
                            messageQueue = new LinkedBlockingQueue<>();
                            databases.getUserMessages().put(s,messageQueue);
                        }
                        messageQueue.add(message);
                    }
                }

                int followerConnectionID=-1;
                for(Map.Entry<String,List<String>> user : followingMap.entrySet()){
                    if(user.getValue().contains(myUserName)) {
                        followerConnectionID = findConIdByUserName(user.getKey());

                            if (followerConnectionID != -1){
                                //to prevent logout in the same time of getting pm,private messages.
                                synchronized (databases.getUserNameByConnectionId().get(followerConnectionID)) {
                                    if(!tags.contains(user.getKey()))
                                        connections.send(followerConnectionID, new NotificationMessage('1', myUserName, ((PostMessage) message).getContent()));
                                }
                        }
                        else{
                            Queue<Message> messageQueue=databases.getUserMessages().get(user.getKey());
                            if(messageQueue==null) {
                                messageQueue = new LinkedBlockingQueue<>();
                                databases.getUserMessages().put(user.getKey(),messageQueue);
                            }
                            if(!messageQueue.contains(message))
                                messageQueue.add(message);
                        }
                    }
                }
                connections.send(connectionid,new AckMessage((short)5));
            }
        }

        if(message instanceof PMmessage){
            if(!UserNameByConnectionId.containsKey(connectionid))
                connections.send(connectionid,new ErrorMessage((short) 6));
            else if(!databases.getUsers().containsKey(((PMmessage) message).getUsername()))
                connections.send(connectionid,new ErrorMessage((short) 6));
            else {
                String dstUserName = ((PMmessage) message).getUsername();
                int dstUserConnectionId = -1;
                dstUserConnectionId = findConIdByUserName(dstUserName);
                if (dstUserConnectionId == -1) {
                    Queue<Message> messageQueue = databases.getUserMessages().get(dstUserName);
                    if (messageQueue == null) {
                        messageQueue = new LinkedBlockingQueue<>();
                        databases.getUserMessages().put(dstUserName, messageQueue);
                    }
                    messageQueue.add(message);
                }
                else {
                    //to prevent logout in the same time of getting pm,private messages.
                    synchronized (databases.getUserNameByConnectionId().get(dstUserConnectionId)) {
                        for (Map.Entry<Integer, String> user : UserNameByConnectionId.entrySet()) {
                            if (user.getValue().equals(((PMmessage) message).getUsername()))
                                connections.send(user.getKey(), new NotificationMessage('0', myUserName, ((PMmessage) message).getContent()));
                        }

                    }
                }
                connections.send(connectionid, new AckMessage((short) 6));
            }

        }

        if(message instanceof UserlistMessage){
            if(!UserNameByConnectionId.containsKey(connectionid))
                connections.send(connectionid,new ErrorMessage((short)7));
            else {
                LinkedList<String> users = databases.getUserList();
                int numOfusers = users.size();
                connections.send(connectionid, new UserlistAckMessage((short) 7, (short)numOfusers, users));
            }
        }

        if(message instanceof StatMessage) {
            if (!UserNameByConnectionId.containsKey(connectionid))
                connections.send(connectionid, new ErrorMessage((short) 8));
            else if (!databases.getUsers().containsKey(((StatMessage) message).getUsername()))
                connections.send(connectionid, new ErrorMessage((short) 8));
            else {
                int numOfPosts=0;
                if(databases.getNumOfPosts()!=null&&databases.getNumOfPosts().get(((StatMessage) message).getUsername())!=null)
                     numOfPosts = databases.getNumOfPosts().get(((StatMessage) message).getUsername());
                int numOfFollowers = 0;
                int numOfFollowing = 0;
                ConcurrentHashMap<String, List<String>> following = databases.getFollowingMap();
                List<String> followingList = following.get(((StatMessage) message).getUsername());
                //calculate num of users i follow them.
                if (followingList != null)
                    numOfFollowing = numOfFollowing + followingList.size();
                //calculate num of users follow me.
                for (Map.Entry<String, List<String>> user : following.entrySet()) {
                    List<String> followers = user.getValue();
                    if (followers.contains(((StatMessage) message).getUsername()))
                        numOfFollowers++;
                }
                connections.send(connectionid,new StatAckMessage((short)8,(short)numOfPosts,(short)numOfFollowers,(short)numOfFollowing));
            }
        }


    }






    private int findConIdByUserName(String userName) {
        Databases databases = Databases.getInstance();
        ConcurrentHashMap<Integer,String> UserNameByConnectionId=databases.getUserNameByConnectionId();
        for (Map.Entry<Integer, String> user : UserNameByConnectionId.entrySet()) {
            if(user.getValue().equals(userName))
                return user.getKey();
        }
        return -1;
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
