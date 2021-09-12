#!/bin/bash

echo "Environment Variables"
env
MODECHOOSER=`env | grep MODECHOOSER`
MODECHOOSERVALUE=`echo $MODECHOOSER | sed -r 's/([A-Z]*)=(.*)/\2/'`
echo $MODECHOOSERVALUE
if [ -z $MODECHOOSERVALUE ]
then
   MODECHOOSERVALUE="WORKER"
fi

if [ "LOADSHIP" = $MODECHOOSERVALUE ]
then
   echo "starting Loadship"
   java -jar loadship.jar
else
   echo "Starting Worker"
   chmod 755 script.sh
   ./script.sh   
fi

echo "Done! Container will exit."
