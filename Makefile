.PHONY: all all-doc clean server client runserv runclient

all: server client

server:
	javac -d bin -cp "libs/*" -sourcepath src/ \
		src/WinsomeServer/*.java
client:
	javac -d bin -cp "libs/*" -sourcepath src/ src/WinsomeClient/ClientMain.java
all-doc:
	javadoc -d doc -sourcepath src -package WinsomeClient WinsomeServer

clean:
	rm -fr $(wildcard bin/**/*.class)
