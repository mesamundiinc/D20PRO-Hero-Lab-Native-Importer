package com.mesamundi.d20pro.herolabnative.ext.three_x;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.mesamundi.common.ObjectCommon;
import com.mindgene.d20.common.Rules;
import com.mindgene.d20.common.creature.CreatureDamageReduction;
import com.mindgene.d20.common.creature.CreatureTemplate;

import static com.mesamundi.d20pro.herolabnative.HeroLabNativeImportLogic.*;

public class ExtractDamageReduction3x extends AbstractExtractor3x {
  private static final Logger lg = Logger.getLogger(ExtractDamageReduction3x.class);

  private final Set<String> knownDRQualities;

  public ExtractDamageReduction3x() {
    super("damagereduction");
    knownDRQualities = new LinkedHashSet<>();

    try {
      List<String> qualities = (List<String>) Rules.getInstance().getFieldValue("Rules.DR.LIST");
      knownDRQualities.addAll(qualities);
      lg.info("Known qualities: " + ObjectCommon.formatList(qualities));
    } catch (Exception e) {
      lg.error("Failed to load DR list", e);
    }
  }

  @Override
  protected void acceptSpecial(CreatureTemplate ctr, String name, String shortname, String description) {
    ctr.addToNotes(name);

    String[] split = shortname.split(Pattern.quote("/"));

    CreatureDamageReduction dr = ctr.getDamageReduction();
    if(null == dr) {
      dr = new CreatureDamageReduction();
      ctr.setDamageReduction(dr);
    }

    int amount = Integer.parseInt(split[0]);
    dr.setReductionAmount(amount);

    String type = split[1];
    boolean found = false;
    for(String quality : knownDRQualities)
    {
      if(quality.equalsIgnoreCase(type)) {
        lg.debug("Matched " + type + " to " + quality);
        dr.addReductionQuality(quality);
        found = true;
        break;
      }
    }

    if(!found) {
      lg.warn("Quality not found: " + type);
      ctr.addToNotes("DR Quality not recognized: " + type);
    }
  }

  // TODO: 5/20/2018 reincorporate additional mappings
  private static void processDamageReductionOLD(CreatureTemplate ctr, String DR) {
    try {
      int slash = DR.indexOf("/");
      int open = DR.indexOf("(");
      int close = DR.indexOf(")");
      String DRType = DR.substring(slash + 1, close);
      String DRValue = DR.substring(open + 1, slash);

      CreatureDamageReduction theDR = ctr.getDR();
      String quality = AttackQualityFromString(DRType);

      if (theDR == null) {
        // Create new DR
        theDR = new CreatureDamageReduction();
        if (quality != null)
          theDR.addReductionQuality(quality);
        theDR.setReductionAmount(Integer.parseInt(DRValue));
      } else {
        // If same DR value Append AttackQuality
        if (theDR.getReductionAmount() == Integer.parseInt(DRValue))
          theDR.addReductionQuality(quality);

        // New DR is higher replace it
        if (theDR.getReductionAmount() < Integer.parseInt(DRValue)) {
          // Create new DR
          theDR = new CreatureDamageReduction();
          if (quality != null)
            theDR.addReductionQuality(quality);
          theDR.setReductionAmount(Integer.parseInt(DRValue));
        }
      }
      ctr.setDR(theDR);
    } catch (Exception e) {
      String msg = "Failed to process DR: " + DR;
      ctr.addToErrorLog(msg);
    }
  }

  private static String AttackQualityFromString(String qualName) {
    String quality = qualName.toLowerCase();
    if (qualName.equalsIgnoreCase("-"))
      return null;
    if (quality.contains("blunt") || quality.contains("bash"))
      return "Bludgeoning";
    if (quality.contains("pierce") || quality.contains("puncture"))
      return "Piercing";
    if (quality.contains("slash") || quality.contains("edged"))
      return "Slashing";
    else
      return qualName;

//      return "Dash";
  }
}
