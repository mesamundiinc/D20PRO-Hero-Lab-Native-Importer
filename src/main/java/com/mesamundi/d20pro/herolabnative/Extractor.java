package com.mesamundi.d20pro.herolabnative;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.d20pro.plugin.api.CreatureImportServices;
import com.mindgene.d20.common.creature.CreatureTemplate;
import com.mindgene.d20.dm.DM;

/**
 * Created by Mat on 7/8/2017.
 */
public interface Extractor {
  final class Nug {
    public final CreatureTemplate ctr;
    public final CreatureImportServices svc;
    public final Document doc;

    public Nug(CreatureTemplate ctr, CreatureImportServices svc, Document doc) {
      this.ctr = ctr;
      this.svc = svc;
      this.doc = doc;
    }

    public final DM dm()
    {
      return (DM) svc.accessImageService();
    }
  }

  void extract(Nug nug) throws Exception;

  static void extract(Nug nug, Extractor... extractors) throws Exception {
    for (Extractor extractor : extractors) {
      extractor.extract(nug);
    }
  }

  static void extract(Nug nug, List<Extractor> extractors) throws Exception {
    for (Extractor extractor : extractors) {
      extractor.extract(nug);
    }
  }

  interface TriConsumer<T, U, V> {
    void accept(T t, U u, V v);
  }

  static Extractor nodeListOverNames(NodeList nodes, String[] names, String key, TriConsumer<CreatureTemplate, Node, Integer> logic) {
    return nug -> {
      if (nodes.getLength() <= 0) return;
      for (int i = 0, len = nodes.getLength(); i < len; i++) {
        Node item = nodes.item(i);
        NamedNodeMap attr = item.getAttributes();
        String val = attr.getNamedItem(key).getNodeValue();

        for (byte j = 0; j < names.length; j++) {
          if (val.toUpperCase().startsWith(names[j].toUpperCase())) {
            logic.accept(nug.ctr, item, (int) j);
          }
        }
      }
    };
  }
}
