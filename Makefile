.PHONY: all jars doc clean server client runserv runclient

all: server client
jars: all
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
