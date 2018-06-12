package com.mesamundi.d20pro.herolabnative;

import java.util.ArrayList;
import java.util.List;

import com.mesamundi.d20pro.herolabnative.HeroLabNativeImportLogic.Extractors;

/**
 * Created by Mat on 7/21/2017.
 */
public interface DependentExtractor extends Extractor {
  List<Extractors> defineDependencies();

  static List<Extractors> wrap(Extractors... extractors) {
    List<Extractors> es = new ArrayList<>(extractors.length);
    for (Extractors e : extractors)
      es.add(e);
    return es;
  }
}
