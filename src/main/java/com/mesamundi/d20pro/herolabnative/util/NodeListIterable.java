package com.mesamundi.d20pro.herolabnative.util;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by Mat on 10/25/2017.
 */
public class NodeListIterable implements Iterable<Node> {
  private static final Logger lg = Logger.getLogger(NodeListIterable.class);

  private final NodeListIterator iterator;

  public NodeListIterable(Node parent) {
    iterator = new NodeListIterator(parent);
  }

  @Override
  public Iterator<Node> iterator() {
    return iterator;
  }

  public static class NodeListIterator implements Iterator<Node> {

    private final NodeList children;

    private int i;
    private final int len;

    public NodeListIterator(Node parent) {
      children = parent.getChildNodes();

      i = 0;
      len = children.getLength();
    }

    @Override
    public boolean hasNext() {
      return i < len;
    }

    @Override
    public Node next() {
      return children.item(i++);
    }
  }

  public static void iterateOverChildren(Node node, Predicate<Node> filter, Consumer<Node> logic) {
    NodeList nodes = node.getChildNodes();
    lg.debug("Iterating over " + nodes.getLength() + " children");
    for (int i = 0, len = nodes.getLength(); i < len; i++) {
      Node item = nodes.item(i);
      if (filter.test(item))
        logic.accept(item);
    }
  }
}
