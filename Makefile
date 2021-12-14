.PHONY: all all-doc clean server client

all: server client all-doc

server:
	javac -d bin -sourcepath src/ src/WinsomeServer/ServerMain.java
client:
	javac -d bin -sourcepath src/ src/WinsomeClient/ClientMain.java
all-doc:
	javadoc -d doc -sourcepath src -package WinsomeClient WinsomeServer
clean:
	rm -fr $(wildcard bin/**/*.class)
