#!/bin/bash

#curl --insecure --data-binary @hello.zip -H "Content-type: application/octet-stream; charset=utf-8" -X POST https://localhost:6010/testcache/zip

#curl --insecure -d "testrunner" -H "Content-type: application/octet-stream; charset=utf-8" -X POST https://10.96.0.2:8490/testcache/zip

curl --insecure -X GET http://master:6210/loadship/data > foo.zip

ls -la

unzip foo.zip

#mvn test

chmod 755 test.sh

./test.sh

