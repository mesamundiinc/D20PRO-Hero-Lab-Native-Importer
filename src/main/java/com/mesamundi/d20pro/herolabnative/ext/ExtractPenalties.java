package com.mesamundi.d20pro.herolabnative.ext;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.mesamundi.d20pro.herolabnative.ChildrenExtractor;
import com.mindgene.d20.common.creature.CreatureTemplate;

import static com.mesamundi.d20pro.herolabnative.arc.NamedItemArc.namedValue;
import static com.mesamundi.d20pro.herolabnative.util.ValueParser.parseShort;

/**
 * Created by Mat on 10/25/2017.
 */
public class ExtractPenalties extends ChildrenExtractor {
  private static final Logger lg = Logger.getLogger(ExtractPenalties.class);

  public ExtractPenalties() {
    super("penalties/penalty");
  }

  @Override
  public void accept(Node node, CreatureTemplate ctr) {
    String name = namedValue(node, "name");
    short value = parseShort(namedValue(node, "value"));
    switch (name) {
      case "Armor Check Penalty": // TODO: 10/25/2017 inject armor check penalty
        String todo = "Ignoring: Armor Check Penalty";
        ctr.addToErrorLog(todo);
        lg.warn(todo);
        break;
      case "Max Dex Bonus":
        ctr.setMaxDexBonus(value);
        break;
      default:
        lg.error("Unsupported penalty: " + name);
    }
  }
}
