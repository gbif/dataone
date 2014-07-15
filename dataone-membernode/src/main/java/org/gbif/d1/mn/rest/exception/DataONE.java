package org.gbif.d1.mn.rest.exception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides annotation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DataONE {

  public enum Method {
    CREATE
  };

  public Method value();
}
