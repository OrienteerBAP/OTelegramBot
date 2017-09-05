package org.orienteer.telegram.bot.search;

import com.google.common.base.Strings;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import org.orienteer.telegram.bot.Cache;
import org.orienteer.telegram.bot.util.MessageKey;
import org.orienteer.telegram.bot.util.Markdown;

import java.util.*;

/**
 * Abstract class for search document in class
 */
public abstract class Search {

    protected final Map<String, OClass> classCache = Cache.getClassCache();
    protected final Map<String, String> queryCache = Cache.getQueryCache();
    protected final Locale locale;
    private Map<Integer, String> resultOfSearch;
    private Map<Integer, String> docLinks;
    private int counter = 0;

    public Search(Locale locale) {
        this.locale = locale;
    }

    public abstract Result execute();

    public static Search getSearch(String searchWord, String className, Locale locale) {
        return className == null ? new ClassNameSearch(searchWord, locale) : new ClassSearch(searchWord, className, locale);
    }

    protected Result getResultOfSearch(List<String> values,
                                       List<String> docs, List<String> classes, List<String> links) {
        resultOfSearch = new HashMap<>();
        docLinks = new HashMap<>();
        if (values == null) values = new ArrayList<>();
        if (docs == null) docs = new ArrayList<>();
        if (classes == null) classes = new ArrayList<>();

        String info = "\n" + Markdown.BOLD.toString(MessageKey.SEARCH_RESULT.getString()) + "\n";

        if (values.size() > 0 || docs.size() > 0 || classes.size() > 0) {

            if (classes.size() > 0) {
                buildResult(classes, null, info);
            }
            if (docs.size() > 0) {
                buildResult(docs, links, info);
            }
            if (values.size() > 0) {
                buildResult(values, links, info);
            }
        } else resultOfSearch.put(0, Markdown.BOLD.toString(MessageKey.SEARCH_RESULT_FAILED_MSG.getString()));
        return new Result(resultOfSearch, docLinks);
    }

    private void buildResult(List<String> list, List<String> links, String info) {
        boolean isStart = true;
        for (String string : list) {
            if (!Strings.isNullOrEmpty(string)) {
                if (links != null) docLinks.put(counter, links.get(counter));
                if (isStart) {
                    string = info + Markdown.BOLD.toString((counter + 1) + ".  ") + string;
                    isStart = false;
                    resultOfSearch.put(counter, string);
                } else resultOfSearch.put(counter, Markdown.BOLD.toString((counter + 1) + ".  ") + string);
                counter++;
            }
        }
    }


    protected boolean isWordInLine(final String word, String line) {
        boolean isIn = false;
        if (line.toLowerCase().contains(word.toLowerCase())) isIn = true;
        return isIn;
    }
}
