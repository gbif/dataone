#!/usr/bin/env bash
STOREPASS=$1
MNPEMCERT=$2
MNPUBLICCERT=$3
CNSERVER=${4:-None}
DATAONECERT=${5:-None}
#keystore
openssl pkcs12 -export -out client.p12 -in $MNPEMCERT -passout pass:$STOREPASS
keytool -importkeystore -srckeystore client.p12 -srcstoretype pkcs12 -destkeystore clientkeystore.jks -deststoretype jks -storepass password -srcstorepass password
rm -f client.p12

#trust store
if [$CNSERVER -ne "None"]; then
  openssl s_client -showcerts -connect $CNSERVER:443 < /dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > $CNSERVER.crt
  keytool -importcert -file $CNSERVER.crt -alias $CNSERVER -keystore clienttruststore.jks -storepass password -noprompt
  rm -f $CNSERVER.crt
fi
if [$DATAONECERT -ne "None"]; then
  keytool -import -file $DATAONECERT -alias dataonetest -keystore clienttruststore.jks -storepass password -noprompt
fi
keytool -import -file $MNPUBLICCERT -alias membernodecert -keystore clienttruststore.jks -storepass password -noprompt

