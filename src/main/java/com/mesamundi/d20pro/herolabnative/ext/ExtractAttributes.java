package com.mesamundi.d20pro.herolabnative.ext;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mesamundi.common.ObjectCommon;
import com.mesamundi.d20pro.herolabnative.Extractor;
import com.mindgene.d20.common.Rules;

import static com.d20pro.plugin.api.XMLToDocumentHelper.xpath;
import static com.mesamundi.d20pro.herolabnative.HeroLabNativeImportLogic.characterPath;
import static com.mesamundi.d20pro.herolabnative.arc.NamedItemArc.namedValue;
import static com.mesamundi.d20pro.herolabnative.util.ValueParser.parseByte;

/**
 * Created by Mat on 7/9/2017.
 */
public class ExtractAttributes implements Extractor {
  private static final Logger lg = Logger.getLogger(ExtractAttributes.class);

  @Override
  public void extract(Nug nug) throws Exception {
    NodeList nodes = xpath(nug.doc, characterPath("abilityscores/abilityscore"));
    if (nodes == null || nodes.getLength() == 0)
      nodes = xpath(nug.doc, characterPath("attributes/attribute"));

    String[] ABILITY_NAMES = peekAbilityFullNames();
    lg.debug("Abilities: " + ObjectCommon.formatArray(ABILITY_NAMES, ", "));
    Extractor.nodeListOverNames(nodes, ABILITY_NAMES, "name", (c, m, i) -> {
      Node attrValue = m.getChildNodes().item(1);
      byte base = parseByte(attrValue, "base");
      byte mod = (byte) (parseByte(attrValue, "modified") - base);
      nug.ctr.setAbilityScoreBase(i.byteValue(), base, false);
      nug.ctr.setAbilityScoreMods(i.byteValue(), mod);
      lg.debug("Set Ability: " + i.byteValue() + " " + base + "/" + mod);
    }).extract(nug);
  }

  static String[] peekAbilityNames() {
    try {
      return (String[]) Rules.getInstance().getFieldValue("Rules.Ability.NAMES");
    } catch (Exception e) {
      return new String[]{"STR", "DEX", "CON", "INT", "WIS", "CHA"};
      //throw new RuntimeException("Failed to peekAbilityNames", e);
    }
  }

  static String[] peekAbilityFullNames() {
    try {
      return (String[]) Rules.getInstance().getFieldValue("Rules.Ability.FULL_NAMES");
    } catch (Exception e) {
      return new String[]{"Strength", "Dexterity", "Constitution", "Intelligence", "Wisdom", "Charisma"};
      //throw new RuntimeException("Failed to peekAbilityNames", e);
    }
  }

  static String peekGameSystem(Document doc) throws Exception {
    NodeList nodes = xpath(doc, characterPath("program/programinfo"));

    String info = namedValue(nodes.item(0), "abilabbreviation");

    if (info.contains("Pathfinder"))
      return "pathfinder";

    if (info.contains("System Reference Document 5") && info.contains("Wizards of the Coast"))
      return "5e";

    return "3.5";
  }
}
