package com.mesamundi.d20pro.herolabnative;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.log4j.Logger;

import com.d20pro.plugin.api.CreatureImportServices;
import com.d20pro.plugin.api.ImportCreatureException;
import com.d20pro.plugin.api.ImportCreaturePlugin;
import com.d20pro.plugin.api.ImportMessageLog;
import com.mesamundi.common.FileCommon;
import com.mesamundi.common.ObjectCommon;
import com.mesamundi.common.util.Zipper;
import com.mindgene.common.util.FileFilterForExtension;
import com.mindgene.d20.common.D20LF;
import com.mindgene.d20.common.creature.CreatureTemplate;
import com.mindgene.d20.common.creature.view.CreatureViewWindow;
import com.mindgene.d20.common.util.ImageProvider;
import com.mindgene.d20.dm.DM;
import com.mindgene.d20.dm.creature.DMCreatureView;
import com.mindgene.d20.dm.game.CreatureInPlay;
import com.mindgene.lf.SwingSafe;
import com.sengent.common.control.exception.UserVisibleException;
import com.sengent.common.control.exception.VerificationException;

/**
 * Imports Creatures directly from Hero Lab portfolio files.
 *
 * @author thraxxis
 */
public class HeroLabNativeImporter implements ImportCreaturePlugin {
  private static final Logger lg = Logger.getLogger(HeroLabNativeImporter.class);

  @Override
  public String getPluginName() {
    return "Hero Lab Native";
  }

  @Override
  public List<CreatureTemplate> importCreatures(CreatureImportServices svc, ImportMessageLog log)
          throws ImportCreatureException {
    java.util.List<File> files = new ArrayList<>();
    if (DebugHeroLabNative.isOn()) {
      List<File> memory = DebugHeroLabNative.peek();
      if (!memory.isEmpty()) {
        if (D20LF.Dlg.showConfirmation(svc.accessAnchor(), "Proceed with last path?")) {
          files.addAll(memory);
        } else {
          DebugHeroLabNative.clear();
        }

      }
    }

    if (files.isEmpty()) {
      List<File> freshFiles = svc.chooseFiles(this);
      files.addAll(freshFiles);
      if (DebugHeroLabNative.isOn())
        DebugHeroLabNative.poke(files);
    }

    lg.debug("User chose files: " + ObjectCommon.formatList(files));

    List<CreatureTemplate> allCreatures = new LinkedList<>();

    for (File file : files) {
      List<CreatureTemplate> creatures = processPortfolio(file, svc);
      if (!creatures.isEmpty()) {
        lg.debug("Imported " + creatures.size() + " Creatures.");
        allCreatures.addAll(creatures);
      }
    }

    return allCreatures;
  }

  private List<CreatureTemplate> processPortfolio(File file, CreatureImportServices svc) {
    lg.debug("Processing Portfolio: " + file);

    ProcessPortfolioWRP wrp = new ProcessPortfolioWRP(file);
    wrp.showDialog(svc.accessAnchor());
    if (wrp.isCancelled())
      return Collections.emptyList();

    Zipper zipper = null;
    try {

      try {
        zipper = new Zipper(file);
      } catch (Exception e) {
        lg.error("Failed to access zip archive", e);
        return Collections.emptyList();
      }

      GameInfo gameInfo;
      try {
        gameInfo = GameInfo.extract(zipper);
      } catch (GameInfo.GameInfoNotAvailableException e) {
        D20LF.Dlg.showError(svc.accessAnchor(), String.format("The Portfolio: %s appears to be corrupt.", file.getAbsolutePath()), e);
        return Collections.emptyList();
      }

      lg.info("Importing for: " + gameInfo.toString());

      List<CreatureTemplate> creatures = new LinkedList<>();

      for (StatBlockHandle handle : wrp.peekSelectedCreatures()) {
        try {
          List<CreatureTemplate> someCreatures = new HeroLabNativeImportLogic(handle, zipper, svc, gameInfo).importCreature();
          someCreatures.forEach(ctr -> showEditor(ctr, svc).ifPresent(creatures::add));
        } catch (Exception e) {
          lg.error("Failed to import Creatures from: " + handle, e);
        }
      }

      return creatures;
    } finally {
      if (null != zipper)
        try {
          zipper.close();
        } catch (IOException e) {
          lg.error("Failed to close zipper", e);
        }
    }
  }

  private Optional<CreatureTemplate> showEditor(CreatureTemplate creature, CreatureImportServices svc) {
    DM dm = (DM) svc.accessImageService();

    boolean[] keep = {false};
    HeroLabNativeCreatureView view = new HeroLabNativeCreatureView(dm, new CreatureInPlay(creature)) {
      public void recognizePressedOK() throws Exception {
        try {
          verify();

          commit(_creatureOriginal);

          keep[0] = true;
        } catch (UserVisibleException uve) {
          throw uve;
        } catch (Exception e) {
          lg.error("Unexpected error", e);
          throw new VerificationException("Unexpected error", e);
        }
      }
    };

    SwingSafe.runSafeWait(() -> {
      CreatureViewWindow<DM> creatureViewWindow = new CreatureViewWindow<>(dm, view);
      creatureViewWindow.buildContent();

      JDialog dlg = creatureViewWindow.showDialog(dm.accessAppBlockerView());
      dlg.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
          super.windowClosing(e);
          resume();
        }

        @Override
        public void windowClosed(WindowEvent e) {
          super.windowClosed(e);
          resume();
        }
      });
    });

    lg.debug("Waiting for dialog...");
    synchronized (this) {
      try {
        wait();
      } catch (InterruptedException e) {
        lg.error("Wait interrupted", e);
        return Optional.empty();
      }
    }

    return keep[0] ? Optional.of(creature) : Optional.empty();
  }

  private void resume() {
    synchronized (this) {
      lg.debug("Done waiting.");
      notifyAll();
    }
  }

  private class HeroLabNativeCreatureView extends DMCreatureView {

    HeroLabNativeCreatureView(DM dm, CreatureInPlay mob) {
      super(dm, mob, "Hero Lab Native Importer");
    }
  }

  static Optional<Short> importImage(StatBlockHandle handle, Zipper zipper, CreatureImportServices svc) {
    Optional<String> optImage = handle.getImageFilename();

    if (optImage.isPresent()) {
      String imageEntry = optImage.get();
      lg.debug("Importing: " + imageEntry + " for: " + handle.getDisplayName());
      try {
        File tempDest = File.createTempFile(HeroLabNativeImporter.class.getSimpleName() + "Image", '.' + FileCommon.getExtension(imageEntry));
        FileCommon.ZipUtil.extractFileFromArchiveToFile(zipper.peekSourceFile(), imageEntry, tempDest);
        lg.debug("Extracted image to temp file: " + tempDest);
        return svc.accessImageService().assimilateImage(handle.getDisplayName(), tempDest, ImageProvider.Categories.CREATURE, "Hero Lab", true);
      } catch (UserVisibleException uve) {
        lg.error("Image not available: " + ObjectCommon.buildCollapsedExceptionMessage(uve));
      } catch (Exception e) {
        lg.error("Failed to extract image: " + imageEntry, e);
      }
    }

    return Optional.empty();
  }

  @Override
  public FileFilterForExtension getPluginFileFilter() {
    return new FileFilterForExtension("por", "Hero Lab Portfolio");
  }
}
