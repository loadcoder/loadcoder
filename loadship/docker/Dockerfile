FROM loadcoder.base:1.0.0

WORKDIR /root/
ADD target/loadship-1.0.0-SNAPSHOT.jar loadship.jar
ADD docker/script.sh .
ADD docker/modechooser.sh .
RUN chmod 755 modechooser.sh

CMD ["/root/modechooser.sh"]
