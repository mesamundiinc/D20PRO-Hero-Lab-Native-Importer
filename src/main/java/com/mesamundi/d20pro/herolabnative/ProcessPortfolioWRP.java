package com.mesamundi.d20pro.herolabnative;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.zip.ZipException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.d20pro.plugin.api.XMLToDocumentHelper;
import com.mesamundi.common.FileCommon.ZipUtil;
import com.mesamundi.common.ObjectCommon;
import com.mesamundi.common.util.Zipper;
import com.mesamundi.d20pro.herolabnative.util.NodeCollector;
import com.mindgene.d20.LAF;
import com.mindgene.d20.common.D20LF;
import com.mindgene.d20.common.lf.D20OKCancelReadyPanel;
import com.mindgene.d20.laf.BlockerControl;
import com.mindgene.d20.laf.BlockerView;

import static com.d20pro.plugin.api.XMLToDocumentHelper.peekMapData;
import static com.d20pro.plugin.api.XMLToDocumentHelper.xpath;

/**
 * Dialog that displays the contents of a Hero Lab Portfolio.
 *
 * @author thraxxis
 */
final class ProcessPortfolioWRP extends D20OKCancelReadyPanel {
  private static final Logger lg = Logger.getLogger(ProcessPortfolioWRP.class);

  private final BlockerView _blocker;
  private final File _file;

  private List<StatBlockHandle> _handles;
  private List<JCheckBox> _handleChecks;

  ProcessPortfolioWRP(File file) {
    _file = file;
    _blocker = LAF.Area.blocker(LAF.Area.clear());

    setLayout(new BorderLayout());
    add(_blocker, BorderLayout.CENTER);

    setPreferredSize(new Dimension(300, 200));
    setResizable(true);

    new LoadPortfolioLogic();
  }

  private interface Entry {
    String INDEX = "index.xml";
    String STAT_BLOCK = "statblocks_xml/";
    String STAT_TEXT = "statblocks_text/";
    String IMAGE = "images/";
  }

  private class LoadPortfolioLogic extends BlockerControl {
    LoadPortfolioLogic() {
      super(LoadPortfolioLogic.class, "Loading Portfolio...", _blocker);
      startThread();
    }

    @Override
    protected void work() {
      try {
        Zipper zipper = new Zipper(_file);

        lg.trace("Portfolio entries:\n" + ObjectCommon.formatArray(zipper.getCurrentEntries(), "\n"));

        Document docIndex;
        if (zipper.entryExists(Entry.INDEX)) {
          byte[] index = ZipUtil.extractFileBytesFromArchive(_file, Entry.INDEX);
          if (lg.isTraceEnabled())
            lg.trace("Contents of " + Entry.INDEX + ": " + new String(index));
          docIndex = XMLToDocumentHelper.loadDocument(index);
        } else {
          throw new IOException("Portfolio is missing " + Entry.INDEX);
        }

        String gameSystem = resolveGameSystem(docIndex);
        lg.debug("Discovered game system: " + gameSystem);

        _handles = resolveStatBlocks(zipper, docIndex);

        SwingUtilities.invokeLater(() -> {
          JPanel area = LAF.Area.clear(0, 2);
          area.setBorder(D20LF.Brdr.padded(4));

          JPanel areaHandles = LAF.Area.clear(new GridLayout(0, 1, 0, 2));

          _handleChecks = new ArrayList<>(_handles.size());
          for (StatBlockHandle handle : _handles) {
            JCheckBox checkHandle = LAF.Check.common(handle.toString());
            checkHandle.setSelected(true);
            _handleChecks.add(checkHandle);
            areaHandles.add(checkHandle);
          }

          class SelectAction extends AbstractAction {
            private final boolean state;

            private SelectAction(String name, boolean state) {
              super(name);
              this.state = state;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
              _handleChecks.forEach(h -> h.setSelected(state));
            }
          }

          JPanel areaFlip = LAF.Area.clear(new FlowLayout(FlowLayout.CENTER, 2, 0));
          areaFlip.add(LAF.Button.common(new SelectAction("Select All", true)));
          areaFlip.add(LAF.Button.common(new SelectAction("Select None", false)));

          JPanel areaTop = LAF.Area.clear();
          areaTop.add(D20LF.Pnl.labeled("Game System: ", LAF.Label.left(gameSystem)), BorderLayout.NORTH);
          areaTop.add(areaFlip, BorderLayout.SOUTH);

          area.add(areaTop, BorderLayout.NORTH);
          JScrollPane sPane = LAF.Area.sPaneVertical(LAF.Area.Hugging.top(areaHandles));
          sPane.setBorder(null);
          area.add(sPane, BorderLayout.CENTER);

          _blocker.setContent(area);

          if (DebugHeroLabNative.isOn() && _handles.size() < 2) {
            doClick_OK();
          }
        });
      } catch (Exception e) {
        D20LF.Dlg.showError(ProcessPortfolioWRP.this, "Failed to load Portfolio", e);
        disposeWindow();
      }
    }

    private String resolveGameSystem(Document doc) {
      try {
        return XMLToDocumentHelper.peekValueForNamedItem(doc, "/document/game", "name");
      } catch (Exception e) {
        lg.error("Failed to extract game system", e);
      }
      return "???";
    }

    private List<String> resolveCharacters(Zipper zipper) {
      return null;
    }

    private String path(Node node) {
      Map<String, String> formatData = peekMapData(node);
      return formatData.get("folder") + "/" + formatData.get("filename");
    }

    private Optional<String> statBlockByFormat(String format, Node parent) {
      List<Node> nodes = NodeCollector.collectChildren(parent,
              n -> peekMapData(n).get("format").equalsIgnoreCase(format),
              Node.ELEMENT_NODE);
      return nodes.isEmpty() ? Optional.empty() : Optional.of(path(nodes.get(0)));
    }

    /**
     * List of Pairs where the key is the name of the Creature and the byte[] is its XML stat block.
     *
     * @param zipper
     * @return
     * @throws ZipException
     */
    private List<StatBlockHandle> resolveStatBlocks(Zipper zipper, Document docIndex) throws Exception {
      List<StatBlockHandle> statBlocks = new LinkedList<>();

//      Map<Integer, String> images = resolveImages(entries);

      NodeList nodes = xpath(docIndex, "/document/characters/character");
      lg.debug("Expecting " + nodes.getLength() + " characters.");
      for (int i = 0, len = nodes.getLength(); i < len; i++) {
        Node item = nodes.item(i);

        Map<String, String> itemData = peekMapData(item);
        lg.debug("Resolving: " + itemData.get("name"));
        Integer id = Integer.valueOf(itemData.get("herolableadindex"));

        Node statblocks = XMLToDocumentHelper.xpath(item, "statblocks");
        String entry = statBlockByFormat("xml", statblocks).get();

        lg.debug("Loading stat block for entry: " + entry);
        byte[] statBlock = ZipUtil.extractFileBytesFromArchive(_file, entry);
        String name = entry.substring(Entry.STAT_BLOCK.length());

        Optional<String> optTextStat;
        Optional<String> optTextStatEntry = statBlockByFormat("text", statblocks);
        if (optTextStatEntry.isPresent()) {
          byte[] statTextBlock = ZipUtil.extractFileBytesFromArchive(_file, optTextStatEntry.get());
          optTextStat = Optional.of(new String(statTextBlock));
        } else {
          optTextStat = Optional.empty();
        }

        Node images = XMLToDocumentHelper.xpath(item, "images");
        List<Node> imageNodes = NodeCollector.collectChildren(images,
                n -> true,
                Node.ELEMENT_NODE);

        Optional<String> optImage = imageNodes.isEmpty() ? Optional.empty() : Optional.of(path(imageNodes.get(0)));
        ;

        statBlocks.add(new StatBlockHandle(id, name, statBlock, optImage, optTextStat));
      }

      Collections.sort(statBlocks);

      return statBlocks;
    }

    private Optional<String> findTextStatEntry(String referenceEntryName, String[] entries) {
      String minus = referenceEntryName.substring(0, referenceEntryName.indexOf('.'));
      String target = Entry.STAT_TEXT + minus + ".txt";
      for (String entry : entries) {
        if (entry.equals(target)) {
          return Optional.of(target);
        }
      }
      return Optional.empty();
    }

    private Map<Integer, String> resolveImages(String[] entries) {
      Map<Integer, String> images = new LinkedHashMap<>();

      for (String entry : entries) {
        if (entry.startsWith(Entry.IMAGE)) {
          try {
            String name = entry.substring(Entry.IMAGE.length());
            int at = name.indexOf('_');
            Integer id = Integer.valueOf(name.substring(0, at));
            name = name.substring(0, at + 1);
            at = name.indexOf('_');
            if (at == 1) {
              lg.debug("Image found for ID: " + id);
              images.put(id, entry);
            } else
              lg.debug("Skipping additional image: " + entry);
          } catch (Exception e) {
            lg.error("Failed to resolve image for: " + entry, e);
          }
        }
      }

      lg.debug("Found image entries: " + ObjectCommon.formatCollection(images.values()));

      return images;
    }
  }

  java.util.List<StatBlockHandle> peekSelectedCreatures() {
    int size = _handles.size();
    java.util.List<StatBlockHandle> selected = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      if (_handleChecks.get(i).isSelected())
        selected.add(_handles.get(i));
    }

    return selected;
  }

  @Override
  public String getTitle() {
    return "Portfolio Summary";
  }
}
