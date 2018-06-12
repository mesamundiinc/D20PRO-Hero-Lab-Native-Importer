package com.mesamundi.d20pro.herolabnative.ext;

import org.apache.log4j.Logger;
import org.w3c.dom.NodeList;

import com.mesamundi.d20pro.herolabnative.Extractor;
import com.mindgene.d20.common.Rules;

import static com.d20pro.plugin.api.XMLToDocumentHelper.xpath;
import static com.mesamundi.d20pro.herolabnative.HeroLabNativeImportLogic.characterPath;
import static com.mesamundi.d20pro.herolabnative.HeroLabNativeImportLogic.valueFromMap;

/**
 * Created by Mat on 7/8/2017.
 */
public class ExtractMoney implements Extractor {
  private static final Logger lg = Logger.getLogger(ExtractMoney.class);

  @Override
  public void extract(Nug nug) throws Exception {
    String[] types = (String[]) Rules.getInstance().getFieldValue("Rules.Money.NAMES");
    for (String type : types) {
      money(type.toLowerCase()).extract(nug);

    }
    newMoney(nug);
  }

  private static Extractor money(String key) throws Exception {
    byte id = (byte) Rules.getInstance().getFieldValue("Rules.Money." + key.toUpperCase());
    lg.error("Money: " + key + " with id: " + id);
    return valueFromMap("money", key, (c, v) -> c.setMoney(id, Integer.parseInt((v))));
  }

  private void newMoney(Nug nug) throws Exception {
    String[] names = (String[]) Rules.getInstance().getFieldValue("Rules.Money.NAMES");
//        for(int i = 0; i < names.length; i++)
//            names[i] = names[i] + " pieces";

    NodeList nodes = xpath(nug.doc, characterPath("money/coins"));
    Extractor.nodeListOverNames(nodes, names, "abbreviation", (c, m, i) -> {
      String base = m.getAttributes().getNamedItem("count").getNodeValue();
      lg.error("money : " + base);
      if (!base.isEmpty()) {
        nug.ctr.setSaveBase(i.byteValue(), Byte.parseByte(base), false);
      }
    }).extract(nug);
  }
}
