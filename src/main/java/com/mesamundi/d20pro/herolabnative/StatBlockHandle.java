package com.mesamundi.d20pro.herolabnative;

import java.util.Optional;

/**
 * @author thraxxis
 */
final class StatBlockHandle implements Comparable<StatBlockHandle> {
  private final int id;
  private final String _filename;
  private final String _displayName;
  private final byte[] _data;
  private final Optional<String> _imageFilename;
  private final Optional<String> _textStatFilename;

  StatBlockHandle(int id, String filename, byte[] data, Optional<String> optImageFilename, Optional<String> optTextStatFilename) {
    this.id = id;
    _filename = filename;
    _displayName = resolveDisplayName(filename);
    _data = data;
    _imageFilename = optImageFilename;
    _textStatFilename = optTextStatFilename;
  }

  static String resolveDisplayName(String filename) {
    String displayName = filename.substring(filename.indexOf('_') + 1);
    displayName = displayName.substring(0, displayName.length() - ".xml".length());
    return displayName;
  }

  public int getId() {
    return id;
  }

  public String getFilename() {
    return _filename;
  }

  public String getDisplayName() {
    return _displayName;
  }

  public byte[] getData() {
    return _data;
  }

  public Optional<String> getImageFilename() {
    return _imageFilename;
  }

  public Optional<String> getTextStatFilename() {
    return _textStatFilename;
  }

  @Override
  public String toString() {
    return _displayName;
  }

  @Override
  public int compareTo(StatBlockHandle o) {
    return Integer.compare(id, o.id);
  }
}
