package org.gbif.d1.mn.resource;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

class URLDecoder {

  /**
   * @return The value decoded from UTF-8 form data encoded format
   */
  static String decode(String value) {
    try {
      return java.net.URLDecoder.decode(value, StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      // we are completely hosed if we don't support UTF-8
      throw new IllegalStateException("System does not support UTF-8");
    }
  }
}
