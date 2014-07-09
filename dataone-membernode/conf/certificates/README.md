# Certificate generation

This application uses SSL and certificate based authorization.  

This directory contains the configuration required to run in development mode.  Thus there are the following:
1. A certificate for the server to present when requests come in
2. A certificate that the client will send
3. A truststore that server uses as the list of certificates that it should trust - loaded with both the client and the server certificates

The following provides details about how they were generated. 

```
cd certificates
openssl genrsa -des3 -out server.key
[password]

openssl req -new -x509 -key server.key -out server.crt
[defaults for everything, except FQDN which is "localhost"]

openssl pkcs12 -inkey server.key -in server.crt -export -out server.p12
["password"]

keytool -import -file server.crt -alias server -keystore truststore.jks

openssl genrsa -des3 -out client.key
[password]

openssl req -new -x509 -key client.key -out client.crt
[defaults for everything, except FQDN which is "Tim Robertson"]

openssl pkcs12 -inkey client.key -in client.crt -export -out client.p12
["password"]

keytool -import -file client.crt -alias client -keystore truststore.jks
```