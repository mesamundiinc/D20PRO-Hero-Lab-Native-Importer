package com.mesamundi.d20pro.herolabnative.ext;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.mesamundi.d20pro.herolabnative.ChildrenExtractor;
import com.mindgene.d20.common.creature.CreatureTemplate;
import com.mindgene.d20.common.game.trait.GenericTrait;

import static com.mesamundi.d20pro.herolabnative.arc.NamedItemArc.namedValue;

/**
 * Created by Mat on 10/26/2017.
 */
public class ExtractLanguages extends ChildrenExtractor {
  private static final Logger lg = Logger.getLogger(ExtractLanguages.class);

  public ExtractLanguages() {
    super("languages/language");
  }

  @Override
  public void accept(Node node, CreatureTemplate ctr) {
    String name = "Speak " + namedValue(node, "name");

    try {
      GenericTrait trait = new GenericTrait(name);
      trait.setSource("Languages");
      String key = GenericTrait.makeKey(trait);
      if (!ctr.getTraits().containsKey(key))
        ctr.getTraits().put(key, trait);
    } catch (Exception e) {
      String todo = "Ignoring language: " + name;
      ctr.addToErrorLog(todo);
      lg.warn(todo);
    }
    lg.debug("Added trait: " + name);
  }
}
