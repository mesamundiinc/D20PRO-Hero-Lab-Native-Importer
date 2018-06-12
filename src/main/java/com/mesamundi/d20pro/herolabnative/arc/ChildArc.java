package com.mesamundi.d20pro.herolabnative.arc;

import java.util.Optional;
import java.util.function.Consumer;

import org.w3c.dom.Node;

import com.mesamundi.d20pro.herolabnative.util.NodeListIterable;

/**
 * Convenience class that finds a child by its tag name.
 */
public class ChildArc {
  private final String tagName;
  private final Consumer<Node> target;

  public ChildArc(String tagName, Consumer<Node> target) {
    this.tagName = tagName;
    this.target = target;
  }

  public static Optional<Node> findChild(Node parent, String tagName) {
    for (Node child : new NodeListIterable(parent)) {
      if (tagName.equals(child.getNodeName()))
        return Optional.of(child);
    }

    return Optional.empty();
  }
}
