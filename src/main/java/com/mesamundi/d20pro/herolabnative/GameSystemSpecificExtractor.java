package com.mesamundi.d20pro.herolabnative;

/**
 * If an Extractor is only applicable for certain game systems it must implement this interface.
 */
public interface GameSystemSpecificExtractor extends Extractor {
  boolean isApplicable(GameInfo gameInfo);
}
