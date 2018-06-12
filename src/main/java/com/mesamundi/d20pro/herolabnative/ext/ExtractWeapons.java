package com.mesamundi.d20pro.herolabnative.ext;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mesamundi.d20pro.herolabnative.DependentExtractor;
import com.mesamundi.d20pro.herolabnative.HeroLabNativeImportLogic.Extractors;
import com.mindgene.common.ObjectLibrary;
import com.mindgene.d20.common.creature.CreatureTemplate;
import com.mindgene.d20.common.creature.attack.*;
import com.mindgene.d20.common.dice.Dice;
import com.mindgene.d20.common.dice.DiceFormatException;
import com.mindgene.d20.common.item.ItemTemplate;
import com.mindgene.d20.dm.game.CreatureInPlay;

import static com.d20pro.plugin.api.XMLToDocumentHelper.xpath;
import static com.mesamundi.d20pro.herolabnative.HeroLabNativeImportLogic.characterPath;
import static com.mesamundi.d20pro.herolabnative.HeroLabNativeImportLogic.resolveBAB;
import static com.mesamundi.d20pro.herolabnative.arc.NamedItemArc.arc;
import static com.mesamundi.d20pro.herolabnative.arc.NamedItemArc.trav;

/**
 * Extractor for Weapons.
 */
public class ExtractWeapons implements DependentExtractor {
  private static final Logger lg = Logger.getLogger(ExtractWeapons.class);

  private final String name;
  private final CreatureAttackType type;
  private static String typeText;

  public ExtractWeapons(String name, CreatureAttackType type) {
    this.name = name;
    this.type = type;
  }

  @Override
  public void extract(Nug nug) throws Exception {
    ArrayList<CreatureAttack> attacks = nug.ctr.getAttacks();

    NodeList nodes = xpath(nug.doc, characterPath(name + "/weapon"));
    for (int i = 0, len = nodes.getLength(); i < len; i++) {
      CreatureAttack weapon = extractWeapon(nodes.item(i), nug);
      attacks.add(weapon);
    }

    // TODO: 7/18/2017 handle rules specific attacks like combat maneuver for pathfinder
  }

  @Override
  public List<Extractors> defineDependencies() {
    return DependentExtractor.wrap(Extractors.attributes, Extractors.classes);  // TODO depend on feats (Finesse)
  }

  private CreatureAttack extractWeapon(Node node, Nug nug) {
    CreatureAttack atk = new CreatureAttack();
    atk.assumeType(type);

    trav(node,
            arc("name", atk::setName),
            arc("typetext", typetext(nug.ctr, atk)),
            arc("attack", attack(nug, atk)),
            arc("crit", crit(atk)),
            arc("damage", damage(nug, atk)),
            arc("equipped", equipped(nug.ctr, atk))
    );

    return atk;
  }

  private static Consumer<String> typetext(CreatureTemplate ctr, CreatureAttack atk) {
    return v -> {
      String[] split = v.split(Pattern.quote("/"));
      typeText = ObjectLibrary.formatArray(split, ", ");
    };
  }

  private static Consumer<String> attack(Nug nug, CreatureAttack atk) {
    return v -> {
      String[] raw = v.split(Pattern.quote("/"));
      atk.setAttackCascading(raw.length > 1);

      int resolvedToHit = Integer.parseInt(raw[0]);

      DeclaredCreatureAttack attack = new DeclaredCreatureAttack(atk, 0, new CreatureInPlay(nug.ctr));
      int toHit = attack.resolveToHit(nug.dm());
      atk.setToHit(resolvedToHit - toHit);
    };
  }

  /**
   * EXAMPLES:
   * <weapon name="+3 greataxe" categorytext="Melee Weapon" typetext="S" attack="+20/+15/+10"
   * crit="×3" damage="1d12+6" equipped="bothhands" quantity="1">
   * <p>
   * <weapon name="+4 flaming burst heavy crossbow" categorytext="Projectile Weapon" typetext="P" attack="+26"
   * crit="17-20/×2+1d10 fire" damage="1d10+4 plus 1d6 fire" quantity="1">
   * <p>
   * <weapon name="Masterwork silver dagger" categorytext="Melee Weapon, Thrown Weapon" typetext="P/S"
   * attack="+18/+13/+8" crit="19-20/×2" damage="1d4+1" quantity="1">
   */
  private static Consumer<String> crit(CreatureAttack atk) {
    return v -> {
      String[] raw = v.split(Pattern.quote("/"));
      int at = 0;
      if (raw.length > 1) {
        String min = raw[0].split(Pattern.quote("-"))[0];
        atk.setCritMinThreat(Byte.parseByte(min));
        at = 1;
      }
      atk.setCritMultiplier(Byte.parseByte(raw[at].substring(1).split(Pattern.quote("+"))[0]));
    };
  }

  private static Consumer<String> damage(Nug nug, CreatureAttack atk) {
    return v -> {
      ArrayList<CreatureAttackDamage> damages = atk.getDamages();
      damages.clear();

      String[] raw = v.split("plus");

      Dice dice;
      String[] prune = raw[0].split("\\s+");
      try {
        dice = new Dice(prune[0]);
      } catch (DiceFormatException e) {
        throw new RuntimeException("Failed to parse primary damage: " + raw[0]);
      }

      // look for the corresponding item
      ExtractGear.findItem(atk.getName(), nug).ifPresent(item -> {
        float weight = item.accessWeight();
        if(weight > 8)
          atk.setStyle(new CreatureAttackStyle_2Hand());
      });

      // TODO account for 2 hand or off hand
      byte mod = nug.ctr.getAbilityScoreMods(atk.getAbilityToDamage());
      dice.setMod(dice.getMod() - mod);

      CreatureAttackDamage dmg = new CreatureAttackDamage();
      dmg.setDice(dice);
      LinkedHashSet<String> qualities = new LinkedHashSet<>();
      String[] dTypes = typeText.split(",");
      for (int q = 0; q < dTypes.length; q++) {
        dTypes[q] = dTypes[q].trim();
        String qual = dTypes[q].toLowerCase();
        if (qual.equalsIgnoreCase("B") || qual.contains("bash") || qual.contains("bludgeon"))
          qualities.add("Bludgeoning");
        else if (qual.equalsIgnoreCase("S") || qual.contains("slash") || qual.contains("edged"))
          qualities.add("Slashing");
        else if (qual.equalsIgnoreCase("P") || qual.contains("piercing") || qual.contains("puncture"))
          qualities.add("Piercing");
        else
          qualities.add(dTypes[q].toLowerCase());
      }
      dmg.setAttackQualities(qualities);
      damages.add(dmg);
    };
  }

  private static Consumer<String> equipped(CreatureTemplate ctr, CreatureAttack atk) {
    return v -> {
      if (!v.isEmpty()) {
        // TODO: 10/26/2017 inject equipped
        String todo = "Ignoring weapon equipped: " + v;
        ctr.addToErrorLog(todo);
        lg.warn(todo);
      }
    };
  }
}
