package com.mesamundi.d20pro.herolabnative;

import java.util.ArrayList;
import java.util.List;

import com.d20pro.plugin.api.ImportCreaturePlugin;
import com.mindgene.common.plugin.Factory;

/**
 * Factory for Hero Lab.
 *
 * @author thraxxis
 */
public class CommandFactoryImpl implements Factory<ImportCreaturePlugin> {
  @Override
  public List<ImportCreaturePlugin> getPlugins() {
    ArrayList<ImportCreaturePlugin> plugins = new ArrayList<ImportCreaturePlugin>();
    plugins.add(new HeroLabNativeImporter());
    return plugins;
  }
}
