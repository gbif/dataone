# Example configuration 

> The example configuration is suitable only for **testing purposes**.

This configuration starts a server and uses a self signed certificate along with a truststore into which the certificate is loaded under the alias of "server".  In addition to the "server" certificate, a "client" aliased certificate is loaded to the truststore.  As such, the server is configured to authenticate incoming requests that present the client certificate.

To start the server from the project root directory use something along the lines of:
```
  java -jar target/dataone-membernode-0.1-SNAPSHOT-in-memory.jar server conf/config.yml 
```

## See also
- [examples](examples/README.md) for examples of using curl to call the server
- [certificates](certificates/README.md) for instructions on recreating certificates for the server