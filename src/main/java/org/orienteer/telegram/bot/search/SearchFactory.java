package org.orienteer.telegram.bot.search;

import java.util.Locale;

/**
 * @author Vitaliy Gonchar
 */
public abstract class SearchFactory {


    public static Search getSearch(String searchWord, String className, Locale locale) {
        ClassSearch classSearch = new ClassSearch(searchWord, className, locale);
        return classSearch;
    }

    public static Search getSearch(String searchWord, Locale locale) {
        ClassNameSearch classNameSearch = new ClassNameSearch(searchWord, locale);
        return classNameSearch;
    }
}
