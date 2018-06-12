package com.mesamundi.d20pro.herolabnative.ext;

import java.util.function.BiConsumer;

import com.mesamundi.d20pro.herolabnative.Extractor;
import com.mindgene.d20.common.D20Rules;
import com.mindgene.d20.common.creature.CreatureTemplate;

import static com.mesamundi.d20pro.herolabnative.HeroLabNativeImportLogic.valueFor;
import static com.mesamundi.d20pro.herolabnative.HeroLabNativeImportLogic.valueForName;

/**
 * Created by Mat on 7/8/2017.
 */
public class ExtractSize implements Extractor {
  @Override
  public void extract(Nug nug) throws Exception {
    Extractor size = valueForName("size", (c, v) -> c.setSize(D20Rules.Size.getID(v)));
    Extractor face = factor("space", (c, v) -> c.setFace(v, v));
    Extractor reach = factor("reach", (c, v) -> c.setReach(v));

    Extractor.extract(nug, size, face, reach);
  }

  private static Extractor factor(String subpath, BiConsumer<CreatureTemplate, Byte> logic) {
    return valueFor("size/" + subpath, "value", (c, v) -> {
      byte val = (byte) (Integer.parseInt(v) / 5);
      logic.accept(c, val);
    });
  }
}
