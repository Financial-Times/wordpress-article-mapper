package com.ft.wordpressarticlemapper.response;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum WordPressPostType {
  POST("post"),
  MARKETS_LIVE("webchat-markets-live"),
  LIVE_BLOG("webchat-live-blogs"),
  LIVE_Q_AND_A("webchat-live-qa");

  private static Set<String> stringValues;

  private String postType;

  WordPressPostType(String apiPostType) {
    this.postType = apiPostType;
  }

  public String getApiPostType() {
    return postType;
  }

  public static Set<String> stringValues() {
    if (stringValues == null) {
      Set<String> postTypes =
          EnumSet.allOf(WordPressPostType.class).stream()
              .map(WordPressPostType::getApiPostType)
              .collect(Collectors.toSet());

      stringValues = Collections.unmodifiableSet(postTypes);
    }

    return stringValues;
  }

  public static WordPressPostType fromString(String in) {
    if (in == null) {
      return null;
    }

    for (WordPressPostType t : WordPressPostType.values()) {
      if (t.postType.equals(in)) {
        return t;
      }
    }

    throw new IllegalArgumentException(String.format("No WordPressPostType enum value for %s", in));
  }
}
