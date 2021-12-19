.PHONY: all jars doc clean runserv runclient

all: bin/WinsomeServer/ServerMain.class bin/WinsomeClient/ClientMain.class 
jars: bin/WinsomeServer.jar bin/WinsomeClient.jar
runserv: bin/WinsomeServer/ServerMain.class
	java -cp ".:bin/:libs/*" WinsomeServer.ServerMain
runclient: bin/WinsomeClient/ClientMain.class
	java -cp ".:bin/:libs/*" WinsomeClient.ClientMain
bin/WinsomeServer.jar: bin/WinsomeServer/ServerMain.class 
	jar cvfe bin/WinsomeServer.jar WinsomeServer.ServerMain \
		-C bin WinsomeServer \
		-C bin WinsomeExceptions \
		-C bin WinsomeRequests \
		-C bin WinsomeTasks
bin/WinsomeClient.jar: bin/WinsomeClient/ClientMain.class
	jar cvfe bin/WinsomeClient.jar WinsomeClient.ClientMain \
		-C bin WinsomeClient \
		-C bin WinsomeExceptions \
		-C bin WinsomeRequests \
		-C bin WinsomeTasks
bin/WinsomeServer/ServerMain.class: \
	src/WinsomeServer/*.java \
	src/WinsomeRequests/*.java \
	src/WinsomeTasks/*.java \
	src/WinsomeExceptions/*.java
	javac -d bin -cp "libs/*" -sourcepath src/ \
		src/WinsomeServer/*.java	
bin/WinsomeClient/ClientMain.class: \
	src/WinsomeClient/*.java \
	src/WinsomeRequests/*.java \
	src/WinsomeExceptions/*.java
	javac -d bin -cp "libs/*" -sourcepath src/ \
		src/WinsomeClient/*.java 
doc:
	javadoc -private -d doc \
		-link https://docs.oracle.com/en/java/javase/11/docs/api/ \
		-link http://fasterxml.github.io/jackson-core/javadoc/2.9/ \
		-link http://fasterxml.github.io/jackson-databind/javadoc/2.9/ \
		-link https://commons.apache.org/proper/commons-cli/apidocs/ \
	   	-sourcepath src -cp ".:libs/*" WinsomeClient WinsomeServer

clean:
	rm -fr bin/* 
