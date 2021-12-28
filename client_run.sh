#!/bin/bash

make
java -ea -cp ".:bin/:libs/*" Winsome.WinsomeClient.ClientMain "$@"
