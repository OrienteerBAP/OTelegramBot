package org.orienteer.telegram.bot.search;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import org.orienteer.telegram.bot.util.MessageKey;
import org.orienteer.telegram.bot.util.BotState;
import org.orienteer.telegram.bot.util.Markdown;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 */
public class ClassNameSearch extends Search {

    private final String searchWord;

    public ClassNameSearch(String searchWord, Locale locale) {
        super(locale);
        this.searchWord = searchWord;
    }

    @Override
    public Result execute() {
        List<String> resultList = new ArrayList<>();
        String searchClass;
        for (OClass oClass : classCache.values()) {
            if (isWordInLine(searchWord, oClass.getName())) {
                searchClass = Markdown.BOLD.toString(MessageKey.CLASS_NAME.getString() + " ") + oClass.getName() + " "
                        + BotState.GO_TO_CLASS.getCommand() + oClass.getName() + "\n";
                resultList.add(searchClass);
            }
        }
        return getResultOfSearch(null, null, resultList, null);
    }
}
