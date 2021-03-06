package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.BGSMessageEncoderDecoder;
import bgu.spl.net.api.BGSMessagingProtocol;
import bgu.spl.net.api.bidi.Message;
import bgu.spl.net.srv.Server;

public class ReactorMain {
    public static void main(String[] args) {
        int port=Integer.parseInt(args[0]);
        int numOfThreads= Integer.parseInt(args[1]);
    Server.reactor(
            numOfThreads,
            port,
                () ->  new BGSMessagingProtocol<Message>(), //protocol factory
                BGSMessageEncoderDecoder::new //message encoder decoder factory
        ).serve();
    }
}
