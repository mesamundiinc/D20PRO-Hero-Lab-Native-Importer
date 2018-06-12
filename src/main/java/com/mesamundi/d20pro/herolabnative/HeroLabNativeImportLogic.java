package com.mesamundi.d20pro.herolabnative;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.d20pro.plugin.api.CreatureImportServices;
import com.mesamundi.common.util.Zipper;
import com.mesamundi.d20pro.herolabnative.ext.*;
import com.mesamundi.d20pro.herolabnative.ext.five_e.ExtractConditionImmunities5e;
import com.mesamundi.d20pro.herolabnative.ext.five_e.ExtractDamageImmunities5e;
import com.mesamundi.d20pro.herolabnative.ext.five_e.ExtractDamageResistances5e;
import com.mesamundi.d20pro.herolabnative.ext.three_x.ExtractDamageReduction3x;
import com.mesamundi.d20pro.herolabnative.ext.three_x.ExtractImmunities3x;
import com.mesamundi.d20pro.herolabnative.ext.three_x.ExtractWeaknesses3x;
import com.mindgene.common.ObjectLibrary;
import com.mindgene.d20.common.Rules;
import com.mindgene.d20.common.creature.CreatureTemplate;
import com.mindgene.d20.common.creature.attack.CreatureAttackType_Melee;
import com.mindgene.d20.common.creature.attack.CreatureAttackType_Range;

import static com.d20pro.plugin.api.XMLToDocumentHelper.*;
import static com.mesamundi.d20pro.herolabnative.HeroLabNativeImporter.importImage;
import static java.lang.String.format;

/**
 * @author thraxxis
 */
public final class HeroLabNativeImportLogic {
  private static final Logger lg = Logger.getLogger(HeroLabNativeImportLogic.class);

  private final StatBlockHandle _handle;
  private final Zipper _zipper;
  private final CreatureImportServices _svc;
  private final GameInfo _gameInfo;

  private Document _doc;

  HeroLabNativeImportLogic(StatBlockHandle handle, Zipper zipper, CreatureImportServices svc, GameInfo gameInfo) {
    _handle = handle;
    _zipper = zipper;
    _svc = svc;
    _gameInfo = gameInfo;
  }

  List<CreatureTemplate> importCreature() throws Exception {
    lg.info("Importing: " + _handle);

    byte[] data = _handle.getData();
    if (lg.isTraceEnabled())
      lg.trace("Contents of " + _handle + ":\n" + new String(data));
    _doc = loadDocument(data);

    CreatureTemplate ctr = new CreatureTemplate();
    ctr.setModuleName("Hero Lab Native");

    applyExtractors(ctr);

    // Build the notes tab by combining notes, abilities, and error log
    ctr.buildFullNotes();

    _handle.getTextStatFilename().ifPresent(s -> ctr.setGMNotes(ctr.getGMNotes() + "\n" + s));

    importImage(_handle, _zipper, _svc).ifPresent(id -> ctr.setImageID(id.shortValue()));

    List<CreatureTemplate> creatures = new LinkedList<>();
    creatures.add(ctr);
    creatures.addAll(importMinions());
    return creatures;
  }

  private void applyExtractors(CreatureTemplate ctr) {
    final Extractor.Nug nug = new Extractor.Nug(ctr, _svc, _doc);

    Set<Extractors> dependencySet = new LinkedHashSet<>();

    Extractors[] extractors = Extractors.values();
    lg.debug(format("Found %d Extractors: %s", extractors.length, ObjectLibrary.formatArray(extractors, ", ")));
    for (Extractors extractor : extractors) {
      try {
        lg.debug(format("Applying extractor:\n  <------ %s -------", extractor));

        if (extractor.e instanceof DependentExtractor) {
          List<Extractors> dependencies = ((DependentExtractor) extractor.e).defineDependencies();
          for (Extractors dependency : dependencies) {
            if (!dependencySet.contains(dependency)) {
              String msg = format("Extractor: %s is missing dependency: %s", extractor, dependency);
              ctr.addToErrorLog(msg);
              lg.error(msg);
            }
          }
        }

        if (extractor.e instanceof GameSystemSpecificExtractor) {
          if (!((GameSystemSpecificExtractor) extractor.e).isApplicable(_gameInfo)) {
            lg.debug(format("Skipping Extractor: %s because its not applicable to the Game System.", extractor));
            continue;
          }
        }

        extractor.e.extract(nug);
        boolean add = dependencySet.add(extractor);
        if (!add) {
          lg.error("Redundant attempt to add Extractor: " + extractor);
        }
      } catch (Exception e) {
        String msg = "Failure in extractor: " + extractor;
        ctr.addToErrorLog(msg);
        lg.error(msg, e);
      } finally {
        lg.debug(format("\n  ------- %s ------>\n", extractor));
      }
    }
  }

  private static BiConsumer<CreatureTemplate, String> toNotes(String name)
  {
    return mutate(addToNotes, v -> name + ": " + v);
  }

  @SuppressWarnings("unused")
  public enum Extractors {
    name(valueForName(null, CreatureTemplate::setName)),

    alignment(valueForName("alignment", CreatureTemplate::setAlignment)),

    size(new ExtractSize()),

    templates(new ExtractTemplates()),

    classes(new ExtractClasses()),

    attributes(new ExtractAttributes()),

    // senses

    // auras

    health(new ExtractHealth()),

    xp(valueFor("xp", "total", CreatureTemplate::setExperiencePoints)),

    challengerating("CR TODO"),

    money(new ExtractMoney()),

    personal_description(textForPath("personal/description", CreatureTemplate::setDescription)),
    personal_charheight(valueFor("personal/charheight", "text", toNotes("Height"))),
    personal_charweight(valueFor("personal/charweight", "text", toNotes("Weight"))),
    personal_chargender(valueFor("personal", "gender", toNotes("Gender"))),
    personal_charage(valueFor("personal", "age", toNotes("Age"))),
    personal_charhair(valueFor("personal", "hair", toNotes("Hair"))),
    personal_chareyes(valueFor("personal", "eyes", toNotes("Eyes"))),
    personal_charskin(valueFor("personal", "skin", toNotes("Skin"))),


    languages(new ExtractLanguages()),
    saves(new ExtractSaves()),
    allsaves(valueFor("saves/allsaves/situationalmodifiers", "text", toNotes("All Saves"))),

    // defensive
    damagereduction_3x(new ExtractDamageReduction3x()),
    immunities_3x(new ExtractImmunities3x()),
    weaknesses_3x(new ExtractWeaknesses3x()),

    damageimmunities_5e(new ExtractDamageImmunities5e()),
    damageresistances_5e(new ExtractDamageResistances5e()),
    conditionimmunities_5e(new ExtractConditionImmunities5e()),

    // weaknessess
    armorclass(new ExtractArmorClass()),
    penalties(new ExtractPenalties()),
    // initiative
    // movement
    // encumbrance
    skills(new ExtractSkills()),
    // skillabilities
    feats(new ExtractFeats()),
    backgrounds(new ExtractTraits("background/backgroundtrait", "type")),
    // traits
    // flaws
    // skilltricks
    // attack
    melee(new ExtractWeapons("melee", new CreatureAttackType_Melee())),
    ranged(new ExtractWeapons("ranged", new CreatureAttackType_Range())),
    // defenses
    magicitems(new ExtractGear(ExtractGear.C.MAGIC_ITEMS)),
    gear(new ExtractGear(ExtractGear.C.GEAR)),
    // otherspecials
    // spellsknown
    // spellsmemorized
    // spellbook
    // spellclasses
    // journals
    // ...
    ;

    private final Extractor e;

    /**
     * Stubbec constructor for WIPs
     */
    Extractors() {
      this(new Stub());
    }

    Extractors(Extractor e) {
      this.e = e;
    }

    Extractors(String val) {
      this(nug -> addToNotes.accept(nug.ctr, val));
    }

    private static class Stub implements Extractor {
      @Override
      public void extract(Nug nug) throws Exception {
        lg.warn("STUBBED!");
      }
    }
  }

  public static final BiConsumer<CreatureTemplate, String> addToNotes = CreatureTemplate::addToNotes;

  public static BiConsumer<CreatureTemplate, String> mutate(BiConsumer<CreatureTemplate, String> bc, Function<String, String> logic) {
    return (ctr, val) -> bc.accept(ctr, logic.apply(val));
  }

  public static String characterPath(String subpath) {
    StringBuilder path = new StringBuilder("/document/public/character");
    if (null != subpath)
      path.append('/').append(subpath);
    return path.toString();
  }

  public static Extractor valueFor(String subpath, String name, BiConsumer<CreatureTemplate, String> logic) {
    return nug -> logic.accept(nug.ctr, peekValueForNamedItem(nug.doc, characterPath(subpath), name));
  }

  public static Extractor valueForName(String subpath, BiConsumer<CreatureTemplate, String> logic) {
    return valueFor(subpath, "name", logic);
  }

  public static Extractor textForPath(String subpath, BiConsumer<CreatureTemplate, String> logic) {
    return nug -> logic.accept(nug.ctr, xpath(nug.doc, characterPath(subpath)).item(0).getTextContent());
  }

  public static Extractor valueFromMap(String subpath, String key, BiConsumer<CreatureTemplate, String> logic) {
    return nug -> logic.accept(nug.ctr, peekMapData(nug.doc, characterPath(subpath)).get(key));
  }

  private List<CreatureTemplate> importMinions() {
    lg.info("Minions currently not supported");
    return Collections.emptyList();
  }

  public static int resolveBAB(CreatureTemplate ctr) {
    try {
      int BAB = (int) Rules.getInstance().invokeMethod("Rules.CreatureClass.resolveBAB", ctr);
      return BAB;
    } catch (Exception e) {
      throw new RuntimeException("Failed to resolveBAB", e);
    }
  }
}
