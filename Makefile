.PHONY: all doc clean runserv runclient

all: server client
jars: bin/WinsomeServer.class bin/WinsomeClient.class 
	jar cf bin/WinsomeServer/*.class
	jar cf bin/WinsomeClient/*.class
server: $(wildcard src/WinsomeServer/*.java)
	javac -d bin -cp "libs/*" -sourcepath src/ \
		src/WinsomeServer/*.java
client: $(wildcard src/WinsomeClient/*.java)
	javac -d bin -cp "libs/*" -sourcepath src/ src/WinsomeClient/ClientMain.java
doc:
	javadoc -private -d doc -link https://docs.oracle.com/en/java/javase/11/docs/api/ \
	   	-sourcepath src -cp ".:libs/*" WinsomeClient WinsomeServer

clean:
	rm -fr $(wildcard bin/**/*.class)
