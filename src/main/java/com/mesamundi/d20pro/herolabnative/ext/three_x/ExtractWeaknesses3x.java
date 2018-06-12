package com.mesamundi.d20pro.herolabnative.ext.three_x;

import org.apache.log4j.Logger;

import com.mindgene.d20.common.creature.CreatureTemplate;

/**
 * Created by Mat on 12/6/2017.
 */
public class ExtractWeaknesses3x extends AbstractExtractor3x {
  private static final Logger lg = Logger.getLogger(ExtractWeaknesses3x.class);

  public ExtractWeaknesses3x() {
    super("weaknesses");
  }

  @Override
  protected void acceptSpecial(CreatureTemplate ctr, String name, String shortname, String description) {
    // TODO: 12/6/2017 inject 3x weaknesses
    String msg = "Ignoring weakness: " + shortname;
    lg.warn(msg);
    ctr.addToErrorLog(msg);
  }
}
