package bgu.spl.net.api.bidi;

public class ErrorMessage implements Message{
    private short MsgOpcode;

    public ErrorMessage(short msgOpcode) {
        MsgOpcode = msgOpcode;
    }

    public short getMsgOpcode() {
        return MsgOpcode;
    }
}
