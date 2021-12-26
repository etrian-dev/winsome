#!/bin/bash

make
java -ea -cp ".:bin/:libs/*" WinsomeServer.ServerMain "$@"
