package org.orienteer.telegram.bot.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import org.orienteer.telegram.bot.Cache;

import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Abstract class for search document in class
 */
public abstract class AbstractSearch {
    protected final Map<String, OClass> classCache;
    protected final Map<String, String> queryCache;
    private int counter = 0;

    public AbstractSearch() {
        classCache = Cache.getClassCache();
        queryCache = Cache.getQueryCache();
    }

    public abstract Map<Integer, String> search();

    public static AbstractSearch newSearch(String searchWord, String className) {
        return className == null ? new OClassNameSearch(searchWord) : new OClassDocumentSearch(searchWord, className);
    }

    protected Map<Integer, String> newSearchResult(List<String> values, String headInfo) {
        Map<Integer, String> result = Maps.newHashMap();
        if (values != null && !values.isEmpty()) {
            boolean isStart = true;
            for (String value : values) {
                if (!Strings.isNullOrEmpty(value)) {
                    if (isStart) {
                        value = headInfo + value;
                        isStart = false;
                        result.put(counter, value);
                    } else result.put(counter, Markdown.BOLD.toString((counter + 1) + ".  ") + value);
                    counter++;
                }
            }
        } else result.put(0, Markdown.BOLD.toString(MessageKey.SEARCH_RESULT_FAILED_MSG.toLocaleString()));
        return result;
    }

    protected boolean isWordInLine(final String word, String line) {
        return line.toUpperCase().contains(word.toUpperCase());
    }

    protected String createSearchResultString(String word, String value) {
        Stack<Character> stack = new Stack<>();
        int counter = 1;
        char currentChar = word.charAt(0);
        List<SearchPoint> points = Lists.newArrayList();
        value = OTelegramUtil.makeStringNonMarkdown(value);
        for (int i = 0; i < value.length(); i++) {
            char valueChar = value.charAt(i);
            if (equalsCharIgnoreCase(valueChar, currentChar)) {
                if (stack.isEmpty()) {
                    points.add(new SearchPoint(i));
                }
                stack.push(valueChar);
                if (counter == word.length()) {
                    points.get(points.size() - 1).end = i;
                    stack.clear();
                    counter = 0;
                }
                currentChar = word.charAt(counter++);
            } else if (!stack.isEmpty()) {
                if (equalsCharIgnoreCase(stack.peek(), valueChar)) {
                    points.get(points.size() - 1).start = i;
                } else {
                    counter = 1;
                    currentChar = word.charAt(0);
                    stack.clear();
                }
            }
        }
        return createStringResult(value, points);
    }

    private String createStringResult(String value, List<SearchPoint> points) {

        for (int i = 0; i < points.size(); i++) {
            SearchPoint point = points.get(i);
            if (point.start > -1 && point.end > -1) {
                value = value.substring(0, point.start) + Markdown.BOLD.toString(value.substring(point.start, point.end + 1)) + value.substring(point.end + 1);
                for (int j = i + 1; j < points.size(); j++) {
                    point = points.get(j);
                    if (point.start > -1 && point.end > -1) {
                        point.start += 2;
                        point.end += 2;
                    }
                }
            }
        }
        return value;
    }

    private static class SearchPoint {
        private int start = -1;
        private int end = -1;

        public SearchPoint(int start) {
            this.start = start;
        }

        public SearchPoint(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return "SearchPoint{" +
                    "start=" + start +
                    ", end=" + end +
                    '}';
        }
    }

    private boolean equalsCharIgnoreCase(char c1, char c2) {
        return Character.toLowerCase(c1) == Character.toLowerCase(c2);
    }
}
