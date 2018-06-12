package com.mesamundi.d20pro.herolabnative.ext;

import java.util.Optional;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.mesamundi.d20pro.herolabnative.Extractor;
import com.mesamundi.d20pro.herolabnative.map.HeroLabNativeMap;

import static com.d20pro.plugin.api.XMLToDocumentHelper.xpath;
import static com.mesamundi.d20pro.herolabnative.HeroLabNativeImportLogic.characterPath;

/**
 * Created by Mat on 10/6/2017.
 */
public class ExtractTemplates implements Extractor {
  private static final Logger lg = Logger.getLogger(ExtractTemplates.class);

  @Override
  public void extract(Nug nug) throws Exception {
    Node item = xpath(nug.doc, characterPath("templates")).item(0);
    String summary = item.getAttributes().getNamedItem("summary").getNodeValue();
    if (summary.isEmpty())
      return;

    int at = summary.indexOf('+');
    String name = summary.substring(0, at).trim();

    Optional<String> mappedTemplateToClass = HeroLabNativeMap.templates().peekKeyForValue(name);
    if (mappedTemplateToClass.isPresent()) {
      String mapped = mappedTemplateToClass.get();
      lg.debug("Mapped Template: " + name + " to Class: " + mapped);
      name = mapped;
    }

    int levels = Integer.parseInt(summary.substring(at + 1));

    ExtractClasses.addCreatureClass(name, levels, nug.ctr, nug.svc);
  }
}
