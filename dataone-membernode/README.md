# The Member Node framework

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

## See also
- The [example configuration](conf/README.md) to understand the configuration with which this will start
