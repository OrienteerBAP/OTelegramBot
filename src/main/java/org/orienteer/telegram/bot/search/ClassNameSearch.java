package org.orienteer.telegram.bot.search;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import org.orienteer.telegram.bot.BotMessage;
import org.orienteer.telegram.bot.BotState;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vitaliy Gonchar
 */
public class ClassNameSearch extends Search {

    private final String searchWord;

    public ClassNameSearch(String searchWord, BotMessage botMessage) {
        super(botMessage);
        this.searchWord = searchWord;
    }

    @Override
    public ArrayList<String> execute() {
        ArrayList<String> resultList = new ArrayList<>();
        String searchClass;
        for (OClass oClass : CLASS_CACHE.values()) {
            if (isWordInLine(searchWord, oClass.getName())) {
                searchClass = String.format(botMessage.HTML_STRONG_TEXT, "•  " + botMessage.CLASS_NAME + " ") + oClass.getName() + " "
                        + BotState.GO_TO_CLASS.getCommand() + oClass.getName() + "\n";
                resultList.add(searchClass);
            }
        }
        return getResultListOfSearch(null, null, resultList);
    }
}
