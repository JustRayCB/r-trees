#!/bin/bash

# MAX=100
# MIN=50
# METHOD="quadratic"
# METHOD="linear"
echo -n "Entrez la valeur MAX: "
read -r MAX
echo -n "Entrez la valeur MIN: "
read -r MIN
echo -n "Entrez la valeur pour la METHOD: "
read -r METHOD
for ((i = 0; i < 10; i++)); do
    make run ARGS="$MAX $MIN $METHOD"
done
