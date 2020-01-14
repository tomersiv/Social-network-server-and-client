package bgu.spl.net.api.bidi;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Databases {
    private ConcurrentHashMap<String,User> users;
    private ConcurrentHashMap<String,List<String>> followingMap;
    private ConcurrentHashMap<String, Queue<Message>> userMessages;
    private ConcurrentHashMap<String,Integer> numOfPosts;
    private ConcurrentHashMap<Integer,String> userNameByConnectionId;
    private LinkedList<String> userList;

    private static class SingletonHolder {
        private static Databases instance = new Databases();
    }
    private Databases() {
        this.users=new ConcurrentHashMap<>();
        this.followingMap=new ConcurrentHashMap<>();
        this.userMessages=new ConcurrentHashMap<>();
        this.numOfPosts=new ConcurrentHashMap<>();
        this.userNameByConnectionId=new ConcurrentHashMap<>();
        this.userList=new LinkedList<>();
    }
    public static Databases getInstance() {
        return SingletonHolder.instance;
    }

    public ConcurrentHashMap<String, User> getUsers() {
        return users;
    }

    public ConcurrentHashMap<String, List<String>> getFollowingMap() {
        return followingMap;
    }

    public ConcurrentHashMap<String, Queue<Message>> getUserMessages() {
        return userMessages;
    }

    public ConcurrentHashMap<String, Integer> getNumOfPosts() {
        return numOfPosts;
    }

    public ConcurrentHashMap<Integer, String> getUserNameByConnectionId() {
        return userNameByConnectionId;
    }

    public LinkedList<String> getUserList() {
        return userList;
    }
}
