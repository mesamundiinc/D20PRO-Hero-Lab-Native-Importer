package com.mesamundi.d20pro.herolabnative;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.mindgene.common.ObjectLibrary;

/**
 * Debugger code for {@link HeroLabNativeImporter}.
 *
 * @author thraxxis
 */
public final class DebugHeroLabNative {
  /**
   * Pass in as a VM argument to enable (e.g. {@code -DDebugHeroLabNative=true}.
   */
  static final String DEBUG_KEY = "DebugHeroLabNative";

  private static final String MEMORY_KEY = "DebugHeroLabNativeMemory";
  private static final String DELIMITER = "|";

  static void poke(List<File> files) {
    List<String> filenames = files.stream().map(File::getAbsolutePath).collect(Collectors.toList());
    System.setProperty(MEMORY_KEY, ObjectLibrary.formatList(filenames, DELIMITER));
  }

  static List<File> peek() {
    String memory = System.getProperty(MEMORY_KEY);
    if (null == memory)
      return Collections.emptyList();
    String[] split = memory.split(Pattern.quote(DELIMITER));
    return Arrays.stream(split).map(File::new).collect(Collectors.toList());
  }

  static void clear() {
    System.clearProperty(MEMORY_KEY);
  }

  public static boolean isOn() {
    return Boolean.getBoolean(DEBUG_KEY);
  }

  private DebugHeroLabNative() {
  }
}
