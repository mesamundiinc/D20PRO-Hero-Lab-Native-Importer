package com.mesamundi.d20pro.herolabnative.ext;

import java.util.Map;
import java.util.function.Function;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.mesamundi.d20pro.herolabnative.Extractor;
import com.mesamundi.d20pro.herolabnative.util.ValueParser;
import com.mindgene.d20.common.Rules;

import static com.d20pro.plugin.api.XMLToDocumentHelper.peekMapData;
import static com.d20pro.plugin.api.XMLToDocumentHelper.xpath;
import static com.mesamundi.d20pro.herolabnative.HeroLabNativeImportLogic.characterPath;

public class ExtractArmorClass implements Extractor {
  private static final Logger lg = Logger.getLogger(ExtractArmorClass.class);

  @Override
  public void extract(Nug nug) throws Exception {
    Node node = xpath(nug.doc, characterPath("armorclass")).item(0);
    Map<String, String> map = peekMapData(node.getAttributes());

    String gamesystem = Rules.getInstance().getSYSTEM();
    lg.error("Found gamesystem: " + gamesystem);
    Function<String, Integer> pluck = attr -> ValueParser.parseInt(map.get(attr));
    int fromarmor = pluck.apply("fromarmor");
    if (gamesystem.equalsIgnoreCase("5e"))
      fromarmor += 10;

    int fromdex = pluck.apply("fromdexterity");
    int fromsize = pluck.apply("fromsize");
    int unarmored = pluck.apply("unarmored");
    int fromshield = pluck.apply("fromshield");
    int fromnatural = pluck.apply("fromnatural");
    int fromdeflect = pluck.apply("fromdeflect");
    int fromdodge = pluck.apply("fromdodge");
    int frommisc = pluck.apply("frommisc");


    // TODO: 10/25/2017 inject armor class
    byte[] ac = new byte[]{
            (byte) fromnatural,
            (byte) fromarmor,
            (byte) fromshield,
            (byte) fromdeflect,
            (byte) frommisc,
            (byte) fromdodge
    };
    nug.ctr.setAC(ac);
    /* note: maxDexBonus is set in com.mesamundi.d20pro.herolabnative.ext.ExtractPenalties */
  }
}
