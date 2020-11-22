FROM maven:3.6.3-jdk-11-openj9

WORKDIR /root/

ADD script.sh .
RUN chmod 755 script.sh
RUN apt-get update ; 
RUN apt-get install zip -y

CMD ["/root/script.sh"]

