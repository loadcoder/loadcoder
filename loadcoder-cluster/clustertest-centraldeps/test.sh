#!/bin/bash
cp settings.xml /usr/share/maven/conf/settings.xml
#sleep 600
mvn -Dtest=InfluxReportTest -Dconfiguration=loadcoder_cluster.conf test > /root/docker_mavenrepo/$LOADCODER_CLUSTER_INSTANCE_ID.log

