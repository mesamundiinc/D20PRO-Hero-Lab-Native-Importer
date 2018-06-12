package com.mesamundi.d20pro.herolabnative.util;

import org.w3c.dom.Node;

/**
 * Created by Mat on 10/25/2017.
 */
public class ValueParser {
  /**
   * Parses a number string to an {@link Integer}, treating {@code ""} as {@code 0}.
   *
   * @param number the number to parse
   * @return the parsed value
   */
  public static Integer parseInt(String number) {
    if (number == null || number.isEmpty()) return 0;
    if (number.substring(0, 1).equalsIgnoreCase("+"))
      number = number.substring(1);


    return number.isEmpty() ? 0 : Integer.parseInt(number);
  }

  /**
   * Parses a number string to a {@link Short}, treating {@code ""} as {@code 0}.
   *
   * @param number the number to parse
   * @return the parsed value
   */
  public static Short parseShort(String number) {
    return number.isEmpty() ? 0 : Short.parseShort(number);
  }

  public static byte parseByte(Node attr, String name) {
    return Byte.parseByte(attr.getAttributes().getNamedItem(name).getNodeValue());
  }
}
