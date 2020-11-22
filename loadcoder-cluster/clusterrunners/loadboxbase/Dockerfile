FROM maven:3.6.3-jdk-11-openj9

WORKDIR /root/

ADD content_script.sh .
ADD mvn_profile.txt .
ADD mvn_servers.txt .
ADD mvn_local_repository.txt .

RUN chmod 755 content_script.sh ; ./content_script.sh /usr/share/maven/conf/settings.xml mvn_local_repository.txt "<\!-- localRepository" ; ./content_script.sh /usr/share/maven/conf/settings.xml mvn_profile.txt "</profiles>" ; ./content_script.sh /usr/share/maven/conf/settings.xml mvn_servers.txt "</servers>" ;apt-get update ; apt-get install zip -y

CMD ["sleep", "600"]
