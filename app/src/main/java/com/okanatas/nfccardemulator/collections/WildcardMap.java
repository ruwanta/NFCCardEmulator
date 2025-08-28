package com.okanatas.nfccardemulator.collections;

import java.util.*;
import java.util.regex.*;

/**
 * A map where key can have a wildcard characters.
 *
 * (e.g., "foo*bar", "*.txt", "user??")
 *
 * @param <V>
 */
public class WildcardMap<V> {
    private final Map<String, V> rawMap = new LinkedHashMap<>();
    private final Map<Pattern, V> patternMap = new LinkedHashMap<>();

    // Convert wildcard to regex
    private Pattern wildcardToRegex(String wildcard) {
        String regex = "^" +
                wildcard.replace(".", "\\.")   // escape dot
                        .replace("?", ".")     // ? → single char
                        .replace("*", ".*")    // * → any chars
                + "$";
        return Pattern.compile(regex);
    }

    // Put a wildcard key
    public void put(String wildcard, V value) {
        rawMap.put(wildcard, value);
        patternMap.put(wildcardToRegex(wildcard), value);
    }

    // Get value by input string
    public V get(String input) {
        for (Map.Entry<Pattern, V> entry : patternMap.entrySet()) {
            if (entry.getKey().matcher(input).matches()) {
                return entry.getValue();
            }
        }
        return null;
    }

    // Optional: get all matches
    public List<V> getAllMatches(String input) {
        List<V> results = new ArrayList<>();
        for (Map.Entry<Pattern, V> entry : patternMap.entrySet()) {
            if (entry.getKey().matcher(input).matches()) {
                results.add(entry.getValue());
            }
        }
        return results;
    }

    public boolean containsKey(String input) {
        return get(input) != null;
    }

    public void clear() {
        rawMap.clear();
        patternMap.clear();
    }
}

