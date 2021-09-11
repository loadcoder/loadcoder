cd loadcoder

mvn install:install-file -Dfile=loadcoder-cluster-1.0.0-SNAPSHOT.jar -DgroupId=com.loadcoder -DartifactId=loadcoder-cluster -Dversion=1.0.0-SNAPSHOT -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=loadcoder-core-1.0.0-SNAPSHOT.jar -DgroupId=com.loadcoder -DartifactId=loadcoder-core -Dversion=1.0.0-SNAPSHOT -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=loadcoder-utilities-1.0.0-SNAPSHOT.jar -DgroupId=com.loadcoder -DartifactId=loadcoder-utilities -Dversion=1.0.0-SNAPSHOT -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=loadcoder-network-1.0.0-SNAPSHOT.jar -DgroupId=com.loadcoder -DartifactId=loadcoder-network -Dversion=1.0.0-SNAPSHOT -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=loadcoder-logback-1.0.0-SNAPSHOT.jar -DgroupId=com.loadcoder -DartifactId=loadcoder-logback -Dversion=1.0.0-SNAPSHOT -Dpackaging=jar -DgeneratePom=true

cd ..
mvn clean package
mvn exec:java -Dexec.mainClass=LoadTest -Dexec.args=""

exit $?



