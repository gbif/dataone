### Example curl calls

This holds some sample calls which illustrate usage using curl.

> Note: curl on mac may need upgrading using homebrew, and then symlink created to use homebrew version using something like:
> ln -s /usr/local/Cellar/curl/7.36.0/bin/curl /usr/bin/curl

```
cd examples

curl --insecure --cert ../certificates/client.p12:password --cert-type p12 -F "pid=10.5072/example-full" -F "object=@sciencemetadata.xml" -F "sysmeta=@sysmeta.xml" https://localhost:8443/mn/v1/object

curl --insecure --cert ../certificates/client.p12:password --cert-type p12 https://localhost:8443/mn/v1/object/10.5072/example-full 

curl --insecure --cert ../certificates/client.p12:password --cert-type p12 https://localhost:8443/mn/v1

curl --insecure --cert ../certificates/client.p12:password --cert-type p12 https://localhost:8443/mn/v1/isAuthorized/XYZ33256?action=READ
```
