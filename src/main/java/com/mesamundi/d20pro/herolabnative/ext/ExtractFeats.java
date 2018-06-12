package com.mesamundi.d20pro.herolabnative.ext;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.mesamundi.d20pro.herolabnative.ChildrenExtractor;
import com.mesamundi.d20pro.herolabnative.arc.ChildArc;
import com.mindgene.d20.common.creature.CreatureTemplate;
import com.mindgene.d20.common.game.trait.GenericTrait;

import static com.mesamundi.d20pro.herolabnative.arc.NamedItemArc.namedValue;

/**
 * Created by Mat on 10/20/2017.
 */
public class ExtractFeats extends ChildrenExtractor {
  private static final Logger lg = Logger.getLogger(ExtractFeats.class);

  public ExtractFeats() {
    super("feats/feat");
  }

  @Override
  public void accept(Node node, CreatureTemplate ctr) {
    String name = namedValue(node, "name");
    String category = namedValue(node, "categorytext");
    String description = ChildArc.findChild(node, "description").get().getTextContent();

    try {
      GenericTrait trait = new GenericTrait(name);
      if (category.isEmpty())
        trait.setSource("General");
      else
        trait.setSource(category);

      trait.setDescription(description);
      trait.setLongDescription(description);

      String key = GenericTrait.makeKey(trait);
      if (!ctr.getTraits().containsKey(key))
        ctr.getTraits().put(key, trait);
    } catch (Exception e) {
      String todo = "Ignoring feat: " + name;
      ctr.addToErrorLog(todo);
      lg.warn(todo);
    }
    lg.debug("Added trait: " + name);
  }
}
