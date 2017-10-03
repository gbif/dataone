### The Member Node framework

This is a full tier 4 implementation of the RESTful Member Node API defined on http://mule1.dataone.org/ArchitectureDocs-current/apis/MN_APIs.html.

This implementation allows for plugging in different backends by implementing the backend API.  

When building, this will produce an artifact suitable for extension (e.g. to implement a backend) and also a runnable version which uses an in memory backend.  The in memory backend is only suitable for testing purposes.

To run the in memory backend version:
```
-- from root project
mvn package
cd dataone-membernode
java -jar target/dataone-membernode-0.1-SNAPSHOT-in-memory.jar server conf/config.yml 
```

### Configuration

#### Security
A DataOne Member Node makes use of X509 certificate to expose some of service endpoints on a secure (SSL) connection and also to establish
secure connections with other DataOne nodes (Coordinating and Member nodes); for such tasks the following key stores are required:
  * Keystore: it must contain the host private certificate key used to provide SSL connections.
  * Truststore: contains CA certificates to establish trusted connections and authenticate users against known certificates.
  
##### Server key stores configuration
 The server key store must contain the certificate private key to provide SSL connections, only the root/intermediate certificate and the server certificate are needed to be in this store; on the other hands, the server trust store must contain the server certificate, the DataOne root/intermediate, client and Coordinating Node certificates. The script [buildServerStores.sh](conf/certificates/buildServerStores.sh) 
 can be used to build both server stores, for example:
  `./buildServerStores.sh password server_private.key server_bundle.pem DataONEIntCA.pem cn.dataone.org`, which usage is (parameters are positional):
  - *password*: password used to create and access both key stores.
  - *server_private.key*: server certificate private key
  - *server_bundle.pem*: server certificate or bundle certificate (intermediate + server certificates), along with the  server private key are copied into the server key store and are used to provide SSL connections.
  - *DataONEIntCA.pem* (optional): DataOne root or intermediate certificate, all connections issued using this certificate will be trusted, therefore it is copied into the server trust store.
  - *cn.dataone.org* (optional): DataOne Coordinating Node server name, it is contacted to download its certificate public key which is stored in the server trust store.
  
  The YAML file (*datarepo_config.yml*)[conf/datarepo_config.yml#L63] contains a example of server stores configuration 

##### Client Truststore configuration
A managed Jersey client is used to establish secure connections between this MemberNode and other DataOne Coordinating/Member nodes.
A private key certificate to identify this MemberNode is provided by the DataOne infrastructure, such certificate must be copied into both key and trust stores used by the Jersey client. Additionally, the DataOne root/intemediate certificates and Coordinating Nodes certificartes must be copied into the client truststore.
The script [`buildClientStores.sh`](conf/certificates/buildClientStores.sh) can be used to generate the client certificate stores, for example: 
`./buildClientStores.sh password DataOneMemberNode_private_key.pem DataOneMemberNode_private_key.crt cn-stage.test.dataone.org DataONETestIntCA.pem`
    - `password`: password used for both *client* keystore and *client* truststore.
    - `DataOneMemberNode_private_key.pem`: private certificate key provided by DataOne as the member node certificate (this copied into the keystore).
    - `DataOneMemberNode_private_key.crt`: certificate provided by DataOne as the member node certificate (this is copied into the truststore).
    - `cn-stage.test.dataone.org`: Coordinating Node server, this server is contacted to download its certificate public key which is stored in the server trust store.
 
 The YAML file (*datarepo_config.yml*)[conf/datarepo_config.yml#L85] contains a configuration example of a managed client certificate stores. 

###### GBIF DataONE test environment
  To configure GBIF DataOne MemberNode please consult the documentation at this[link](https://github.com/gbif/deploy/tree/master/dataone-membernode).(This is private repository)   

#### Test local environment 
- The [example configuration](conf/README.md) contains the steps run a local instance (not-registered into DataOne).
