#!/bin/bash

BASEDIR=certs/hosts
CLIENTCERTDIR=certs/clientcerts
password=`cat password.txt`
rm ./password.txt

create_dir () {
toCreate=$1
if [[ -d "$toCreate" ]]
then
    echo "$toCreate already exists!"
else
	mkdir -p $toCreate;
fi
}


generate_cert () {

commonName=$1
password=$2
dst=$3

openssl genrsa -aes256 -passout pass:$password -out $dst/ca-key.pem 4096

openssl req -new -x509 -days 365 -key $dst/ca-key.pem -sha256 -out $dst/ca.pem -passin pass:$password -subj "/C=SE/ST=Stockholm/L=Stockholm/O=Loadcoder/OU=Team Loadcoder/CN=$commonName"

openssl genrsa -passout pass:$password -out $dst/server-key.pem 4096

openssl req -sha256 -new -key $dst/server-key.pem -out $dst/server.csr -passin pass:$password -subj "/C=SE/ST=Stockholm/L=Stockholm/O=Loadcoder/OU=Team Loadcoder/CN=$commonName"

echo "Exports the certificate to a truststore.jks"
keytool -import -file $dst/ca.pem -alias serverCA -keystore $dst/truststore.jks -storepass $password -noprompt

openssl pkcs12 -export -in $dst/ca.pem -inkey $dst/ca-key.pem -certfile $dst/ca.pem -out $dst/keystore.p12 -passin pass:$password -passout pass:$password

echo subjectAltName = DNS:$commonName,IP:127.0.0.1 >> $dst/extfile.cnf
echo extendedKeyUsage = serverAuth >> $dst/extfile.cnf

openssl x509 -req -days 365 -sha256 -passin pass:$password -in $dst/server.csr -CA $dst/ca.pem -CAkey $dst/ca-key.pem -CAcreateserial -out $dst/server-cert.pem -extfile $dst/extfile.cnf


openssl genrsa -passout pass:$password -out $dst/key.pem 4096


openssl req -passin pass:$password -subj /CN=$commonName -new -key $dst/key.pem -out $dst/client.csr

echo extendedKeyUsage = clientAuth > $dst/extfile-client.cnf

openssl x509 -req -days 365 -sha256 -passin pass:$password -in $dst/client.csr -CA $dst/ca.pem -CAkey $dst/ca-key.pem -CAcreateserial -out $dst/cert.pem -extfile $dst/extfile-client.cnf

echo "Generating Key Certificate p12 file..."
openssl pkcs12 -export -out $dst/clientkeycert.p12 -inkey $dst/key.pem -in $dst/cert.pem -name "ClientAutoCert" -passin pass:$password -passout pass:$password

keytool -importkeystore -srckeystore $dst/clientkeycert.p12 -srcstoretype pkcs12 -destkeystore $dst/clientkeystore.jks -noprompt -storepass $password -srcstorepass $password

echo "Done"
}


create_cert () {
        hosten=$1;
        echo $hosten;
        create_dir $BASEDIR/$hosten;
        generate_cert $hosten $password $BASEDIR/$hosten;
}

move_clientcert () {

	hosten=$1;
	mv $BASEDIR/$hosten/clientkeystore.jks $CLIENTCERTDIR/$hosten.jks
}

create_certs () {
while read p; do
  create_cert $p;
  move_clientcert $p;
done <hosts.txt
}

create_dir $BASEDIR;
create_dir $CLIENTCERTDIR
create_certs;

