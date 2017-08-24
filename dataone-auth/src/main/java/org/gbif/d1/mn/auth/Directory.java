package org.gbif.d1.mn.auth;

import java.util.LinkedHashMap;
import java.util.Map;

public class Directory {

  public static void main(String[] args) {
    Map<String, String> people = new LinkedHashMap<>();
    people.put("S1", "Charles Darwin");
    people.put("S2", "Alexander von Humboldt");
    people.put("C1", "Charles Chaplin");
  }

  public String findFirstComedian(Map<String,String> people) {
    for (String key : people.keySet()) {
     if (key.startsWith("C")) {
       return people.get(key);
     }
    }
    return null;
  }


  public String findFirstComedianBetter(Map<String,String> people) {
    for (Map.Entry<String,String> entry : people.entrySet()) {
      if (entry.getKey().startsWith("C")) {
        return entry.getValue();
      }
    }
    return null;
  }


  public String findFirstComedianBetterJava8(Map<String,String> people) {
    return people.entrySet().stream()
      .filter(entry -> entry.getKey().startsWith("C"))
      .map(Map.Entry::getValue).findFirst().orElse(null);
  }
}
