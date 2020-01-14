//
// Created by tomersiv@wincs.cs.bgu.ac.il on 1/4/19.
//

#ifndef BOOST_ECHO_CLIENT_READFROMKEYBOARD_H
#define BOOST_ECHO_CLIENT_READFROMKEYBOARD_H


#include "connectionHandler.h"

class ReadFromKeyboard {
public:
    ReadFromKeyboard(ConnectionHandler *connectionHandler,std::mutex& mutex,std::condition_variable &cv,bool *isOnline);

private:
    ConnectionHandler *connectionHandler;
    std::mutex &mutex;
    std::condition_variable &cv;
    bool *isOnline;
public:
    ConnectionHandler *getHandler() const;
    void run();
    void Terminate();

};


#endif //BOOST_ECHO_CLIENT_READFROMKEYBOARD_H
