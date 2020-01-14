//
// Created by tomersiv@wincs.cs.bgu.ac.il on 1/4/19.
//

#include "ReadFromSocket.h"
#include <connectionHandler.h>
#include <mutex>
#include <condition_variable>
using namespace std;
ReadFromSocket::ReadFromSocket(ConnectionHandler *connectionHandler, std::mutex& mutex, std::condition_variable &cv,bool *isOnline) :
        connectionHandler(connectionHandler),mutex(mutex),cv(cv), isOnline(isOnline)  {
}


ConnectionHandler *ReadFromSocket::getHandler() const {
    return connectionHandler;
}

void ReadFromSocket::run() {
    while (*isOnline) {
        char ch[2];
        // Stop when we encounter the null character.
        // Notice that the null character is not appended to the frame string.
        try {
            getHandler()->getBytes(&ch[0], 1);
            getHandler()->getBytes(&ch[1], 1);
            short opcode= getHandler()->bytesToShort(ch);
            if(opcode==11){
                getHandler()->getBytes(&ch[0], 1);
                getHandler()->getBytes(&ch[1], 1);
                int messageOpcode= getHandler()->bytesToShort(ch);
                cout<<"ERROR "<<messageOpcode<<endl;
                cv.notify_all();
            }
            if(opcode==10){
                getHandler()->getBytes(&ch[0], 1);
                getHandler()->getBytes(&ch[1], 1);
                short messageOpcode= getHandler()->bytesToShort(ch);
                if(messageOpcode==1||messageOpcode==2||messageOpcode==3||messageOpcode==5||messageOpcode==6)
                    cout<<"ACK "<<+messageOpcode<<endl;
                if(messageOpcode==3) {
                    *isOnline = false;
                    cv.notify_all();
                    //getHandler()->close();
                }
                if(messageOpcode==4){
                    getHandler()->getBytes(&ch[0], 1);
                    getHandler()->getBytes(&ch[1], 1);
                    short numOfUsers= getHandler()->bytesToShort(ch);
                    string output="ACK 4 "+to_string(numOfUsers);
                    string username="";
                    getHandler()->getBytes(&ch[0], 1);
                    for(int i=0;i<numOfUsers;i++){
                        while(ch[0]!='\0'){
                            username=username+ch[0];
                            getHandler()->getBytes(&ch[0], 1);

                        }
                        output=output+" "+username;
                        username="";
                        if(i!=numOfUsers-1)
                            getHandler()->getBytes(&ch[0], 1);
                    }
                    cout<<output<<endl;
                }
                if(messageOpcode==7){
                    getHandler()->getBytes(&ch[0], 1);
                    getHandler()->getBytes(&ch[1], 1);
                    short numOfUsers= getHandler()->bytesToShort(ch);
                    string output="ACK 7 "+to_string(numOfUsers);
                    string username="";
                    getHandler()->getBytes(&ch[0], 1);
                    for( int i=0;i<numOfUsers;i++){
                        while(ch[0]!=0){
                            username=username+ch[0];
                            getHandler()->getBytes(&ch[0], 1);
                        }
                        output=output+" "+username;
                        username="";
                        if(i!=numOfUsers-1)
                            getHandler()->getBytes(&ch[0], 1);
                    }
                    cout<<output<<endl;
                }
                if(messageOpcode==8){
                    getHandler()->getBytes(&ch[0], 1);
                    getHandler()->getBytes(&ch[1], 1);
                    short numPosts= getHandler()->bytesToShort(ch);
                    getHandler()->getBytes(&ch[0], 1);
                    getHandler()->getBytes(&ch[1], 1);
                    short numFollowers= getHandler()->bytesToShort(ch);
                    getHandler()->getBytes(&ch[0], 1);
                    getHandler()->getBytes(&ch[1], 1);
                    short numFollowing= getHandler()->bytesToShort(ch);
                    cout<<"ACK 8 "<<numPosts<<" "<<numFollowers<<" "<<numFollowing<<endl;
                }
            }
            if(opcode==9) {
                getHandler()->getBytes(&ch[0], 1);
                char type = ch[0];
                getHandler()->getBytes(&ch[0], 1);
                string postingUserAndContent = "";
                string output="";
                if(type=='0')
                    output="NOTIFICATION PM";
                if(type=='1')
                    output="NOTIFICATION PUBLIC";
                for (int i = 0;(unsigned) i < 2; i++) {
                    while (ch[0] != '\0') {
                        postingUserAndContent = postingUserAndContent + ch[0];
                        getHandler()->getBytes(&ch[0], 1);
                    }
                    output=output+" "+postingUserAndContent;
                    postingUserAndContent="";
                    if(i!=1)
                        getHandler()->getBytes(&ch[0], 1);
                }
                cout<<output<<endl;
            }


        } catch (std::exception &e) {
            std::cerr << "recv failed (ERROR: " << e.what() << ')' << std::endl;
            //return false;
        }
        //return true;
    }
    }
