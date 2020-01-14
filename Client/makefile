CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -pthread

all: Client
	g++ -o bin/BGSclient bin/connectionHandler.o bin/echoClient.o bin/ReadFromKeyboard.o bin/ReadFromSocket.o $(LDFLAGS)

Client: bin/connectionHandler.o bin/echoClient.o bin/ReadFromKeyboard.o bin/ReadFromSocket.o
	
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/echoClient.o: src/echoClient.cpp
	g++ $(CFLAGS) -o bin/echoClient.o src/echoClient.cpp

bin/ReadFromKeyboard.o: src/ReadFromKeyboard.cpp
	g++ $(CFLAGS) -o bin/ReadFromKeyboard.o src/ReadFromKeyboard.cpp

bin/ReadFromSocket.o: src/ReadFromSocket.cpp
	g++ $(CFLAGS) -o bin/ReadFromSocket.o src/ReadFromSocket.cpp
	
.PHONY: clean
clean:
	rm -f bin/*
