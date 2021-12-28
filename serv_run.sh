#!/bin/bash

make
java -ea -cp ".:bin/:libs/*" Winsome.WinsomeServer.ServerMain "$@"
