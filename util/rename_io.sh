#!/bin/bash
for file in $@
do
    if [ -f $file ]
    then
        echo "Renaming $file"
        sed -i 's/io_//g' $file
    fi
done