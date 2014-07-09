package org.dataone.ns.service.exceptions;

/**
 * Codes are defined in the specification per method.
 * TODO:
 */
public class DetailCodes {

  // GET /isAuthorized/{pid}?action={action}
  public static final String IS_AUTHORIZED_SERVICE_FAILURE = "1760";
  public static final String IS_AUTHORIZED_NOT_IMPLEMENTED = "1780";
  public static final String IS_AUTHORIZED_NOT_FOUND = "1800";
  public static final String IS_AUTHORIZED_NOT_AUTHORIZED = "1820";
  public static final String IS_AUTHORIZED_INVALID_TOKEN = "1840";
  public static final String IS_AUTHORIZED_INVALID_REQUEST = "1761";
}
