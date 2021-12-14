.PHONY: all doc clean server client runserv runclient

all: server client

server:
	javac -d bin -cp "libs/*" -sourcepath src/ \
		src/WinsomeServer/*.java
client:
	javac -d bin -cp "libs/*" -sourcepath src/ src/WinsomeClient/ClientMain.java
doc:
	javadoc -d doc \
	   	-sourcepath src -cp ".:libs/*" WinsomeClient WinsomeServer

clean:
	rm -fr $(wildcard bin/**/*.class)
