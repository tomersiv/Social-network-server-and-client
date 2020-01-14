//
// Created by tomersiv@wincs.cs.bgu.ac.il on 1/4/19.
//

#include "ReadFromKeyboard.h"
#include <condition_variable>


ReadFromKeyboard::ReadFromKeyboard(ConnectionHandler *connectionHandler,std::mutex& mutex, std::condition_variable &cv, bool *isOnline) :
        connectionHandler(connectionHandler), mutex(mutex),cv(cv), isOnline(isOnline)
{

}
void ReadFromKeyboard::run(){
    while (*isOnline) {
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
        std::string line(buf);
        if(line.substr(0,line.find_first_of(' '))=="REGISTER"){
            std::string username;
            std::string password;
            line=line.substr(9);
            username=line.substr(0,line.find_first_of(' '));
            password=line.substr(username.length()+1,line.length());
            char bytes[4+username.length()+password.length()];
            getHandler()->shortToBytes(1,bytes);
            int index=2;
            for(int i=0;(unsigned)i<username.length();i++){
                bytes[index]=username[i];
                index++;
            }
            bytes[index]='\0';
            index++;
            for(int i=0;(unsigned)i<password.length();i++){
                bytes[index]=password[i];
                index++;
            }
            bytes[index]='\0';
             getHandler()->sendBytes(bytes,4+username.length()+password.length());
        }
        if(line.substr(0,line.find_first_of(' '))=="LOGIN"){
            std::string username;
            std::string password;
            line=line.substr(6);
            username=line.substr(0,line.find_first_of(' '));
            password=line.substr(username.length()+1,line.length());
            char bytes[4+username.length()+password.length()];
            getHandler()->shortToBytes(2,bytes);
            int index=2;
            for(int i=0;(unsigned)i<username.length();i++){
                bytes[index]=username[i];
                index++;
            }
            bytes[index]='\0';
            index++;
            for(int i=0;(unsigned)i<password.length();i++){
                bytes[index]=password[i];
                index++;
            }
            bytes[index]='\0';
            getHandler()->sendBytes(bytes,4+username.length()+password.length());
        }
        if((line.substr(0,line.find_first_of(' '))=="LOGOUT")){
            char bytes[2];
            getHandler()->shortToBytes(3,bytes);
            getHandler()->sendBytes(bytes,2);
            std::unique_lock<std::mutex> lk{mutex};
            cv.wait(lk);
        }
        if((line.substr(0,line.find_first_of(' '))=="POST")){
            std::string content;
            content=line.substr(5,line.length());
            char bytes[3+content.length()];
            getHandler()->shortToBytes(5,bytes);
            int index=2;
            for(int i=0;(unsigned)i<content.length();i++){
                bytes[index]=content[i];
                index++;
            }
            bytes[index]='\0';
            index++;
            getHandler()->sendBytes(bytes,3+content.length());

        }
        if(line.substr(0,line.find_first_of(' '))=="PM"){
            std::string username;
            std::string content;
            line=line.substr(3);
            username=line.substr(0,line.find_first_of(' '));
            content=line.substr(username.length()+1,line.length());
            char bytes[4+username.length()+content.length()];
            getHandler()->shortToBytes(6,bytes);
            int index=2;
            for(int i=0;(unsigned)i<username.length();i++){
                bytes[index]=username[i];
                index++;
            }
            bytes[index]='\0';
            index++;
            for(int i=0;(unsigned)i<content.length();i++){
                bytes[index]=content[i];
                index++;
            }
            bytes[index]='\0';
            getHandler()->sendBytes(bytes,4+username.length()+content.length());
        }
        if((line.substr(0,line.find_first_of(' '))=="USERLIST")){
            char bytes[2];
            getHandler()->shortToBytes(7,bytes);
            getHandler()->sendBytes(bytes,2);
        }
        if((line.substr(0,line.find_first_of(' '))=="STAT")){
            std::string username;
            username=line.substr(5,line.length());
            char bytes[3+username.length()];
            getHandler()->shortToBytes(8,bytes);
            int index=2;
            for(int i=0;(unsigned)i<username.length();i++){
                bytes[index]=username[i];
                index++;
            }
            bytes[index]='\0';
            getHandler()->sendBytes(bytes,3+username.length());

        }

        if(line.substr(0,line.find_first_of(' '))=="FOLLOW"){
            std::string UserNameList;
            char followUnfollow;
            short NumOfUsers;
            char NumOfUsersArr[2];
            followUnfollow=line[7]-48;
            NumOfUsers=(line[9]-48);
            getHandler()->shortToBytes(NumOfUsers,NumOfUsersArr);
            UserNameList=line.substr(11,line.length());
            char bytes[UserNameList.length()+6];
            getHandler()->shortToBytes(4,bytes);
            bytes[2]=followUnfollow;
            bytes[3]=NumOfUsersArr[0];
            bytes[4]=NumOfUsersArr[1];
            int index=5;
            for(int i=0;(unsigned)i<UserNameList.length();i++){
                if(UserNameList[i]==' ') {
                    bytes[index] = '\0';
                    index++;
                }
                else {
                    bytes[index] = UserNameList[i];
                    index++;
                }
            }
            bytes[index]='\0';
            getHandler()->sendBytes(bytes,UserNameList.length()+6);
        }

    }
    }

ConnectionHandler *ReadFromKeyboard::getHandler() const {
    return connectionHandler;
}

void ReadFromKeyboard::Terminate() {
    *isOnline = false;

}