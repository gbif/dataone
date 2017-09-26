/**
 *
 */
package org.gbif.d1.mn.auth;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;
import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERUTF8String;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.types.v1.Group;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.Subject;
import org.dataone.ns.service.types.v1.SubjectInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities to deal with X509 certificates to produce the represented Session.
 * <p>
 * Methods marked with @VisibleForTesting are not intended to be used outside of this class.
 */
@ThreadSafe
public final class CertificateUtils {

  private static final Logger LOG = LoggerFactory.getLogger(CertificateUtils.class);
  private static final String REQ_X509CERTIFICATE = "javax.servlet.request.X509Certificate";
  private static final JAXBContext SUBJECT_INFO_CONTEXT = initJaxbContext(); // confirmed threadsafe

  public static final Session PUBLIC_SESSION = Session.builder()
                                                  .withSubject(Subject.builder().withValue("public").build())
                                                  .withSubjectInfo(SubjectInfo.builder()
                                                                     .withGroup(Group.builder()
                                                                                  .withGroupName("public").build())
                                                                     .build())
                                                  .build();

  private final List<String> extensionOIDs;

  // not for instantiation by others
  private CertificateUtils(List<String> extensionOIDs) {
    this.extensionOIDs = ImmutableList.copyOf(extensionOIDs);
  }

  /**
   * @return an instance using the {@link AuthorizationManager#DEFAULT_OID_SUBJECT_INFO} only
   */
  public static CertificateUtils newInstance() {
    return new CertificateUtils(ImmutableList.of(AuthorizationManager.DEFAULT_OID_SUBJECT_INFO));
  }

  public static CertificateUtils newInstance(List<String> extensionOIDs) {
    Preconditions.checkNotNull(extensionOIDs, "Extensions OIDs are required");
    return new CertificateUtils(extensionOIDs);
  }

  private static JAXBContext initJaxbContext() {
    try {
      return JAXBContext.newInstance(SubjectInfo.class);
    } catch (JAXBException e) {
      throw Throwables.propagate(e); // we are hosed
    }
  }

  /**
   * Checks whether given X.509 certificate is self-signed.
   */
  public static boolean isSelfSigned(X509Certificate cert)  {
    try {
      // Try to verify certificate signature with its own public key
      PublicKey key = cert.getPublicKey();
      cert.verify(key);
      return true;
    } catch (SignatureException sigEx) {
      // Invalid signature --> not self-signed
      return false;
    } catch (InvalidKeyException keyEx) {
      // Invalid key --> not self-signed
      return false;
    } catch (NoSuchAlgorithmException | NoSuchProviderException | CertificateException ex){
      throw new RuntimeException(ex);
    }
  }


  /**
   * Builds a new session from the given request
   *
   * @param request The HTTP request; must be present and hold a single certificate
   * @return The session
   * @throws InvalidToken Should it be impossible to create a session from the given request
   * @throws NullPointerException If the request is null
   */
  public Session newSession(HttpServletRequest request, boolean optional) {
    Preconditions.checkNotNull(request, "A request must be provided"); // indicates invalid use
    Certificate[] certs = (Certificate[]) request.getAttribute(REQ_X509CERTIFICATE);
    if (certs != null && certs.length == 1) {
      // session subject is the primary principle of the certificate
      X509Certificate x509Cert = (X509Certificate) certs[0];
      LOG.info("Certificate {}", x509Cert);
      return newSession(x509Cert);
    }
    if (certs != null && certs.length > 1) {
      LOG.info("One certificate expected in the request");
      throw new NotAuthorized("One certificate expected in the request, found " + certs.length);
    }
    if (!optional) {
      throw new NotAuthorized("No certificate found in the request");
    }
    LOG.info("No certificate found in the request, default to public session");
    return PUBLIC_SESSION;
  }

  private static SubjectInfo parseSubjectInfo(String subjectInfoAsXMLString) throws JAXBException {
    LOG.debug("SubjectInfo as XML: {}", subjectInfoAsXMLString);
    if (subjectInfoAsXMLString != null) {
      // not strictly required for a StringReader, but good practice
      try (StringReader reader = new StringReader(subjectInfoAsXMLString)) {
        return (SubjectInfo) SUBJECT_INFO_CONTEXT.createUnmarshaller().unmarshal(reader);
      }
    }
    return null;
  }

  /**
   * @see <a
   *      href="http://stackoverflow.com/questions/2409618/how-do-i-decode-a-der-encoded-string-in-java">StackOverflow</a>
   */
  private static DERObject toDERObject(byte[] data) throws IOException {
    try (ASN1InputStream asn1In = new ASN1InputStream(new ByteArrayInputStream(data))) {
      return asn1In.readObject();
    }
  }

  /**
   * Retrieves the extension value given by the object id.
   * Not intended for client use - visible only for testing.
   *
   * @see <a
   *      href="http://stackoverflow.com/questions/2409618/how-do-i-decode-a-der-encoded-string-in-java">StackOverflow</a>
   */
  @VisibleForTesting
  static String getExtension(X509Certificate X509Certificate, String oid) throws IOException {
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

  /**
   * Builds the session from the certificate.
   *
   * @throws InvalidToken Should the extension exist but be unparsable
   */
  @VisibleForTesting
  Session newSession(X509Certificate x509Cert) {
    Session.Builder<Void> session = Session.builder();
    X500Principal principal = x509Cert.getSubjectX500Principal();
    if (principal.equals(x509Cert.getIssuerX500Principal())) {
      throw new NotAuthorized("Self-signed certificates are not allowed");
    }
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
      // last chance to log detail
      LOG.warn("Unable to read extension from certificate (Hint: possible unexpected encoding?)", e);
      throw new InvalidToken("Extension in certificate cannot be decoded");
    }

    if (subjectInfoAsXMLString != null) {
      try {
        SubjectInfo subjectInfo = parseSubjectInfo(subjectInfoAsXMLString);
        session.withSubjectInfo(subjectInfo);
      } catch (Exception e) {
        // last chance to log detail
        LOG.warn("Cannot parse XML for certificate extension into SubjectInfo", e);
        throw new InvalidToken("Extension in certificate for SubjectInfo cannot be parsed as XML");
      }
    }

    return session.build();
  }
}
