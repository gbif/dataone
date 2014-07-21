package org.gbif.d1.mn.rest.exception;

import org.gbif.d1.mn.rest.exception.DataONE.Method;

import java.util.Map;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import org.dataone.ns.service.exceptions.DataONEException;
import org.dataone.ns.service.exceptions.IdentifierNotUnique;
import org.dataone.ns.service.exceptions.InsufficientResources;
import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidSystemMetadata;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.exceptions.UnsupportedType;

/**
 * Utilities to determine the detailCode for exceptions.
 * <p>
 * This class hides a lot of complexity that would otherwise be scattered around code by providing a simple lookup
 * facility for the required detail code depending on the method of execution. Methods can simple be annotated with
 * {@link DataONE} and then throw {@link DataONEException} without worrying about detail codes.
 */
class DetailCodes {

  /**
   * Container class to improve readability only (removes need for Map of Maps with illegible generics).
   */
  @Immutable
  private static class Key {

    private final Class<? extends DataONEException> exception;
    private final DataONE.Method method;

    private Key(Method method, Class<? extends DataONEException> exception) {
      this.exception = exception;
      this.method = method;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if (obj instanceof Key) {
        Key that = (Key) obj;
        return Objects.equal(this.exception, that.exception)
          && Objects.equal(this.method, that.method);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(exception, method);
    }
  }

  private static final Map<Key, String> DETAIL_CODES = ImmutableMap.<Key, String>builder()

    .put(new Key(Method.PING, NotImplemented.class), "2041")
    .put(new Key(Method.PING, ServiceFailure.class), "2042")
    .put(new Key(Method.PING, InsufficientResources.class), "2045")

    .put(new Key(Method.GET_LOG_RECORDS, NotAuthorized.class), "1460")
    .put(new Key(Method.GET_LOG_RECORDS, InvalidRequest.class), "1480")
    .put(new Key(Method.GET_LOG_RECORDS, ServiceFailure.class), "1490")
    .put(new Key(Method.GET_LOG_RECORDS, InvalidToken.class), "1470")
    .put(new Key(Method.GET_LOG_RECORDS, NotImplemented.class), "1461")

    .put(new Key(Method.GET_CAPABILITIES, NotImplemented.class), "2160")
    .put(new Key(Method.GET_CAPABILITIES, ServiceFailure.class), "2162")

    .put(new Key(Method.GET, NotAuthorized.class), "1000")
    .put(new Key(Method.GET, NotFound.class), "1020")
    .put(new Key(Method.GET, ServiceFailure.class), "1030")
    .put(new Key(Method.GET, InvalidToken.class), "1010")
    .put(new Key(Method.GET, NotImplemented.class), "1001")
    .put(new Key(Method.GET, InsufficientResources.class), "1002")

    .put(new Key(Method.GET_SYSTEM_METADATA, NotAuthorized.class), "1040")
    .put(new Key(Method.GET_SYSTEM_METADATA, NotImplemented.class), "1041")
    .put(new Key(Method.GET_SYSTEM_METADATA, NotFound.class), "1060")
    .put(new Key(Method.GET_SYSTEM_METADATA, ServiceFailure.class), "1090")
    .put(new Key(Method.GET_SYSTEM_METADATA, InvalidToken.class), "1050")

    .put(new Key(Method.DESCRIBE, NotAuthorized.class), "1360")
    .put(new Key(Method.DESCRIBE, NotFound.class), "1380")
    .put(new Key(Method.DESCRIBE, ServiceFailure.class), "1350")
    .put(new Key(Method.DESCRIBE, InvalidToken.class), "1370")
    .put(new Key(Method.DESCRIBE, NotImplemented.class), "1361")

    .put(new Key(Method.GET_CHECKSUM, NotAuthorized.class), "1400")
    .put(new Key(Method.GET_CHECKSUM, NotFound.class), "1420")
    .put(new Key(Method.GET_CHECKSUM, InvalidRequest.class), "1402")
    .put(new Key(Method.GET_CHECKSUM, ServiceFailure.class), "1410")
    .put(new Key(Method.GET_CHECKSUM, InvalidToken.class), "1430")
    .put(new Key(Method.GET_CHECKSUM, NotImplemented.class), "1401")

    .put(new Key(Method.LIST_OBJECTS, NotAuthorized.class), "1520")
    .put(new Key(Method.LIST_OBJECTS, InvalidRequest.class), "1540")
    .put(new Key(Method.LIST_OBJECTS, NotImplemented.class), "1560")
    .put(new Key(Method.LIST_OBJECTS, ServiceFailure.class), "1580")
    .put(new Key(Method.LIST_OBJECTS, InvalidToken.class), "1530")

    .put(new Key(Method.SYNCHRONIZATION_FAILED, NotImplemented.class), "2160")
    .put(new Key(Method.SYNCHRONIZATION_FAILED, ServiceFailure.class), "2161")
    .put(new Key(Method.SYNCHRONIZATION_FAILED, NotAuthorized.class), "2162")
    .put(new Key(Method.SYNCHRONIZATION_FAILED, InvalidToken.class), "2164")

    .put(new Key(Method.SYSTEM_METADATA_CHANGED, NotImplemented.class), "1330")
    .put(new Key(Method.SYSTEM_METADATA_CHANGED, NotAuthorized.class), "1331")
    .put(new Key(Method.SYSTEM_METADATA_CHANGED, InvalidToken.class), "1332")
    .put(new Key(Method.SYSTEM_METADATA_CHANGED, ServiceFailure.class), "1333")
    .put(new Key(Method.SYSTEM_METADATA_CHANGED, InvalidRequest.class), "1334")
    .put(new Key(Method.SYSTEM_METADATA_CHANGED, NotFound.class), "1335") // TODO: verify this is correct behavior

    .put(new Key(Method.GET_REPLICA, NotImplemented.class), "2180")
    .put(new Key(Method.GET_REPLICA, ServiceFailure.class), "2181")
    .put(new Key(Method.GET_REPLICA, NotAuthorized.class), "2182")
    .put(new Key(Method.GET_REPLICA, InvalidToken.class), "2183")
    .put(new Key(Method.GET_REPLICA, InsufficientResources.class), "2184")
    .put(new Key(Method.GET_REPLICA, NotFound.class), "2185")

    .put(new Key(Method.QUERY, InvalidToken.class), "2820")
    .put(new Key(Method.QUERY, ServiceFailure.class), "2821")
    .put(new Key(Method.QUERY, NotAuthorized.class), "2822")
    .put(new Key(Method.QUERY, InvalidRequest.class), "2823")
    .put(new Key(Method.QUERY, NotImplemented.class), "2824")
    .put(new Key(Method.QUERY, NotFound.class), "2825")

    .put(new Key(Method.GET_QUERY_ENGINE_DESCRIPTION, NotImplemented.class), "2810")
    .put(new Key(Method.GET_QUERY_ENGINE_DESCRIPTION, ServiceFailure.class), "2811")
    .put(new Key(Method.GET_QUERY_ENGINE_DESCRIPTION, InvalidToken.class), "2812")
    .put(new Key(Method.GET_QUERY_ENGINE_DESCRIPTION, NotAuthorized.class), "2813")
    .put(new Key(Method.GET_QUERY_ENGINE_DESCRIPTION, NotFound.class), "2814")

    .put(new Key(Method.LIST_QUERY_ENGINES, NotImplemented.class), "2800")
    .put(new Key(Method.LIST_QUERY_ENGINES, ServiceFailure.class), "2801")
    .put(new Key(Method.LIST_QUERY_ENGINES, InvalidToken.class), "2802")
    .put(new Key(Method.LIST_QUERY_ENGINES, NotAuthorized.class), "2803")

    .put(new Key(Method.VIEW, InvalidToken.class), "2840")
    .put(new Key(Method.VIEW, ServiceFailure.class), "2841")
    .put(new Key(Method.VIEW, NotAuthorized.class), "2842")
    .put(new Key(Method.VIEW, InvalidRequest.class), "2843")
    .put(new Key(Method.VIEW, NotImplemented.class), "2844")

    .put(new Key(Method.GET_PACKAGE, InvalidToken.class), "2870")
    .put(new Key(Method.GET_PACKAGE, ServiceFailure.class), "2871")
    .put(new Key(Method.GET_PACKAGE, NotAuthorized.class), "2872")
    .put(new Key(Method.GET_PACKAGE, InvalidRequest.class), "2873")
    .put(new Key(Method.GET_PACKAGE, NotImplemented.class), "2874")
    .put(new Key(Method.GET_PACKAGE, NotFound.class), "2875")

    .put(new Key(Method.IS_AUTHORIZED, ServiceFailure.class), "1760")
    .put(new Key(Method.IS_AUTHORIZED, NotImplemented.class), "1780")
    .put(new Key(Method.IS_AUTHORIZED, NotFound.class), "1800")
    .put(new Key(Method.IS_AUTHORIZED, NotAuthorized.class), "1820")
    .put(new Key(Method.IS_AUTHORIZED, InvalidToken.class), "1840")
    .put(new Key(Method.IS_AUTHORIZED, InvalidRequest.class), "1761")

    .put(new Key(Method.CREATE, NotAuthorized.class), "1100")
    .put(new Key(Method.CREATE, IdentifierNotUnique.class), "1120")
    .put(new Key(Method.CREATE, UnsupportedType.class), "1140")
    .put(new Key(Method.CREATE, InsufficientResources.class), "1160")
    .put(new Key(Method.CREATE, InvalidSystemMetadata.class), "1180")
    .put(new Key(Method.CREATE, ServiceFailure.class), "1190")
    .put(new Key(Method.CREATE, InvalidToken.class), "1110")
    .put(new Key(Method.CREATE, NotImplemented.class), "1101")
    .put(new Key(Method.CREATE, InvalidRequest.class), "1102")

    .put(new Key(Method.UPDATE, NotAuthorized.class), "1200")
    .put(new Key(Method.UPDATE, IdentifierNotUnique.class), "1220")
    .put(new Key(Method.UPDATE, UnsupportedType.class), "1240")
    .put(new Key(Method.UPDATE, InsufficientResources.class), "1260")
    .put(new Key(Method.UPDATE, NotFound.class), "1280")
    .put(new Key(Method.UPDATE, InvalidSystemMetadata.class), "1300")
    .put(new Key(Method.UPDATE, ServiceFailure.class), "1310")
    .put(new Key(Method.UPDATE, InvalidToken.class), "1210")
    .put(new Key(Method.UPDATE, NotImplemented.class), "1201")
    .put(new Key(Method.UPDATE, InvalidRequest.class), "1202")

    .put(new Key(Method.GENERATE_IDENTIFIER, InvalidToken.class), "2190")
    .put(new Key(Method.GENERATE_IDENTIFIER, ServiceFailure.class), "2191")
    .put(new Key(Method.GENERATE_IDENTIFIER, NotAuthorized.class), "2192")
    .put(new Key(Method.GENERATE_IDENTIFIER, InvalidRequest.class), "2193")
    .put(new Key(Method.GENERATE_IDENTIFIER, NotImplemented.class), "2194")

    .put(new Key(Method.DELETE, NotAuthorized.class), "2900")
    .put(new Key(Method.DELETE, NotFound.class), "2901")
    .put(new Key(Method.DELETE, ServiceFailure.class), "2902")
    .put(new Key(Method.DELETE, InvalidToken.class), "2903")
    .put(new Key(Method.DELETE, NotImplemented.class), "2904")

    .put(new Key(Method.ARCHIVE, NotAuthorized.class), "2910")
    .put(new Key(Method.ARCHIVE, NotFound.class), "2911")
    .put(new Key(Method.ARCHIVE, ServiceFailure.class), "2912")
    .put(new Key(Method.ARCHIVE, InvalidToken.class), "2913")
    .put(new Key(Method.ARCHIVE, NotImplemented.class), "2914")

    .put(new Key(Method.UPDATE_SYSTEM_METADATA, NotImplemented.class), "4866")
    .put(new Key(Method.UPDATE_SYSTEM_METADATA, NotAuthorized.class), "4867")
    .put(new Key(Method.UPDATE_SYSTEM_METADATA, ServiceFailure.class), "4868")
    .put(new Key(Method.UPDATE_SYSTEM_METADATA, InvalidRequest.class), "4869")
    .put(new Key(Method.UPDATE_SYSTEM_METADATA, InvalidSystemMetadata.class), "4956")
    .put(new Key(Method.UPDATE_SYSTEM_METADATA, InvalidToken.class), "4957")

    .put(new Key(Method.REPLICATE, NotImplemented.class), "2150")
    .put(new Key(Method.REPLICATE, ServiceFailure.class), "2151")
    .put(new Key(Method.REPLICATE, NotAuthorized.class), "2152")
    .put(new Key(Method.REPLICATE, InvalidRequest.class), "2153")
    .put(new Key(Method.REPLICATE, InsufficientResources.class), "2154")
    .put(new Key(Method.REPLICATE, UnsupportedType.class), "2155")
    .put(new Key(Method.REPLICATE, InvalidToken.class), "2156")

    .build();

  /**
   * @return the detail code to use for the exception within the exception, or null if not found.
   */
  static String codeFor(DataONE.Method method, Class<? extends DataONEException> exception) {
    return DETAIL_CODES.get(new Key(method, exception));
  }
}