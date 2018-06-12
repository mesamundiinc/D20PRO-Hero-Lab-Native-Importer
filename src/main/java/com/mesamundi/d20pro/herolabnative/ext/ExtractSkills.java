package com.mesamundi.d20pro.herolabnative.ext;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.mesamundi.d20pro.herolabnative.ChildrenExtractor;
import com.mindgene.d20.common.creature.CreatureTemplate;
import com.mindgene.d20.common.game.skill.GenericSkill;

import static com.mesamundi.d20pro.herolabnative.arc.NamedItemArc.namedValue;
import static com.mesamundi.d20pro.herolabnative.ext.ExtractAttributes.peekAbilityNames;

/**
 * Created by Mat on 10/20/2017.
 */
public class ExtractSkills extends ChildrenExtractor {
  private static final Logger lg = Logger.getLogger(ExtractSkills.class);

  public ExtractSkills() {
    super("skills/skill");
  }

  @Override
  public void accept(Node node, CreatureTemplate ctr) {
    String name = namedValue(node, "name");
    lg.debug("Extracting Skill: " + name);
    String abilabbreviation = namedValue(node, "abilabbreviation");
    if (abilabbreviation == null || abilabbreviation.isEmpty())
      abilabbreviation = namedValue(node, "attrname");
    lg.debug("skills: attrname = " + abilabbreviation);
    byte ability = 0;
    boolean found = false;

    String[] abilities = peekAbilityNames();
    ability = (byte) Arrays.asList(abilities).indexOf(abilabbreviation);

    GenericSkill skill = new GenericSkill(name, ability, null);
    lg.debug("created skill " + skill.getName());

    // check proficiency
    String isProficient = namedValue(node, "isproficient");
    String rank = namedValue(node, "ranks");
    short ranks = 0;
    if (rank != null && !rank.isEmpty())
      ranks = Short.parseShort(rank);
    lg.debug(name + " skill rank " + ranks);

    short value = Short.parseShort(namedValue(node, "value"));
    String bonus = namedValue(node, "abilbonus");
    if (bonus == null || bonus.isEmpty())
      bonus = namedValue(node, "attrbonus");

    short attrbonus = 0;
    if (bonus != null || !bonus.isEmpty())
      attrbonus = Short.parseShort(bonus);

    lg.debug(name + " ability bonus " + attrbonus);

    int misc = value - ranks - attrbonus;

    if (ranks == 0 && isProficient.equalsIgnoreCase("yes")) {
      skill.setRanks((short) 1);
      misc -= ctr.resolveBAB(); /// isProf is for 5e specifically
    } else
      skill.setRanks(ranks);

    skill.setMisc((short) misc);

    ctr.getSkills().addSkill(skill);
  }
}
