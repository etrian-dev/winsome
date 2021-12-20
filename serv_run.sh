#!/bin/bash

make
java -cp ".:bin/:libs/*" WinsomeServer.ServerMain "$@"
