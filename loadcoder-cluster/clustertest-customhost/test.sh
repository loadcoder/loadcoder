#!/bin/bash
echo "Inside test.sh"
cp settings.xml /usr/share/maven/conf/settings.xml
#sleep 600

echo "Running mvn..."
mvn -Dtest=CostumHostTest -Dconfiguration=loadcoder_cluster.conf test > /root/docker_mavenrepo/$LOADCODER_CLUSTER_INSTANCE_ID.log

