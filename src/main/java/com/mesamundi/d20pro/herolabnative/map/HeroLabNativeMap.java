package com.mesamundi.d20pro.herolabnative.map;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * Utility for loading mapping .json files.
 *
 * @author thraxxis
 */
public class HeroLabNativeMap {
  private static final Logger lg = Logger.getLogger(HeroLabNativeMap.class);

  private static final Map<String, HeroLabNativeMap> _cache = new HashMap<>();

  private final Map<String, List<String>> _data = new LinkedHashMap<>();

  public static HeroLabNativeMap templates() {
    return make("templates");
  }

  private static URL locate(String name) {
    String path = HeroLabNativeMap.class.getName().replace('.', '/');
    path = path.substring(0, path.lastIndexOf("/"));
    String fullname = path + "/" + name + ".json";
    URL url = HeroLabNativeMap.class.getClassLoader().getResource(fullname);
    if (null == url)
      throw new RuntimeException("Failed to find in classpath: " + fullname);
    return url;
  }

  private static HeroLabNativeMap make(String name) {
    HeroLabNativeMap cached = _cache.get(name);
    if (null != cached)
      return cached;

    InputStream inputStream = null;
    try {

      HeroLabNativeMap map = new HeroLabNativeMap();

      inputStream = HeroLabNativeMap.class.getClassLoader().getResourceAsStream(locate(name).toExternalForm());

      JsonFactory factory = new JsonFactory();
      JsonParser parser = factory.createParser(inputStream);

      // continue parsing the token till the end of input is reached
      while (!parser.isClosed()) {
        // get the token
        JsonToken token = parser.nextToken();
        // if its the last token then we are done
        if (token == null)
          break;
        // we want to look for a field that says dataset

        if (JsonToken.FIELD_NAME.equals(token)) {
          String key = parser.getCurrentName();
          List<String> values = new ArrayList<>();
          map._data.put(key, values);

          token = parser.nextToken();
          if (!JsonToken.START_ARRAY.equals(token)) {
            break;
          }

          while (true) {
            token = parser.nextToken();
            if (JsonToken.END_ARRAY.equals(token))
              break;
            values.add(parser.getText());
          }

        }
      }
      _cache.put(name, map);
      return map;
    } catch (Exception e) {
      lg.error("Failed to access mapping file: " + name, e);
      return new HeroLabNativeMap();
    } finally {
      if (null != inputStream)
        try {
          inputStream.close();
        } catch (IOException e) {
          lg.warn("Failed to close stream", e);
        }
    }
  }

  private HeroLabNativeMap() {
  }

  public Optional<String> peekKeyForValue(String value) {
    for (Map.Entry<String, List<String>> entry : _data.entrySet()) {
      for (String v : entry.getValue()) {
        if (v.equalsIgnoreCase(value))
          return Optional.of(entry.getKey());
      }
    }
    return Optional.empty();
  }

  public static void main(String[] args) {
    templates();
  }
}
