/**
 * 
 */
package org.gbif.d1.mn.auth;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;
import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Closer;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERUTF8String;
import org.dataone.ns.service.exceptions.InvalidCredentials;
import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.Subject;
import org.dataone.ns.service.types.v1.SubjectInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities to deal with X509 certificates to produce the represented Session.
 * This functionality is core to the authorization model and hence has highly restricted access.
 * Methods marked with @VisibleForTesting are not intended to be used outside of this class.
 */
@ThreadSafe
final class CertificateUtils {

  private static final Logger LOG = LoggerFactory.getLogger(CertificateUtils.class);
  private static final String HTTPS_SCHEME = "https";
  private static final String REQ_X509Certificate = "javax.servlet.request.X509Certificate";
  private static final JAXBContext SUBJECT_INFO_CONTEXT = initJaxbContext(); // confirmed threadsafe

  private final List<String> extensionOIDs;

  // not for instantiation by others
  private CertificateUtils(List<String> extensionOIDs) {
    this.extensionOIDs = ImmutableList.copyOf(extensionOIDs);
  }

  private static JAXBContext initJaxbContext() {
    try {
      return JAXBContext.newInstance(SubjectInfo.class);
    } catch (JAXBException e) {
      throw Throwables.propagate(e); // we are hosed
    }
  }

  /**
   * @return an instance using the {@link AuthorizationManager.DEFAULT_OID_SUBJECT_INFO} only
   */
  static CertificateUtils newInstance() {
    return new CertificateUtils(ImmutableList.of(AuthorizationManager.DEFAULT_OID_SUBJECT_INFO));
  }

  static CertificateUtils newInstance(List<String> extensionOIDs) {
    Preconditions.checkNotNull(extensionOIDs, "Extensions OIDs are required");
    return new CertificateUtils(extensionOIDs);
  }

  /**
   * Retrieves the extension value given by the object id.
   * Not intended for client use - visible only for testing.
   * 
   * @see http://stackoverflow.com/questions/2409618/how-do-i-decode-a-der-encoded-string-in-java
   */
  @VisibleForTesting
  String getExtension(X509Certificate X509Certificate, String oid) throws IOException {
    String decoded = null;
    byte[] extensionValue = X509Certificate.getExtensionValue(oid);
    if (extensionValue != null) {
      DERObject derObject = toDERObject(extensionValue);
      if (derObject instanceof DEROctetString) {
        DEROctetString derOctetString = (DEROctetString) derObject;
        derObject = toDERObject(derOctetString.getOctets());
        if (derObject instanceof DERUTF8String) {
          DERUTF8String s = DERUTF8String.getInstance(derObject);
          decoded = s.getString();
        }
      }
    }
    return decoded;
  }

  final Session newSession(HttpServletRequest request, String detailCode) throws InvalidRequest, InvalidCredentials,
    InvalidToken {
    Preconditions.checkNotNull(request, "A request must be provided"); // indicates invalid use

    if (HTTPS_SCHEME.equals(request.getScheme().toLowerCase())) {
      Certificate[] certs = (Certificate[]) request.getAttribute(REQ_X509Certificate);

      if (certs != null && certs.length == 1) {
        // session subject is the primary principle of the certificate
        X509Certificate x509Cert = (X509Certificate) certs[0];
        return newSession(detailCode, x509Cert);

      } else if (certs != null && certs.length > 1) {
        throw new InvalidCredentials("One certificate expected in the request, found " + certs.length, detailCode);
      } else {
        throw new InvalidCredentials("No certificate found in the request", detailCode);
      }

    } else {
      throw new InvalidRequest("Https is required for all authentication", detailCode);
    }
  }

  final Session newSession(String detailCode, X509Certificate x509Cert) throws InvalidToken {
    Session.Builder<Void> session = Session.builder();
    X500Principal principal = x509Cert.getSubjectX500Principal();
    String dn = principal.getName(X500Principal.RFC2253); // LDAPv3 format
    Subject subject = Subject.builder().withValue(dn).build();
    session.withSubject(subject);

    // extract extension data from certificate if provided and build SubjectInfo
    String subjectInfoAsXMLString = null;
    try {
      // Extract the extension by order of preference, using the first found
      // Only one OID is used at the time of writing, but this is written to be future-proof
      for (String extensionOID : extensionOIDs) {
        subjectInfoAsXMLString = getExtension(x509Cert, extensionOID);
        if (subjectInfoAsXMLString != null) {
          break;
        }
      }

    } catch (IOException e) {
      // last chance to log detail [warn only as might not indicate error if service is being misused by clients]
      LOG.warn("Unable to read extension from certificate (Hint: possible unexpected encoding?)", e);
      throw new InvalidToken("Extension in certificate cannot be decoded", detailCode);
    }

    if (subjectInfoAsXMLString != null) {
      try {
        SubjectInfo subjectInfo = parseSubjectInfo(subjectInfoAsXMLString);
        session.withSubjectInfo(subjectInfo);
      } catch (Exception e) {
        // last chance to log detail [warn only as might not indicate error if service is being misused by clients]
        LOG.warn("Cannot parse XML for certificate extension into SubjectInfo", e);
        throw new InvalidToken("Extension in certificate for SubjectInfo cannot be parsed as XML", detailCode);
      }
    }

    return session.build();
  }

  private SubjectInfo parseSubjectInfo(String subjectInfoAsXMLString) throws IOException, JAXBException {
    LOG.debug("SubjectInfo as XML: {}", subjectInfoAsXMLString);
    if (subjectInfoAsXMLString != null) {
      Unmarshaller unmarshaller = SUBJECT_INFO_CONTEXT.createUnmarshaller();
      // not strictly required for a StringReader, but good practice
      Closer closer = Closer.create();
      try {
        StringReader reader = closer.register(new StringReader(subjectInfoAsXMLString));
        SubjectInfo subjectInfo = (SubjectInfo) unmarshaller.unmarshal(reader);
        return subjectInfo;

      } catch (Throwable e) { // required for Closer
        throw closer.rethrow(e);
      } finally {
        closer.close();
      }
    }
    return null;
  }

  /**
   * @see http://stackoverflow.com/questions/2409618/how-do-i-decode-a-der-encoded-string-in-java
   */
  private DERObject toDERObject(byte[] data) throws IOException {
    Closer closer = Closer.create();
    try {
      ByteArrayInputStream in = closer.register(new ByteArrayInputStream(data));
      ASN1InputStream asn1In = closer.register(new ASN1InputStream(in));
      return asn1In.readObject();
    } catch (Throwable e) { // required for Closer
      throw closer.rethrow(e);
    } finally {
      closer.close();
    }
  }
}
