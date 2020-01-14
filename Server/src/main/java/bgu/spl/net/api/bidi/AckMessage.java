package bgu.spl.net.api.bidi;

public class AckMessage implements Message {
    private short MsgOpcode;

    public AckMessage(short msgOpcode) {
        MsgOpcode = msgOpcode;
    }

    public short getMsgOpcode() {
        return MsgOpcode;
    }
}
