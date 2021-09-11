mvn clean package
java -ea -cp target/read-resource-test-1.0.0-SNAPSHOT.jar:../../../loadcoder-utilities/target/loadcoder-utilities-1.0.0-SNAPSHOT.jar ReadResource
#ls -la hello

exit $?



