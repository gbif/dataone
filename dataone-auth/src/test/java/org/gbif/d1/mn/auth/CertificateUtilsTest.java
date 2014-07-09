package org.gbif.d1.mn.auth;

import java.io.StringReader;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.Nullable;
import javax.security.auth.x500.X500Principal;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.provider.asymmetric.ec.KeyPairGenerator;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.SubjectInfo;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * A suite of tests that use self signed certificates to verify that Sessions are correctly extracted.
 * Note: At time of writing the deprecation of the X509V3CertificateGenerator points at a non existing class so is still
 * required.
 * 
 * @see http://www.bouncycastle.org/wiki/display/JA1/X.509+Public+Key+Certificate+and+Certification+Request+Generation
 */
public class CertificateUtilsTest {

  // constants used by the bouncy castle provider used to construct certificates
  private static final String CERTIFICATE_SIGNATURE_ALGORITHM = "SHA256WithRSAEncryption";
  private static final String BOUNCY_CASTLE_PROVIDER = "BC";

  // keys for the client
  private static KeyPair personalKeyPair;
  // certificate from the certificate authority to sign client requests with
  private static X509Certificate caCertificate;

  /**
   * Builds a new certificate for the given values, self signing unless the optional certificate for the certificate
   * authority is provided. If an optional d1Extension (SubjectInfo as XML) is provided it will be added.
   */
  @SuppressWarnings("deprecation")
  private static X509Certificate newCertificate(String dn, KeyPair pair, @Nullable X509Certificate caCert,
    @Nullable String d1Extension) throws Exception {
    X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
    X500Principal dnName = new X500Principal(dn);
    certGen.setSerialNumber(BigInteger.ONE);
    if (caCert == null) {
      certGen.setIssuerDN(dnName); // self signed
    } else {
      certGen.setIssuerDN(caCert.getSubjectX500Principal());
    }
    certGen.setNotBefore(new Date());
    Calendar expires = Calendar.getInstance();
    expires.add(Calendar.DATE, 1); // tomorrow
    certGen.setNotAfter(expires.getTime());
    certGen.setSubjectDN(dnName);
    certGen.setPublicKey(personalKeyPair.getPublic());
    certGen.setSignatureAlgorithm(CERTIFICATE_SIGNATURE_ALGORITHM);

    if (d1Extension != null) {
      certGen.addExtension(
        new DERObjectIdentifier(AuthorizationManager.DEFAULT_OID_SUBJECT_INFO),
        true, // critical
        new DERUTF8String(d1Extension));
    }

    return certGen.generate(pair.getPrivate(), BOUNCY_CASTLE_PROVIDER);
  }

  /**
   * Sets up some keys used for the various tests.
   */
  @BeforeClass
  public static void setup() throws Exception {
    Security.addProvider(new BouncyCastleProvider());
    // creates a random new key pair each time
    personalKeyPair = KeyPairGenerator.getInstance("RSA", BOUNCY_CASTLE_PROVIDER).generateKeyPair();
    KeyPair caKeyPair = KeyPairGenerator.getInstance("RSA", BOUNCY_CASTLE_PROVIDER).generateKeyPair();
    caCertificate = newCertificate("O=Trustme Inc.", caKeyPair, null, null); // self signed and no extension
  }

  /**
   * Ensure that the expected error is thrown for non XML, nonsense extension.
   */
  @Test
  public void testExtractSubjectInfoFailureNonsense() throws Exception {
    try {
      CertificateUtils.newInstance().newSession("1",
        newCertificate("CN=Tim Robertson", personalKeyPair, caCertificate, "Not even XML"));
      fail("Invalid extensions should raise exception");
    } catch (InvalidToken e) {
      assertEquals("1", e.getDetailCode());
    } catch (Exception e) {
      assertEquals(InvalidToken.class, e.getClass());
    }
  }

  /**
   * Ensure that the expected error is thrown for an invalid extension.
   */
  @Test
  public void testExtractSubjectInfoFailureXML() throws Exception {
    // valid but unexpected XML
    try {
      CertificateUtils.newInstance().newSession("1",
        newCertificate("CN=Tim Robertson", personalKeyPair, caCertificate, "<Test/>"));
      fail("Invalid extensions should raise exception");
    } catch (InvalidToken e) {
      assertEquals("1", e.getDetailCode());
    } catch (Exception e) {
      assertEquals(InvalidToken.class, e.getClass());
    }
  }

  /**
   * This simple test ensures that a certificate with a D1 extension results in the extension being added to the
   * session. Because the extension in the certificate is simply the XML representation of the SubjectInfo, anything
   * more complex would only be testing JAXB marshalling which we assume to work.
   */
  @Test
  public void testExtractSubjectInfoSuccess() throws Exception {
    String extension = Resources.toString(
      Resources.getResource("org/gbif/d1/mn/auth/subjectInfo-1.xml"), Charsets.UTF_8);
    Unmarshaller unmarshaller = JAXBContext.newInstance(SubjectInfo.class).createUnmarshaller();
    SubjectInfo original = (SubjectInfo) unmarshaller.unmarshal(new StringReader(extension));

    String dn = "CN=Tim Robertson";
    Session session =
      CertificateUtils.newInstance().newSession("1", newCertificate(dn, personalKeyPair, caCertificate, extension));
    assertNotNull(session);
    assertNotNull(session.getSubject());
    assertEquals(dn, session.getSubject().getValue());
    assertNotNull(session.getSubjectInfo());
    assertEquals(original, session.getSubjectInfo());
  }

  @Test
  public void testNoSubjectInfo() throws Exception {
    String dn = "CN=Tim Robertson";
    // no extension passed in
    Session session =
      CertificateUtils.newInstance().newSession("1", newCertificate(dn, personalKeyPair, caCertificate, null));
    assertNotNull(session);
    assertNotNull(session.getSubject());
    assertEquals(dn, session.getSubject().getValue());
    assertNull(session.getSubjectInfo());
  }
}
