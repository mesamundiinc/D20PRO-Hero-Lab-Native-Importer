package com.mesamundi.d20pro.herolabnative.ext.five_e;

import org.apache.log4j.Logger;

/**
 * Created by Mat on 11/27/2017.
 */
public class ExtractDamageResistances5e extends AbstractExtractor5e {
  private static final Logger lg = Logger.getLogger(ExtractDamageResistances5e.class);

  @Override
  public void extract(Nug nug) throws Exception {
    extractListFromText(nug, "damageresistances", ";", tokens -> {
      for (String resistance : tokens) {
        lg.warn("Ignoring: " + resistance);
        // TODO: 11/27/2017 inject 5e damage resistances
      }
    });
  }
}
