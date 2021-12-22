#!/bin/bash

# Script di utilità per la creazione/rimozione dei blog di tutti gli utenti
if [ $# -eq 0 ]; then
    echo "Usage: $(basename $0) [rm|touch]"
    exit 1
fi

if [[ ! -d data/WinsomeServer/blogs ]]; then
    mkdir -p data/WinsomeServer/blogs
fi
if [[ -r data/WinsomeServer/users.json ]]; then 
for user in \
    $(cat data/WinsomeServer/users.json | grep "username" | cut -d ':' -f 2|tr -d \",);
    do $1 data/WinsomeServer/blogs/$user.json;
    done
else
    echo "File utenti inesistente, nessuna azione"
fi

