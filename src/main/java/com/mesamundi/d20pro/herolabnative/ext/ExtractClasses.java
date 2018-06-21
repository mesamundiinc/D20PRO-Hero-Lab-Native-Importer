package com.mesamundi.d20pro.herolabnative.ext;

import java.util.ArrayList;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.d20pro.plugin.api.CreatureImportServices;
import com.mesamundi.d20pro.herolabnative.Extractor;
import com.mindgene.d20.common.creature.CreatureTemplate;
import com.mindgene.d20.common.creature.CreatureTemplate_Classes;
import com.mindgene.d20.common.game.creatureclass.CreatureClassBinder;
import com.mindgene.d20.common.game.creatureclass.CreatureClassNotInstalledException;
import com.mindgene.d20.common.game.creatureclass.CreatureClassTemplate;
import com.mindgene.d20.common.game.creatureclass.GenericCreatureClass;

import static com.d20pro.plugin.api.XMLToDocumentHelper.xpath;
import static com.mesamundi.d20pro.herolabnative.HeroLabNativeImportLogic.characterPath;


public class ExtractClasses implements Extractor {
  private static final Logger lg = Logger.getLogger(ExtractClasses.class);

  @Override
  public void extract(Nug nug) throws Exception {
    NodeList xpath = xpath(nug.doc, characterPath("classes/class"));
    for (int i = 0, len = xpath.getLength(); i < len; i++) {
      Node item = xpath.item(i);
      NamedNodeMap attr = item.getAttributes();
      String name = attr.getNamedItem("name").getNodeValue();
      int level = Integer.parseInt(attr.getNamedItem("level").getNodeValue());
      addCreatureClass(name, level, nug.ctr, nug.svc);
    }

    /*
    // handle race, subrace, type and subtype
    NodeList rpath = xpath(nug.doc, characterPath("race"));
    if (rpath != null || rpath.getLength() > 0) {
      Node item = rpath.item(0);
      if (item != null) {
        NamedNodeMap attr = item.getAttributes();
        String name = attr.getNamedItem("name").getNodeValue();
        int level = 0;
        addCreatureClass(name, level, nug.ctr, nug.svc);
      }
    }

    NodeList srpath = xpath(nug.doc, characterPath("subrace"));
    if (srpath != null || srpath.getLength() > 0) {
      Node item = srpath.item(0);
      if (item != null) {
        NamedNodeMap attr = item.getAttributes();
        String name = attr.getNamedItem("name").getNodeValue();
        int level = 0;
        addCreatureClass(name, level, nug.ctr, nug.svc);
      }
    }

    NodeList tpath = xpath(nug.doc, characterPath("types/type"));
    if (tpath != null || tpath.getLength() > 0) {
      Node item = tpath.item(0);
      if (item != null) {
        NamedNodeMap attr = item.getAttributes();
        String name = attr.getNamedItem("name").getNodeValue();
        int level = 0;
        addCreatureClass(name, level, nug.ctr, nug.svc);
      }
    }

    NodeList spath = xpath(nug.doc, characterPath("subtypes/subtype"));
    if (spath != null || spath.getLength() > 0) {
      Node item = spath.item(0);
      if (item != null) {
        NamedNodeMap attr = item.getAttributes();
        String name = attr.getNamedItem("name").getNodeValue();
        int level = 0;
        addCreatureClass(name, level, nug.ctr, nug.svc);
      }
    }
    */
  }

  private static Optional<CreatureClassTemplate> findSubstringMatch(String name, CreatureClassTemplate[] installedClasses)
  {
    for(String word : name.split(" "))
    {
      for(CreatureClassTemplate template : installedClasses) {
        String installedClass = template.accessName();
        if(word.equalsIgnoreCase(installedClass))
        {
          lg.info("Mapping: " + name + " to: " + installedClass);
          return Optional.of(template);
        }
      }
    }

    return Optional.empty();
  }

  static void addCreatureClass(String name, int levels, CreatureTemplate ctr, CreatureImportServices svc) {
    CreatureClassBinder binder = svc.accessClasses();

    CreatureClassTemplate creatureClassTemplate;
    try {
      creatureClassTemplate = binder.accessClass(name);
    } catch (CreatureClassNotInstalledException e) {
      lg.info("Template: " + name + " not found in installed Classes, looking for substring matches");

      Optional<CreatureClassTemplate> match = findSubstringMatch(name, binder.accessInstalledClasses());
      if(match.isPresent())
      {
        creatureClassTemplate = match.get();
      }
      else {
        String msg = "Unrecognized creature class: " + name;
        ctr.addToErrorLog(msg);
        lg.warn(msg);
        creatureClassTemplate = null;
      }
    }

    if(null != creatureClassTemplate) {
      GenericCreatureClass creatureClass = new GenericCreatureClass(creatureClassTemplate);
      creatureClass.setCreature(ctr);
      creatureClass.setLevel((byte) levels);

      CreatureTemplate_Classes ctrClasses = ctr.getClasses();
      ArrayList<GenericCreatureClass> classes = ctrClasses.accessClasses();
      classes.add(creatureClass);
      ctrClasses.assignClasses(classes);
    }
  }
}
