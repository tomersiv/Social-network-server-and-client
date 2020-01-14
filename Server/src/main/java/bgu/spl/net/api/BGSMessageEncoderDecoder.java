package bgu.spl.net.api;

import bgu.spl.net.api.bidi.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class BGSMessageEncoderDecoder<T> implements MessageEncoderDecoder<Message> {

    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
    private short opcode=0;
    public int counter=0;
    private int startingFollowIndex=5;
    public boolean finish=false;
    private boolean flag=false;
    private String username = "";
    private String password = "";
    private int startingindex=0;
    private short followUnfollow=0;
    private short numOfUsers = 0;
    private byte[] temp = new byte[2];
    private int zeroCounter = 0;
    private String content="";
    private char notificationType = '0';
    private List<String> users = new LinkedList<>();
    private void resetFields(){
        username="";
        password="";
        len = 0;
        counter=0;
        bytes=new byte[1 << 10];
        flag=false;
        startingindex=0;
        followUnfollow=0;
        startingFollowIndex=5;
        numOfUsers=0;
        temp = new byte[2];
        zeroCounter=0;
        users = new LinkedList<>();
        content="";
        notificationType = '0';
    }
    @Override
    public Message decodeNextByte(byte nextByte) {
        if (counter == 1) {
            pushByte(nextByte);
            len--;
            opcode = bytesToShort(bytes);

            if (opcode == 3) {
                resetFields();
                return new LogoutMessage();
            }
            if (opcode == 7) {
                resetFields();
                return new UserlistMessage();
            }
        }
        if(counter>=1){
            if (opcode == 1) {
                if (nextByte == '\0' && flag) {
                    password = password + popString(startingindex, len);
                    Message registerMessage = new RegisterMessage(username, password);
                    resetFields();
                    return registerMessage;
                }
                if (nextByte == '\0' && !flag) {
                    username = username + popString(2, len);
                    flag = true;
                    startingindex = len + 1;
                }
            }

            if (opcode == 2) {
                if (nextByte == '\0' && flag) {
                    password = password + popString(startingindex, len);
                    Message loginMessage = new LoginMessage(username, password);
                    resetFields();
                    return loginMessage;
                }
                if (nextByte == '\0' && !flag) {
                    username = username + popString(2, len);
                    flag = true;
                    startingindex = len + 1;
                }

            }
            if (opcode == 4) {
                if (counter == 2) {
                    followUnfollow = nextByte;
                }
                if (counter == 5) {
                    temp[0] = bytes[3];
                    temp[1] = bytes[4];
                    numOfUsers = bytesToShort(temp);
                    temp = new byte[2];
                }
                if (counter > 5) {
                    if (nextByte == '\0' && zeroCounter < numOfUsers) {
                        users.add(popString(startingFollowIndex, len));
                        zeroCounter++;
                        startingFollowIndex = len + 1;
                    }
                    if (zeroCounter == numOfUsers) {
                        FollowMessage followMessage=new FollowMessage(followUnfollow, numOfUsers, users);
                        resetFields();
                        return followMessage;
                    }
                }
            }
            if (opcode == 5) {
                if (nextByte == '\0') {
                    content = content + popString(2, len);
                    PostMessage postMessage = new PostMessage(content,System.currentTimeMillis());
                    resetFields();
                    return postMessage;
                }
            }
            if (opcode == 6) {
                if (nextByte == '\0' && flag) {
                    content = content + popString(startingindex, len);
                    Message PMmessage = new PMmessage(username, content,System.currentTimeMillis());
                    resetFields();
                    return PMmessage;
                }
                if (nextByte == '\0' && !flag) {
                    username = username + popString(2, len);
                    flag = true;
                    startingindex = len + 1;
                }

            }



            if (opcode == 8) {
                if (nextByte == '\0') {
                    username = username + popString(2, len);
                    StatMessage statMessage = new StatMessage(username);
                    resetFields();
                    return statMessage;
                }
            }

            if (opcode == 9) {
                if (counter == 3) {
                    notificationType = popString(2, len).charAt(0);
                }
                if (nextByte == '\0' && flag) {
                    content = content + popString(startingindex, len);
                    len = 0;
                    Message notificationMessage = new NotificationMessage(notificationType, username, content);
                    resetFields();
                    return notificationMessage;
                }
                if (nextByte == '\0' && !flag) {
                    username = username + popString(3, len);
                    flag = true;
                    startingindex = len + 1;
                }

            }

            if (opcode == 10) {
                if (counter == 4) {
                    byte[] temp = new byte[2];
                    temp[0] = bytes[2];
                    temp[1] = bytes[3];
                    short MessageOpcode = bytesToShort(temp);
                    resetFields();
                    return new AckMessage(MessageOpcode);
                }
            }

            if (opcode == 11) {
                if (counter == 4) {
                    byte[] temp = new byte[2];
                    temp[0] = bytes[2];
                    temp[1] = bytes[3];
                    short MessageOpcode = bytesToShort(temp);
                    resetFields();
                    return new ErrorMessage(MessageOpcode);
                }
            }
        }
            counter++;
            pushByte(nextByte);
            return null;
        }


    @Override
    public byte[] encode(Message message) {
        if(message instanceof ErrorMessage){
            byte[] errorOpcode=shortToBytes((short)11);
            byte[] messageOpcode=shortToBytes(((ErrorMessage) message).getMsgOpcode());
            byte[] fullMessage=new byte[4];
            fullMessage[0]=errorOpcode[0];
            fullMessage[1]=errorOpcode[1];
            fullMessage[2]=messageOpcode[0];
            fullMessage[3]=messageOpcode[1];
            return fullMessage;
        }
        if(message instanceof UserlistAckMessage){
            int counter=0;
            int index=6;
            byte[] AckOpcode=shortToBytes((short)10);
            byte[] userListOpcode=shortToBytes((short)7);
            byte[] numofUsers=shortToBytes(((UserlistAckMessage) message).getNumOfUsers());
            List<String> users=((UserlistAckMessage) message).getUsers();
            for(String user: users){
                counter=counter+user.getBytes().length+1;
            }
            byte[] fullMessage=new byte[6+counter];
            fullMessage[0]=AckOpcode[0];
            fullMessage[1]=AckOpcode[1];
            fullMessage[2]=userListOpcode[0];
            fullMessage[3]=userListOpcode[1];
            fullMessage[4]=numofUsers[0];
            fullMessage[5]=numofUsers[1];
            for(String user: users){
                byte[] userName=user.getBytes();
                for(int i=0;i<userName.length;i++) {
                    fullMessage[index] = userName[i];
                    index++;
                }
                bytes[index]='0';
                index++;
            }
            return fullMessage;
        }

        if(message instanceof StatAckMessage){
            byte[] AckOpcode=shortToBytes((short)10);
            byte[] statOpcode=shortToBytes((short)8);
            byte[] numPosts=shortToBytes(((StatAckMessage) message).getNumPosts());
            byte[] numFollowers=shortToBytes(((StatAckMessage) message).getNumfollowers());
            byte[] numFollowing=shortToBytes(((StatAckMessage) message).getNumFolowing());
            byte[] fullMessage=new byte[10];
            fullMessage[0]=AckOpcode[0];
            fullMessage[1]=AckOpcode[1];
            fullMessage[2]=statOpcode[0];
            fullMessage[3]=statOpcode[1];
            fullMessage[4]=numPosts[0];
            fullMessage[5]=numPosts[1];
            fullMessage[6]=numFollowers[0];
            fullMessage[7]=numFollowers[1];
            fullMessage[8]=numFollowing[0];
            fullMessage[9]=numFollowing[1];
            return fullMessage;
        }
        if(message instanceof AckFollowMessage) {
            int counter=0;
            int index=6;
            byte[] AckOpcode=shortToBytes((short)10);
            byte[] followOpcode=shortToBytes((short)4);
            byte[] numofUsers=shortToBytes(((AckFollowMessage) message).getNumOfUsrs());
            List<String> users=((AckFollowMessage) message).getUserList();
            for(String user: users){
                counter=counter+user.getBytes().length+1;
            }
            byte[] fullMessage=new byte[6+counter];
            fullMessage[0]=AckOpcode[0];
            fullMessage[1]=AckOpcode[1];
            fullMessage[2]=followOpcode[0];
            fullMessage[3]=followOpcode[1];
            fullMessage[4]=numofUsers[0];
            fullMessage[5]=numofUsers[1];
            for(String user: users){
                byte[] userName=user.getBytes();
                for(int i=0;i<userName.length;i++) {
                    fullMessage[index] = userName[i];
                    index++;
                }
                bytes[index]='0';
                index++;
            }
            return fullMessage;
        }
        if(message instanceof AckMessage){
            byte[] AckOpcode=shortToBytes((short)10);
            byte[] messageOpcode=shortToBytes(((AckMessage) message).getMsgOpcode());
            byte[] fullMessage=new byte[4];
            fullMessage[0]=AckOpcode[0];
            fullMessage[1]=AckOpcode[1];
            fullMessage[2]=messageOpcode[0];
            fullMessage[3]=messageOpcode[1];
            return fullMessage;
        }






        if(message instanceof NotificationMessage){
            byte[] notificationOpcode=shortToBytes((short)9);
            char type=(((NotificationMessage) message).getNotoficationType());
            byte[] postingUser=((NotificationMessage) message).getUsername().getBytes();
            byte[] content=((NotificationMessage) message).getContent().getBytes();
            int size=5+content.length+postingUser.length;
            byte[] fullMessage=new byte[size];
            fullMessage[0]=notificationOpcode[0];
            fullMessage[1]=notificationOpcode[1];
            fullMessage[2]=(byte)type;
            int index=3;
            for(int i=0;i<postingUser.length;i++) {
                fullMessage[index] = postingUser[i];
                index++;
            }
            fullMessage[index]='\0';
            index++;
            for(int i=0;i<content.length;i++) {
                fullMessage[index] = content[i];
                index++;
            }
            fullMessage[index]='\0';

            return fullMessage;
        }

        return null;
    }
    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    private short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    private String popString(int start,int end) {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        String result = new String(bytes, start, end, StandardCharsets.UTF_8);
        result=result.substring(0,result.indexOf('\0'));
        //len = 0;
        return result;
    }

}
