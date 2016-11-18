package org.orienteer.telegram.bot.search;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import org.orienteer.telegram.bot.BotMessage;
import org.orienteer.telegram.bot.Cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Vitaliy Gonchar
 */
public abstract class Search {

    protected final Map<String, OClass> CLASS_CACHE = Cache.getClassCache();
    protected final Map<String, String> QUERY_CACHE = Cache.getQueryCache();
    protected final BotMessage botMessage;
    private final int N = 10;   // max result in one message

    public Search(BotMessage botMessage) {
        this.botMessage = botMessage;
    }

    public abstract ArrayList<String> execute();

    protected ArrayList<String> getResultListOfSearch(ArrayList<String> values,
                                               ArrayList<String> docs, ArrayList<String> classes) {
        ArrayList<String> resultList = new ArrayList<>();
        if (values == null) values = new ArrayList<>();
        if (docs == null) docs = new ArrayList<>();
        if (classes == null) classes = new ArrayList<>();

        if (values.size() > 0 || docs.size() > 0 || classes.size() > 0) {
            int counter = 0;
            if (classes.size() > 0) {
                String info = "\n" + String.format(botMessage.HTML_STRONG_TEXT, botMessage.SEARCH_CLASS_NAMES_RESULT) + "\n";
                resultList.addAll(splitBigResult(classes, info, counter));
            }
            if (docs.size() > 0) {
                String info = "\n" + String.format(botMessage.HTML_STRONG_TEXT, botMessage.SEARCH_DOCUMENT_NAMES_RESULT) + "\n";
                resultList.addAll(splitBigResult(docs, info, counter));
            }
            if (values.size() > 0) {
                String info = "\n" + String.format(botMessage.HTML_STRONG_TEXT, botMessage.SEARCH_FIELD_VALUES_RESULT) + "\n";
                resultList.addAll(splitBigResult(values, info, counter));
            }
        } else resultList.add(String.format(botMessage.HTML_STRONG_TEXT, botMessage.SEARCH_RESULT_FAILED_MSG));
        return resultList;
    }

    private ArrayList<String> splitBigResult(List<String> bigResult, String info, int counter) {
        ArrayList<String> resultList = new ArrayList<>();
        String head = String.format(botMessage.HTML_STRONG_TEXT, botMessage.SEARCH_RESULT_SUCCESS_MSG);
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

    /**
     * getResultOfSearch word in line
     * @param word getResultOfSearch word
     * @param line string where word can be
     * @return true if word is in line
     */
    protected boolean isWordInLine(final String word, String line) {
        boolean isIn = false;
        if (line.toLowerCase().contains(word.toLowerCase())) isIn = true;
        return isIn;
    }
}
