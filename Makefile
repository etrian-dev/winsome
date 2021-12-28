.PHONY: all jars doc clean runserv runclient blogs

all: bin/Winsome/WinsomeServer/ServerMain.class \
   	bin/Winsome/WinsomeClient/ClientMain.class 
jars: bin/Winsome/WinsomeServer.jar bin/Winsome/WinsomeClient.jar
runserv: bin/Winsome/WinsomeServer/ServerMain.class
	java -cp ".:bin/:libs/*" Winsome.WinsomeServer.ServerMain
runclient: bin/Winsome/WinsomeClient/ClientMain.class
	java -cp ".:bin/:libs/*" Winsome.WinsomeClient.ClientMain
bin/Winsome/WinsomeServer.jar: bin/Winsome/WinsomeServer/ServerMain.class 
	jar cvfe bin/WinsomeServer.jar Winsome.WinsomeServer.ServerMain \
		-C bin WinsomeServer \
		-C bin WinsomeExceptions \
		-C bin WinsomeRequests \
		-C bin WinsomeTasks
bin/Winsome/WinsomeClient.jar: bin/WinsomeClient/ClientMain.class
	jar cvfe bin/WinsomeClient.jar WinsomeClient.ClientMain \
		-C bin WinsomeClient \
		-C bin WinsomeExceptions \
		-C bin WinsomeRequests \
		-C bin WinsomeTasks
bin/Winsome/WinsomeServer/ServerMain.class: \
	src/Winsome/WinsomeServer/*.java \
	src/Winsome/WinsomeRequests/*.java \
	src/Winsome/WinsomeTasks/*.java \
	src/Winsome/WinsomeExceptions/*.java
	javac -d bin -cp "libs/*" -sourcepath src/ \
		src/Winsome/WinsomeServer/*.java	
bin/Winsome/WinsomeClient/ClientMain.class: \
	src/Winsome/WinsomeClient/*.java \
	src/Winsome/WinsomeRequests/*.java \
	src/Winsome/WinsomeExceptions/*.java
	javac -d bin -cp "libs/*" -sourcepath src/ \
		src/Winsome/WinsomeClient/*.java 
doc:
	javadoc -private -d doc \
		-link https://docs.oracle.com/en/java/javase/11/docs/api/ \
		-link http://fasterxml.github.io/jackson-core/javadoc/2.9/ \
		-link http://fasterxml.github.io/jackson-databind/javadoc/2.9/ \
		-link https://commons.apache.org/proper/commons-cli/apidocs/ \
	   	-sourcepath src -cp ".:libs/*" \
		-subpackages Winsome

clean:
	rm -fr bin/* 
