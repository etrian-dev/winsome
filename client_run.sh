#!/bin/bash

make
java -cp ".:bin/:libs/*" WinsomeClient.ClientMain "$@"
