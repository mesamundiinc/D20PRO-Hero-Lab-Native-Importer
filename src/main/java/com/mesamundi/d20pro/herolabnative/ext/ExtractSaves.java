package com.mesamundi.d20pro.herolabnative.ext;

import org.apache.log4j.Logger;
import org.w3c.dom.NodeList;

import com.mesamundi.common.ObjectCommon;
import com.mesamundi.d20pro.herolabnative.Extractor;
import com.mindgene.d20.common.D20Rules;
import com.mindgene.d20.common.Rules;
import com.mindgene.d20.common.creature.CreatureTemplateModifiers;

import static com.d20pro.plugin.api.XMLToDocumentHelper.xpath;
import static com.mesamundi.d20pro.herolabnative.HeroLabNativeImportLogic.characterPath;

/**
 * Created by Mat on 7/12/2017.
 */
public class ExtractSaves implements Extractor {
  private static final Logger lg = Logger.getLogger(ExtractSaves.class);

  private static byte mapSaveToAttr(byte saveID) {
    switch (saveID) {
      case D20Rules.Save.FORT:
        return D20Rules.Ability.CON;
      case D20Rules.Save.REF:
        return D20Rules.Ability.DEX;
      case D20Rules.Save.WILL:
        return D20Rules.Ability.WIS;
      default:
        throw new UnsupportedOperationException("Unsupported saveID: " + saveID);
    }
  }

  @Override
  public void extract(Nug nug) throws Exception {
    NodeList nodes = xpath(nug.doc, characterPath("saves/save"));

    String[] SAVE_NAMES = (String[]) Rules.getInstance().getFieldValue("Rules.Save.NAMES");
    lg.debug("Saves: " + ObjectCommon.formatArray(SAVE_NAMES, ", "));

    CreatureTemplateModifiers modifiers = nug.ctr.getCreatureTemplateModifiers();
    byte[] saveMods = modifiers.getSaveMods();
    Extractor.nodeListOverNames(nodes, SAVE_NAMES, "abbr", (c, m, i) -> {
      byte saveID = i.byteValue();
      byte mod = 0;
      String fromresist = m.getAttributes().getNamedItem("fromresist").getNodeValue();
      if (!fromresist.isEmpty())
        mod += Byte.parseByte(fromresist);
      String frommisc = m.getAttributes().getNamedItem("frommisc").getNodeValue();
      if (!frommisc.isEmpty())
        mod += Byte.parseByte(frommisc);

      // compare ability bonus and apply any difference
      String fromattr = m.getAttributes().getNamedItem("fromattr").getNodeValue();
      if (!fromattr.isEmpty()) {
        byte attrMod = Byte.parseByte(fromattr);
        byte attrModActual = (byte) nug.ctr.accessAbilityScoreMod(mapSaveToAttr(saveID));
        byte delta = (byte) (attrMod - attrModActual);
        if (delta != 0)
          mod += delta;
      }

      if (mod != 0) {
        byte modv = (byte) (mod - saveMods[saveID]);
        nug.ctr.setSaveMod(saveID, modv, true);
        saveMods[i.byteValue()] = mod;
      }
    }).extract(nug);
  }
}