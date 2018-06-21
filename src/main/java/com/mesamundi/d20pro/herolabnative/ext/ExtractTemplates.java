package com.mesamundi.d20pro.herolabnative.ext;

import java.util.Optional;

import org.apache.log4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mesamundi.d20pro.herolabnative.Extractor;
import com.mesamundi.d20pro.herolabnative.map.HeroLabNativeMap;
import com.sengent.common.control.exception.UserVisibleException;

import static com.d20pro.plugin.api.XMLToDocumentHelper.xpath;
import static com.mesamundi.d20pro.herolabnative.HeroLabNativeImportLogic.characterPath;
import static com.mesamundi.d20pro.herolabnative.ext.ExtractClasses.addCreatureClass;

/**
 * Extracts a template like "Ogre Mage +8" and adds that many levels of the first type found.
 */
public class ExtractTemplates implements Extractor {
  private static final Logger lg = Logger.getLogger(ExtractTemplates.class);

  @Override
  public void extract(Nug nug) throws Exception {
    Node item = xpath(nug.doc, characterPath("templates")).item(0);
    String summary = item.getAttributes().getNamedItem("summary").getNodeValue();
    if (summary.isEmpty())
      return;

    int at = summary.indexOf('+');
    String name = summary.substring(0, at).trim();
    String notes = "Template: " + name;
    nug.ctr.addToNotes(notes);

    int levels = Integer.parseInt(summary.substring(at + 1));

    if(levels > 0) {
      findType(nug).ifPresent(type -> addCreatureClass(type, levels, nug.ctr, nug.svc));
    }
  }

  private static String remapType(String type)
  {
    switch (type)
    {
      case "Necromancer": return "Necromancy School";
    }
    return type;
  }

  private static Optional<String> findType(Nug nug)
  {
    String path = characterPath("types/type");
    try {
      NodeList tpath = xpath(nug.doc, path);
      if (tpath.getLength() > 0) {
        Node item = tpath.item(0);
        if (item != null) {
          NamedNodeMap attr = item.getAttributes();
          String name = attr.getNamedItem("name").getNodeValue();
          name = remapType(name);
          return Optional.ofNullable(name);
        }
      }
    } catch (UserVisibleException e) {
      lg.error("Failed to xpath: " + path , e);
    }
    return Optional.empty();
  }
}
