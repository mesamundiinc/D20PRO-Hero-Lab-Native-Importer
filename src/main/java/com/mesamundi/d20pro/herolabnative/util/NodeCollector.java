package com.mesamundi.d20pro.herolabnative.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class NodeCollector {
  private static final Logger lg = Logger.getLogger(NodeCollector.class);

  public static List<Node> collectChildren(Node node, Predicate<Node> filter, short... nodeTypes) {
    List<Node> collected = new ArrayList<>();

    NodeList nodes = node.getChildNodes();
    lg.debug("Iterating over " + nodes.getLength() + " children");
    for (int i = 0, len = nodes.getLength(); i < len; i++) {
      Node item = nodes.item(i);
      short type = item.getNodeType();
      if (null != nodeTypes) {
        boolean hit = false;
        for (short nodeType : nodeTypes)
          if (type == nodeType) {
            hit = true;
            break;
          }
        if (!hit)
          continue;
      }
      if (filter.test(item))
        collected.add(item);
    }

    return collected;
  }
}
