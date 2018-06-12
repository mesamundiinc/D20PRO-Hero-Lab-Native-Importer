package com.mesamundi.d20pro.herolabnative;

import java.util.function.BiConsumer;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mindgene.d20.common.creature.CreatureTemplate;

import static com.d20pro.plugin.api.XMLToDocumentHelper.xpath;
import static com.mesamundi.d20pro.herolabnative.HeroLabNativeImportLogic.characterPath;

/**
 * An Extractor with a pattern to scan multiple children of the same type (like items) and delegate to the subclass
 * to process them.
 */
public abstract class ChildrenExtractor implements Extractor, BiConsumer<Node, CreatureTemplate> {
  private static final Logger lg = Logger.getLogger(ChildrenExtractor.class);

  protected final String _subpath;

  public ChildrenExtractor(String subpath) {
    this._subpath = subpath;
  }

  @Override
  public final void extract(Nug nug) throws Exception {
    String xpath = characterPath(_subpath);
    NodeList nodes = xpath(nug.doc, xpath);
    int len = nodes.getLength();
    if (len == 0) {
      handleZeroChildren(xpath);
    } else {
      for (int i = 0; i < len; i++) {
        accept(nodes.item(i), nug.ctr);
      }
    }
  }

  protected void handleZeroChildren(String xpath) {
    lg.warn("No children found at xpath: " + xpath);
  }
}
