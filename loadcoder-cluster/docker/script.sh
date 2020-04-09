#!/bin/bash

echo "Printing file /etc/hosts"
cat /etc/hosts
echo "#############################################################"
echo ""
echo "Printing environment variables"
env
echo "#############################################################"

if [[ -z "${LOADSHIP_HOST}" ]]; then
  LOADSHIP_HOST_TO_USE="master"
  echo "Environment variable LOADSHIP_HOST_TO_USE not found. Defaulting to master"
else
  LOADSHIP_HOST_TO_USE="${LOADSHIP_HOST}"
  echo "Found environment variable LOADSHIP_HOST_TO_USE=$LOADSHIP_HOST_TO_USE"
fi

if [[ -z "${LOADSHIP_PORT}" ]]; then
  LOADSHIP_PORT_TO_USE="master"
  echo "Environment variable LOADSHIP_PORT_TO_USE not found. Defaulting to 6210"
else
  LOADSHIP_PORT_TO_USE="${LOADSHIP_PORT}"
  echo "Found environment variable LOADSHIP_PORT_TO_USE=$LOADSHIP_PORT_TO_USE"
fi

LOADSHIP_URL=http://$LOADSHIP_HOST_TO_USE:$LOADSHIP_PORT_TO_USE/loadship/data
echo "Downloading package from Loadship... at $LOADSHIP_URL"
curl --insecure -X GET $LOADSHIP_URL > foo.zip

echo "Listing everything in current directory..."
ls -la

echo "Unziping..."
unzip foo.zip

echo "Changing Mode for extracted test.sh..."
chmod 755 test.sh

echo "Running extracted test.sh..."
./test.sh

