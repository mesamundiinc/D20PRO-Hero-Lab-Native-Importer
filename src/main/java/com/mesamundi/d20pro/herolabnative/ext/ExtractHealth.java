package com.mesamundi.d20pro.herolabnative.ext;

import java.util.function.BiConsumer;

import com.mesamundi.d20pro.herolabnative.Extractor;
import com.mindgene.d20.common.creature.CreatureTemplate;

import static com.mesamundi.d20pro.herolabnative.HeroLabNativeImportLogic.valueFromMap;
import static java.lang.Short.parseShort;

/**
 * Created by Mat on 10/17/2017.
 */
public class ExtractHealth implements Extractor {
  @Override
  public void extract(Nug nug) throws Exception {
    Extractor.extract(nug,
            health("currenthp", CreatureTemplate::setHP),
            health("hitpoints", CreatureTemplate::setHPMax));
  }

  public static Extractor health(String key, BiConsumer<CreatureTemplate, Short> logic) {
    return nug -> valueFromMap("health", key, (c, v) -> logic.accept(nug.ctr, parseShort(v))).extract(nug);
  }
}
