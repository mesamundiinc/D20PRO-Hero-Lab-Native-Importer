package com.mesamundi.d20pro.herolabnative;

import java.util.function.Consumer;
import java.util.zip.ZipException;

import org.w3c.dom.Document;

import com.d20pro.plugin.api.XMLToDocumentHelper;
import com.mesamundi.common.FileCommon;
import com.mesamundi.common.util.Zipper;
import com.sengent.common.control.exception.UserVisibleException;

import static com.d20pro.plugin.api.XMLToDocumentHelper.peekValueForNamedItem;

/**
 * Created by Mat on 11/27/2017.
 */
public final class GameInfo {
  private static final String INDEX_XML = "index.xml";

  static class GameInfoNotAvailableException extends UserVisibleException {
    public GameInfoNotAvailableException(String message) {
      super(message);
    }

    GameInfoNotAvailableException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  private String _name;
  private String _folder;
  private String _publisher;
  private String _url;

  /**
   * Only way to construct is by calling {@link #extract(Zipper)}.
   */
  private GameInfo() {
  }

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public String getFolder() {
    return _folder;
  }

  public void setFolder(String folder) {
    _folder = folder;
  }

  public String getPublisher() {
    return _publisher;
  }

  public void setPublisher(String publisher) {
    _publisher = publisher;
  }

  public String getUrl() {
    return _url;
  }

  public void setUrl(String url) {
    _url = url;
  }

  static GameInfo extract(Zipper zipper) throws GameInfoNotAvailableException {
    try {
      boolean exists = zipper.entryExists(INDEX_XML);
      if (!exists)
        throw new GameInfoNotAvailableException("index.xml not found in Portfolio");
    } catch (ZipException e) {
      throw new GameInfoNotAvailableException("Failed to find: " + INDEX_XML, e);
    }

    byte[] bytes;
    try {
      bytes = FileCommon.ZipUtil.extractFileBytesFromArchive(zipper.peekSourceFile(), INDEX_XML);
    } catch (Exception e) {
      throw new GameInfoNotAvailableException("Failed to extract: " + INDEX_XML, e);
    }

    Document document;
    try {
      document = XMLToDocumentHelper.loadDocument(bytes);
    } catch (Exception e) {
      throw new GameInfoNotAvailableException("Failed to load: " + INDEX_XML, e);
    }

    GameInfo info = new GameInfo();

    try {
      peekValueForNamedItem(document, "document/game", "name");
    } catch (UserVisibleException e) {
      throw new GameInfoNotAvailableException("Failed to find: " + "name", e);
    }

    apply(document, "name", info::setName);
    apply(document, "folder", info::setFolder);
    apply(document, "publisher", info::setPublisher);
    apply(document, "url", info::setUrl);

    return info;
  }

  private static void apply(Document doc, String name, Consumer<String> dest) throws GameInfoNotAvailableException {
    try {
      String value = peekValueForNamedItem(doc, "document/game", name);
      dest.accept(value);
    } catch (UserVisibleException e) {
      throw new GameInfoNotAvailableException("Failed to find: " + name, e);
    }
  }

  private boolean isFolder(String target) {
    return _folder.equalsIgnoreCase(target);
  }

  public boolean is3_5() {
    // TODO: 11/27/2017 enter 3.5 data
    return isFolder("?");
  }

  public boolean isPathfinder() {
    return isFolder("Pathfinder");
  }

  public boolean is5e() {
    return isFolder("5e");
  }

  @Override
  public String toString() {
    return "GameInfo{" +
            "name='" + _name + '\'' +
            ", folder='" + _folder + '\'' +
            ", publisher='" + _publisher + '\'' +
            ", url='" + _url + '\'' +
            '}';
  }
}
