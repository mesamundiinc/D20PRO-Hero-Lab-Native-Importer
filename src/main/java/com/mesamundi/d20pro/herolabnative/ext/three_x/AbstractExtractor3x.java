package com.mesamundi.d20pro.herolabnative.ext.three_x;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.mesamundi.d20pro.herolabnative.ChildrenExtractor;
import com.mesamundi.d20pro.herolabnative.GameInfo;
import com.mesamundi.d20pro.herolabnative.GameSystemSpecificExtractor;
import com.mesamundi.d20pro.herolabnative.arc.ChildArc;
import com.mindgene.d20.common.creature.CreatureTemplate;

import static com.mesamundi.d20pro.herolabnative.arc.NamedItemArc.namedValue;

/**
 * Extractor meant for "3.x" Systems that include Pathfinder and 3.5.
 */
public abstract class AbstractExtractor3x extends ChildrenExtractor implements GameSystemSpecificExtractor {

  AbstractExtractor3x(String subpath) {
    super(subpath + "/special");
  }

  @Override
  public boolean isApplicable(GameInfo gameInfo) {
    return gameInfo.isPathfinder() || gameInfo.is3_5();
  }

  @Override
  public void accept(Node node, CreatureTemplate ctr) {
    String name = namedValue(node, "name");
    String shortname = namedValue(node, "shortname");
    String desccription = ChildArc.findChild(node, "description").get().getTextContent();
    acceptSpecial(ctr, name, shortname, desccription);
  }

  protected abstract void acceptSpecial(CreatureTemplate ctr, String name, String shortname, String description);

  protected void handleZeroChildren(String xpath) {
    // log as concrete class
    Logger.getLogger(getClass()).info("None found.");
  }
}
