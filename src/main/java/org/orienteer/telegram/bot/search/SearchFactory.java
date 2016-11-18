package org.orienteer.telegram.bot.search;

import org.orienteer.telegram.bot.BotMessage;

/**
 * @author Vitaliy Gonchar
 */
public abstract class SearchFactory {

    /**
     * Get search in class
     * @param searchWord
     * @param className
     * @param botMessage
     * @return
     */
    public Search getSearch(String searchWord, String className, BotMessage botMessage) {
        ClassSearch classSearch = new ClassSearch();
        return classSearch;
    }

    /**
     * Global search is in develop mode.
     * @param searchWord
     * @param botMessage
     * @return
     */
    public Search getSearch(String searchWord, BotMessage botMessage) {
        GlobalSearch globalSearch = new GlobalSearch();
        return globalSearch;
    }
}
