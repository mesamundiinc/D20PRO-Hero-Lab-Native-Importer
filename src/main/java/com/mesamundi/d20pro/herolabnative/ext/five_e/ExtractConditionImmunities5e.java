package com.mesamundi.d20pro.herolabnative.ext.five_e;

import org.apache.log4j.Logger;

/**
 * Created by Mat on 12/6/2017.
 */
public class ExtractConditionImmunities5e extends AbstractExtractor5e {
  private static final Logger lg = Logger.getLogger(ExtractConditionImmunities5e.class);

  @Override
  public void extract(Nug nug) throws Exception {
    extractListFromText(nug, "conditionimmunities", ",", tokens -> {
      for (String immunity : tokens) {
        lg.warn("Ignoring: " + immunity);
        // TODO: 11/27/2017 inject 5e condition immunities
      }
    });
  }
}
