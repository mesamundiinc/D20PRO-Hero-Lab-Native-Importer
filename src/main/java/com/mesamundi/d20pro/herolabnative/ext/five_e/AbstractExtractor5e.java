package com.mesamundi.d20pro.herolabnative.ext.five_e;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.w3c.dom.Node;

import com.mesamundi.d20pro.herolabnative.GameInfo;
import com.mesamundi.d20pro.herolabnative.GameSystemSpecificExtractor;
import com.sengent.common.control.exception.UserVisibleException;

import static com.d20pro.plugin.api.XMLToDocumentHelper.xpath;
import static com.mesamundi.d20pro.herolabnative.HeroLabNativeImportLogic.characterPath;
import static com.mesamundi.d20pro.herolabnative.arc.NamedItemArc.namedValue;


/**
 * Created by Mat on 12/6/2017.
 */
public abstract class AbstractExtractor5e implements GameSystemSpecificExtractor {
  @Override
  public final boolean isApplicable(GameInfo gameInfo) {
    return gameInfo.is5e();
  }

  public static void extractListFromText(Nug nug, String subpath, String delim, Consumer<List<String>> logic) throws UserVisibleException {
    Node node = xpath(nug.doc, characterPath(subpath)).item(0);
    String text = namedValue(node, "text");
    String[] split = text.split(Pattern.quote(delim));

    List<String> rawTokens = Arrays.asList(split);
    List<String> trimmedTokens = rawTokens.stream().map(token -> token.trim()).collect(Collectors.toList());
    logic.accept(trimmedTokens);
  }
}
