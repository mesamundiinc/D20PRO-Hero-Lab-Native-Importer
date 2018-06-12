package com.mesamundi.d20pro.herolabnative.ext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mesamundi.d20pro.herolabnative.ChildrenExtractor;
import com.mindgene.d20.common.creature.CreatureTemplate;
import com.mindgene.d20.common.item.ItemTemplate;
import com.sengent.common.control.exception.UserVisibleException;

import static com.d20pro.plugin.api.XMLToDocumentHelper.xpath;
import static com.mesamundi.d20pro.herolabnative.HeroLabNativeImportLogic.characterPath;
import static com.mesamundi.d20pro.herolabnative.arc.NamedItemArc.*;
import static java.lang.String.format;

/**
 * Extractor for Gear and Magic Items, both of which are items in D20PRO.
 */
public class ExtractGear extends ChildrenExtractor {
  private static final Logger lg = Logger.getLogger(ExtractGear.class);

  public interface C {
    String MAGIC_ITEMS = "magicitems";
    String GEAR = "gear";
    String SUFFIX = "/item";
  }


  public ExtractGear(String subpath) {
    super(subpath + C.SUFFIX);
  }

  @Override
  public void accept(Node node, CreatureTemplate ctr) {
    ItemTemplate item = buildItem(node, ctr);

    lg.debug("Imported item: " + item);
    ctr.getItems().addItem(item);
  }

  private static ItemTemplate buildItem(Node node, CreatureTemplate ctr)
  {
    ItemTemplate item = new ItemTemplate();

    trav(node,
            arc("name", item::setName),
            arc("quantity", item::assignQuantity)
    );

    Map<String, Consumer<Node>> logic = new HashMap<>();
    logic.put("weight", weight(item));
    logic.put("cost", n -> lg.trace("Ignoring cost"));
    logic.put("description", n -> item.assignInfo(n.getTextContent()));
    logic.put("itempower", itempower(ctr, item));
    logic.put("itemslot", n -> lg.trace("Ignoring item slot"));

    NodeList children = node.getChildNodes();
    for (int i = 0, len = children.getLength(); i < len; i++) {
      Node child = children.item(i);
      String childName = child.getNodeName();
      if (childName.startsWith("#"))
        continue;
      Consumer<Node> target = logic.get(childName);
      if (null != target) {
        target.accept(child);
        lg.trace("Accepted: " + childName);
      } else
        lg.error("Unrecognized child: " + childName);
    }

    return item;
  }

  private static Consumer<Node> weight(ItemTemplate item) {
    return node -> {
      String value = namedValue(node, "value");
      try {
        item.assignWeight(Float.parseFloat(value));
      } catch (NumberFormatException nfe) {
        lg.error(format("Failed to parse weight: %s for item: %s", value, item.getName()));
      }
    };
  }

  private static Consumer<Node> itempower(CreatureTemplate ctr, ItemTemplate item) {
    return node -> {
      String name = namedValue(node, "name");
      // TODO: 10/20/2017 handle itempowers
      String todo = format("Ignoring itempower: %s", name);
      ctr.addToErrorLog(todo);
      lg.warn(todo);
    };
  }

  /**
   * Finds the corresponding item by name match or empty if not found.
   *
   * @param name the name, case-sensitive, of the item to find
   * @param nug  helper data
   * @return the item or none
   */
  static Optional<ItemTemplate> findItem(String name, Nug nug) {
    try {
      // search magic items first
      Optional<ItemTemplate> item = findItem(name, xpath(nug.doc, characterPath(C.MAGIC_ITEMS + C.SUFFIX)), nug);
      // then search normal gear
      if (!item.isPresent())
        item = findItem(name, xpath(nug.doc, characterPath(C.GEAR + C.SUFFIX)), nug);
      return item;
    } catch (UserVisibleException uve)
    {
      lg.error("Failed to findItem", uve);
      return Optional.empty();
    }
  }

  private static Optional<ItemTemplate> findItem(String name, NodeList nodes, Nug nug)
  {
    for (int i = 0; i < nodes.getLength(); i++) {
      ItemTemplate item = buildItem(nodes.item(i), nug.ctr);
      if(name.equals(item.getName()))
        return Optional.of(item);
    }

    return Optional.empty();
  }
}
