package com.mesamundi.d20pro.herolabnative.arc;

import java.util.function.Consumer;

import org.w3c.dom.Node;

/**
 * Convenience class that extracts a named item from the node's attributes.
 */
public class NamedItemArc {
  private final String key;
  private final Consumer<String> target;

  public NamedItemArc(String key, Consumer<String> target) {
    this.key = key;
    this.target = target;
  }

  public static String namedValue(Node node, String name) {
    Node item = node.getAttributes().getNamedItem(name);
    return null != item ? item.getNodeValue() : "";
  }

  public static void trav(Node node, NamedItemArc... as) {
    for (NamedItemArc a : as) {
      String val = namedValue(node, a.key);
      a.target.accept(val);
    }
  }

  public static NamedItemArc arc(String key, Consumer<String> target) {
    return new NamedItemArc(key, target);
  }
}
