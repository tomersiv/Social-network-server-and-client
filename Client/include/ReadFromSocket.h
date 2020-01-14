//
// Created by tomersiv@wincs.cs.bgu.ac.il on 1/4/19.
//

#ifndef BOOST_ECHO_CLIENT_READFROMSOCKET_H
#define BOOST_ECHO_CLIENT_READFROMSOCKET_H
#include "connectionHandler.h"
#include <stdlib.h>
#include <mutex>
#include <condition_variable>
#include <boost/thread.hpp>
#include <boost/algorithm/string/split.hpp>
#include <boost/algorithm/string/classification.hpp>
class ReadFromSocket {
private:
    ConnectionHandler *connectionHandler;
    std::mutex &mutex;
    std::condition_variable &cv;
    bool *isOnline;

public:
    ReadFromSocket(ConnectionHandler *connectionHandler, std::mutex &mutex, std::condition_variable &cv, bool *isOnline);

    void run();

    ConnectionHandler *getHandler() const;
};










#endif //BOOST_ECHO_CLIENT_READFROMSOCKET_H
