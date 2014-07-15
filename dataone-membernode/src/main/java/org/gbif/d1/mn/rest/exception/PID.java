package org.gbif.d1.mn.rest.exception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated parameter represents a PID in DataONE.
 * <p>
 * <strong>Only one instance of the PID is allowed to be marked per method.</strong>
 * <p>
 * This class is used to extract information necessary to serialize exceptions.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PID {
}
