#!/bin/bash
 
full_path=$(realpath $0)
 
dir_path=$(dirname $full_path)
echo "Absolute path to the dir where this script exists: $dir_path"


sudo docker stop loadship loadcoder-0
sudo docker rm loadship loadcoder-0

cd ../../../loadship/docker

sudo ./buildBaseAndLoadcoderImage.sh

cd -
mvn clean install
mvn exec:java -Dexec.mainClass=Controller -Dexec.args="$dir_path"

