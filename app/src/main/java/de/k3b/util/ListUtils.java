/*
 * Copyright (c) 2017-2020 by k3b.
 *
 * This file is part of #APhotoManager (https://github.com/k3b/APhotoManager/)
 *              and #toGoZip (https://github.com/k3b/ToGoZip/) and AndroidGeo2ArticlesMap https://github.com/k3b/AndroidGeo2ArticlesMap .
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package de.k3b.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Helper to handle List-s
 * Created by k3b on 07.01.2017.
 */

public class ListUtils {
    private static final String DEFAULT_LIST_ELEMENT_DELIMITER = ",";

    public static List<String> fromString(String stringWithElements) {
        return fromString(stringWithElements, DEFAULT_LIST_ELEMENT_DELIMITER);
    }

    public static List<String> fromString(String stringWithElements, String elementDelimiter) {
        return Arrays.asList(stringWithElements.split(elementDelimiter));
    }

    public static String toString(List<?> list) {
        return toString(DEFAULT_LIST_ELEMENT_DELIMITER, list);
    }

    public static List<String> toStringList(Iterable<?> list) {
        ArrayList<String> result = new ArrayList<>();
        for (Object item : list) {
            if (item != null) result.add(item.toString());
        }
        return result;
    }

    public static List<String> toStringList(Object... list) {
        ArrayList<String> result = new ArrayList<>();

        if (list != null) {
            for (Object item : list) {
                if (item != null) result.add(item.toString());
            }
        }
        return result;
    }

    public static String toString(String elementDelimiter, Collection<?> list) {
        StringBuilder result = new StringBuilder();
        if (list != null) {
            String nextDelim = "";
            for (Object instance : list) {
                if (instance != null) {
                    String instanceString = instance.toString().trim();
                    if (instanceString.length() > 0) {
                        result.append(nextDelim).append(instanceString);
                        nextDelim = elementDelimiter;
                    }
                }
            }
        }
        return result.toString();
    }

    public static String toString(String elementDelimiter, Object... list) {
        StringBuilder result = new StringBuilder();
        if (list != null) {
            String nextDelim = "";
            for (Object instance : list) {
                if (instance != null) {
                    CharSequence instanceString = (instance instanceof CharSequence) ? ((CharSequence) instance) : instance.toString();
                    if (instanceString.length() > 0) {
                        result.append(nextDelim).append(instanceString);
                        nextDelim = elementDelimiter;
                    }
                }
            }
        }
        return result.toString();
    }

    public static String[] asStringArray(List<String> tags) {
        if ((tags == null) || (tags.size() == 0)) return null;
        String[] tagsArray = tags.toArray(new String[0]);
        return tagsArray;
    }

    /**
     * return null if list has no elements
     */
    public static <T> List<T> emptyAsNull(List<T> list) {
        if ((list != null) && (list.size() > 0)) return list;
        return null;
    }

    /** add item "itemToBeAddedIfNotInList" to list if it is not in the list yet */
    public static <T> List<T> include(List<T> list, T itemToBeAddedIfNotInList) {
        if ((itemToBeAddedIfNotInList != null) && (!list.contains(itemToBeAddedIfNotInList))) {
            list.add(itemToBeAddedIfNotInList);
        }
        return list;
    }

    /** add all items from "itemsToBeAddedIfNotInList" to list that are not in the list yet */
    public static <T> List<T> include(List<T> list, Collection<T> itemsToBeAddedIfNotInList) {
        if (itemsToBeAddedIfNotInList != null) {
            for (T itemToBeAddedIfNotInList : itemsToBeAddedIfNotInList) {
                include(list, itemToBeAddedIfNotInList);
            }
        }
        return list;
    }
}
