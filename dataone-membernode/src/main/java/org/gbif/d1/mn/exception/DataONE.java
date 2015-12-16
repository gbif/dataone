package org.gbif.d1.mn.exception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the function within the DataONE specification that the method performs.
 * <p>
 * Exception handling makes use of this in order to prefix detailCodes in exceptional circumstances to reduce the amount
 * of state (e.g. the detailCode) that needs passed between methods.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DataONE {

  enum Method {
    PING, GET_LOG_RECORDS, GET_CAPABILITIES, GET, GET_SYSTEM_METADATA, DESCRIBE, GET_CHECKSUM, LIST_OBJECTS,
    SYNCHRONIZATION_FAILED, SYSTEM_METADATA_CHANGED, GET_REPLICA, IS_AUTHORIZED, CREATE, UPDATE, GENERATE_IDENTIFIER,
    DELETE, ARCHIVE, UPDATE_SYSTEM_METADATA, REPLICATE, QUERY, GET_QUERY_ENGINE_DESCRIPTION, LIST_QUERY_ENGINES, VIEW,
    LIST_VIEWS, GET_PACKAGE
  };

  Method value();
}
