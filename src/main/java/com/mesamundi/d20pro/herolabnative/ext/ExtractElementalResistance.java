package com.mesamundi.d20pro.herolabnative.ext;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.mesamundi.d20pro.herolabnative.ChildrenExtractor;
import com.mindgene.d20.common.creature.CreatureDamageReduction;
import com.mindgene.d20.common.creature.CreatureTemplate;

import static com.mesamundi.d20pro.herolabnative.HeroLabNativeImportLogic.*;

/**
 * Created by Mat on 10/25/2017.
 */
public class ExtractElementalResistance extends ChildrenExtractor {
  private static final Logger lg = Logger.getLogger(ExtractElementalResistance.class);

  private static String _nodeKey;

  public ExtractElementalResistance(String key, String nodeKey) {
    super(key);
    _nodeKey = nodeKey;
    valueFor(key, nodeKey, mutate(addToNotes, v -> "ER: " + v));
  }

  @Override
  public void accept(Node node, CreatureTemplate ctr) {
    // temp diabled
    return;
//        String shortname = namedValue(node, _nodeKey);

//        String[] split = shortname.split(";|\\,");
//        for(int i = 0; i < split.length; i++)
//            lg.error("ER: " + split[i].trim());
//
//        int amount = Integer.parseInt(split[0]);
//        String type = split[1];

//        // TODO: 10/26/2017 inject DR
//        processDamageReduction(ctr, shortname);
//        String todo = "Ignoring DR: " + shortname;
//        ctr.addToErrorLog(todo);
//        lg.warn(todo);
  }

  private static void processDamageReduction(CreatureTemplate ctr, String DR) {
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
    if (qualName.equalsIgnoreCase("-"))
      return null;
    else
      return qualName;

//      return "Dash";
  }
}
