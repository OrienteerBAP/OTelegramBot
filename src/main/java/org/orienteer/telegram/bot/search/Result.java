package org.orienteer.telegram.bot.search;

import java.util.Map;

/**
 * Class which contains result of search
 */
public class Result {
    private final Map<Integer, String> resultOfSearch;
    private final Map<Integer, String> docLinks;

    public Result(Map<Integer, String> resultOfSearch, Map<Integer, String> docLinks) {
        this.resultOfSearch = resultOfSearch;
        this.docLinks = docLinks;
    }

    public Map<Integer, String> getResultOfSearch() {
        return resultOfSearch;
    }

    public Map<Integer, String> getDocLinks() {
        return docLinks;
    }
}
