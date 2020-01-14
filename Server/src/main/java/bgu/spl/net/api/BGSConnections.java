package bgu.spl.net.api;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.api.bidi.Message;
import bgu.spl.net.srv.bidi.BlockingConnectionHandler;
import bgu.spl.net.srv.bidi.ConnectionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BGSConnections<T> implements Connections<T> {
    private HashMap<Integer, ConnectionHandler<Message>> activeclients;

    public BGSConnections() {
        this.activeclients=new HashMap<>();
    }

    @Override
    public boolean send(int connectionId, T msg) {
        if(activeclients.get(connectionId)==null)
            return false;
        ConnectionHandler client=activeclients.get(connectionId);
        synchronized (client) {
            client.send((Message) msg);
        }
        return true;

    }

    @Override
    public void broadcast(T msg) {
        for(Map.Entry<Integer,ConnectionHandler<Message>> entry:activeclients.entrySet()){
            entry.getValue().send((Message) msg);
        }

    }

    @Override
    public void disconnect(int connectionId) {
        activeclients.remove(connectionId);
    }

    public HashMap<Integer, ConnectionHandler<Message>> getActive() {
        return activeclients;
    }
}
