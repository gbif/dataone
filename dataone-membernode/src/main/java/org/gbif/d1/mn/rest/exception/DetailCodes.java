package org.gbif.d1.mn.rest.exception;

import org.gbif.d1.mn.rest.exception.DataONE.Method;

import java.util.Map;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import org.dataone.ns.service.exceptions.DataONEException;
import org.dataone.ns.service.exceptions.IdentifierNotUnique;
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.ServiceFailure;

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
    .put(new Key(Method.CREATE, NotAuthorized.class), "1100")
    .put(new Key(Method.CREATE, IdentifierNotUnique.class), "1120")
    .put(new Key(Method.CREATE, ServiceFailure.class), "1190")
    .build();

  /**
   * @return the detail code to use for the exception within the exception, or null if not found.
   */
  static String codeFor(DataONE.Method method, Class<? extends DataONEException> exception) {
    return DETAIL_CODES.get(new Key(method, exception));
  }
}