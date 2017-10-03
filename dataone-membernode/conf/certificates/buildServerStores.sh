#!/usr/bin/env bash
STOREPASS=$1
#server private
SERVERKEY=$2
SERVERCERT=$3
DATAONECERT=${4:-None}
CNSERVER=${5:-None}
#keystore
sudo openssl pkcs12 -export -inkey $SERVERKEY -in $SERVERCERT -out server.p12 -passout pass:$STOREPASS
keytool -importkeystore -srckeystore server.p12 -srcstoretype pkcs12 -destkeystore serverkeystore.jks -deststoretype jks -storepass password -srcstorepass password
rm -f server.p12

#trust store
if [$CNSERVER -ne "None"]; then
  openssl s_client -showcerts -connect $CNSERVER:443 < /dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > $CNSERVER.crt
  keytool -importcert -file $CNSERVER.crt -alias $CNSERVER -keystore servertruststore.jks -storepass password -noprompt
  rm -f $CNSERVER.crt
fi
if [$CNSERVER -ne "None"]; then
  keytool -import -file $DATAONECERT -alias dataone -keystore servertruststore.jks -storepass password -noprompt
fi
keytool -import -file $SERVERCERT -alias servercert -keystore servertruststore.jks -storepass password -noprompt
