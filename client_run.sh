#!/bin/bash

make
java -ea -cp ".:bin/:libs/*" WinsomeClient.ClientMain "$@"
