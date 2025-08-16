package org.mule.extension.vectors.internal.util;

import static java.lang.System.getProperty;

public class FipsUtils {

  /**
   * 
   * Set of constants pertaining to the FIPS
   *
  */
  public static final String FIPS_SECURITY_MODEL = "fips140-2";
  public static final String PROPERTY_SECURITY_MODEL = "mule.security.model";

  public static boolean isFipsEnabled() {
    return FIPS_SECURITY_MODEL.equals(getProperty(PROPERTY_SECURITY_MODEL));
  }
}
