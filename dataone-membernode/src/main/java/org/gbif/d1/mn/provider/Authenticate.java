package org.gbif.d1.mn.provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Items that are annotated as @Authenticate indicate that the system should perform authentication steps.  Note, that
 * this does NOT mean that authorization is performed.  D1 is built around certificates and Authentication therefore
 * ensures the Subject can be read from the certificate.  It is then up to the developer to ensure the subject is
 * indeed authorized to proceed with the operation.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Authenticate {
  boolean optional() default true;
}
