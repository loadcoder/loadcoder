FROM loadship.base:1.0.0

WORKDIR /root/

ADD script.sh .
ADD loadship.jar .
ADD modechooser.sh .
RUN chmod 755 modechooser.sh

CMD ["/root/modechooser.sh"]
