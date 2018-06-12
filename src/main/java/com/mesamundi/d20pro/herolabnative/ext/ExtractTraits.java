package com.mesamundi.d20pro.herolabnative.ext;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.mesamundi.d20pro.herolabnative.ChildrenExtractor;
import com.mindgene.d20.common.creature.CreatureTemplate;
import com.mindgene.d20.common.game.trait.GenericTrait;

import static com.mesamundi.d20pro.herolabnative.arc.NamedItemArc.namedValue;

/**
 * Created by Mat on 10/20/2017.
 */
public class ExtractTraits extends ChildrenExtractor {
  private static final Logger lg = Logger.getLogger(ExtractTraits.class);

  private String cat = "";

  public ExtractTraits(String subpath, String category) {
    super(subpath);
    cat = category;
  }

  @Override
  public void accept(Node node, CreatureTemplate ctr) {
    String name = namedValue(node, "name");
    String category = namedValue(node, cat);
    String description = node.getTextContent();

    if (name.isEmpty() && !category.isEmpty())
      name = category;

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
