package org.gbif.d1.mn.rest.exception;

import org.gbif.d1.mn.rest.exception.DataONE.Method;

import com.google.common.base.Objects;
import org.dataone.ns.service.exceptions.DataONEException;

public class Key {

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
