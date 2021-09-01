#!/bin/bash
 
full_path=$(realpath $0)
 
dir_path=$(dirname $full_path)
echo "Absolute path to the dir where this script exists: $dir_path"


sudo docker stop loadship loadship-0
sudo docker rm loadship loadhip-0

cd ../../../loadship/docker

sudo ./automate.sh

cd -
mvn exec:java -Dexec.mainClass=Controller -Dexec.args="$dir_path"

