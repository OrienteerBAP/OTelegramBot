package org.orienteer.telegram.bot.search;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import org.orienteer.telegram.bot.Cache;
import org.orienteer.telegram.bot.MessageKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Vitaliy Gonchar
 */
public abstract class Search {

    protected final Map<String, OClass> CLASS_CACHE = Cache.getClassCache();
    protected final Map<String, String> QUERY_CACHE = Cache.getQueryCache();
    protected final Locale locale;
    private final int N = 10;   // max result in one message

    public Search(Locale locale) {
        this.locale = locale;
    }

    public abstract List<String> execute();

    protected List<String> getResultListOfSearch(List<String> values,
                                               List<String> docs, List<String> classes) {
        List<String> resultList = new ArrayList<>();
        if (values == null) values = new ArrayList<>();
        if (docs == null) docs = new ArrayList<>();
        if (classes == null) classes = new ArrayList<>();

        if (values.size() > 0 || docs.size() > 0 || classes.size() > 0) {
            int counter = 0;
            if (classes.size() > 0) {
                String info = "\n" + String.format(MessageKey.HTML_STRONG_TEXT.toString(), MessageKey.SEARCH_CLASS_NAMES_RESULT.getString(locale)) + "\n";
                resultList.addAll(splitBigResult(classes, info, counter));
            }
            if (docs.size() > 0) {
                String info = "\n" + String.format(MessageKey.HTML_STRONG_TEXT.toString(), MessageKey.SEARCH_DOCUMENT_NAMES_RESULT.getString(locale)) + "\n";
                resultList.addAll(splitBigResult(docs, info, counter));
            }
            if (values.size() > 0) {
                String info = "\n" + String.format(MessageKey.HTML_STRONG_TEXT.toString(), MessageKey.SEARCH_FIELD_VALUES_RESULT.getString(locale)) + "\n";
                resultList.addAll(splitBigResult(values, info, counter));
            }
        } else resultList.add(String.format(MessageKey.HTML_STRONG_TEXT.toString(), MessageKey.SEARCH_RESULT_FAILED_MSG.getString(locale)));
        return resultList;
    }

    private List<String> splitBigResult(List<String> bigResult, String info, int counter) {
        List<String> resultList = new ArrayList<>();
        String head = String.format(MessageKey.HTML_STRONG_TEXT.toString(), MessageKey.SEARCH_RESULT_SUCCESS_MSG.getString(locale));
        StringBuilder builder = new StringBuilder();
        builder.append(head);
        builder.append(info);
        for (String string : bigResult) {
            builder.append(string);
            counter++;
            if (counter % N == 0) {
                resultList.add(builder.toString());
                builder = new StringBuilder();
                builder.append(head);
                builder.append(info);
            }
        }
        if (counter % N != 0) resultList.add(builder.toString());
        return resultList;
    }


    protected boolean isWordInLine(final String word, String line) {
        boolean isIn = false;
        if (line.toLowerCase().contains(word.toLowerCase())) isIn = true;
        return isIn;
    }
}
