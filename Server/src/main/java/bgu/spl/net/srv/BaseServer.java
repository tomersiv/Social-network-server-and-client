package bgu.spl.net.srv;

import bgu.spl.net.api.BGSConnections;
import bgu.spl.net.api.BGSMessageEncoderDecoder;
import bgu.spl.net.api.BGSMessagingProtocol;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.bidi.BlockingConnectionHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

public abstract class BaseServer<T> implements Server<T> {

    private final int port;
    private final Supplier<BGSMessagingProtocol<T>> protocolFactory;
    private final Supplier<BGSMessageEncoderDecoder<T>> encdecFactory;
    private ServerSocket sock;
    private BGSConnections connections;

    public BaseServer(
            int port,
            Supplier<BGSMessagingProtocol<T>> protocolFactory,
            Supplier<BGSMessageEncoderDecoder<T>> encdecFactory) {

        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
		this.sock = null;
		connections=new BGSConnections<>();
    }

    @Override
    public void serve() {
        int counter=0;
        try (ServerSocket serverSock = new ServerSocket(port)) {
			System.out.println("Server started");

            this.sock = serverSock; //just to be able to close

            while (!Thread.currentThread().isInterrupted()) {

                Socket clientSock = serverSock.accept();

                BlockingConnectionHandler<T> handler = new BlockingConnectionHandler<T>(
                        clientSock,
                        encdecFactory.get(),
                        protocolFactory.get(),
                        counter,
                        connections);

                counter++;
                connections.getActive().put(handler.getConnectionID(),handler);
                execute(handler);

            }
        } catch (IOException ex) {
        }

        System.out.println("server closed!!!");
    }

    @Override
    public void close() throws IOException {
		if (sock != null)
			sock.close();
    }

    protected abstract void execute(BlockingConnectionHandler<T>  handler);

}
