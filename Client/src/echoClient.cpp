#include <stdlib.h>
#include <connectionHandler.h>
#include <ReadFromKeyboard.h>
#include <ReadFromSocket.h>
#include <thread>
#include <mutex>

/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
//void ThreadGetmessage(ConnectionHandler* con){
//
//    while(true)
//        con->getMessage();
//}

	
	//From here we will see the rest of the ehco client implementation:

//        if (!connectionHandler.sendMessage(line)) {
//            std::cout << "Disconnected. Exiting...\n" << std::endl;
//            break;
//        }
		// connectionHandler.sendLine(line) appends '\n' to the message. Therefor we send len+1 bytes.
       // std::cout<<"Sent " << len+1 << " bytes to server" << std::endl;


 
        // We can use one of three options to read data from the server:
        // 1. Read a fixed number of characters
        // 2. Read a line (up to the newline character using the getline() buffered reader
        // 3. Read up to the null character
       // std::string answer;
        // Get back an answer: by using the expected number of bytes (len bytes + newline delimiter)
        // We could also use: connectionHandler.getline(answer) and then get the answer without the newline char at the end
        //std::thread t(ThreadGetmessage,&connectionHandler);




int main (int argc, char *argv[]) {
    std::string host = argv[1];
    short port = atoi(argv[2]);
    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
    std::mutex mutex;
    std::condition_variable cv;
    bool isOnline = true;
    ReadFromKeyboard *keyboardReader = new ReadFromKeyboard(&connectionHandler,mutex,cv,&isOnline);
    std::thread keyboradThread(&ReadFromKeyboard::run,keyboardReader);


    ReadFromSocket *readFromSocket = new ReadFromSocket(&connectionHandler,mutex,cv,&isOnline);
    std::thread serverThread(&ReadFromSocket::run, readFromSocket);

    serverThread.join();

    keyboradThread.join();


    return 0;
}
