rm -rf target
mkdir target

cp /etc/ssl/openssl.cnf target
echo '[ subject_alt_name ]' >> target/openssl.cnf

echo 'subjectAltName = DNS:master.loadcoder.com, DNS:*, DNS:localhost'>> target/openssl.cnf

openssl req -x509 -nodes -newkey rsa:2048 -config target/openssl.cnf -extensions subject_alt_name -keyout target/private.key -out target/self-signed.pem -subj '/C=se/ST=stockholm/L=stockholm/O=Loadcoder/OU=Loadship/CN=loadcoder.com/emailAddress=info@loadcoder.com' -days 3650

openssl pkcs12 -export -keypbe PBE-SHA1-3DES -certpbe PBE-SHA1-3DES -export -in target/self-signed.pem -inkey target/private.key -name myalias -out target/keystore.p12

#/usr/lib/jvm/openjdk/jdk-11/bin/keytool -importkeystore -destkeystore target/loadship.jks -deststoretype PKCS12 -srcstoretype PKCS12 -srckeystore target/keystore.p12
keytool -importkeystore -destkeystore target/loadship.jks -deststoretype PKCS12 -srcstoretype PKCS12 -srckeystore target/keystore.p12

cp target/loadship.jks ../src/main/resources/





