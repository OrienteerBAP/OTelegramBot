package org.orienteer.telegram.bot;

import org.orienteer.telegram.bot.response.BotState;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author  Vitaliy Gonchar
 */
public class UserSession {
    private BotState botState;
    private BotState previousBotState;
    private String targetClass;
    private Map<Integer, String> result;
    private Map<Integer, String> docLinks;
    private Locale locale;
    private int counter;
    private int start;
    private int end;
    private int results;
    private List<Integer> resultInPages;
    private final int resultsNumber = 8;
    private int page;


    public UserSession() {
        locale = new Locale("en");
        botState = BotState.START;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }

    public BotState getBotState() {
        return botState;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public void setBotState(BotState botState) {
        this.botState = botState;
    }

    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }

    public void setResultOfSearch(Map<Integer, String> result, Map<Integer, String> docLinks) {
        this.docLinks = docLinks;
        this.result = result;
        results = result.size();
        resultInPages = new ArrayList<>();
        counter = 0;
        int N = results;
        while (N > 0) {
            int temp = N;
            N -= resultsNumber;
            if (N >= 0) {
                resultInPages.add(resultsNumber);
            } else resultInPages.add(temp);
        }
        page = 0;
    }


    public int getNextPage() {
        return page < resultInPages.size() ? ++page : page;
    }

    public int getPreviousPage() {
        return page != 0 ? --page : page;
    }

    public String getResultInPage() {
        StringBuilder builder = new StringBuilder();
        int max;
        if (counter > getNumberOfResults(page)) {
            max = getNumberOfResults(page);
            counter = max - resultInPages.get(page);
        } else max = counter + resultInPages.get(page);
        start = counter;

        while (counter < max) {
            builder.append(result.get(counter));
            counter++;
        }
        end = counter;
        return builder.toString();
    }

    private int getNumberOfResults(int N) {
        int resultsInPage = resultInPages.get(N);
        return resultsInPage == resultsNumber ? resultsNumber * (N + 1) : (resultsNumber * N + resultsInPage);
    }

    public String getLink(int numberOfLink) {
        return docLinks.get(numberOfLink);
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getResultSize() {
        return result.size();
    }

    public BotState getPreviousBotState() {
        return previousBotState;
    }

    public void setPreviousBotState(BotState previousBotState) {
        this.previousBotState = previousBotState;
    }

    @Override
    public String toString() {
        return "BotState: " + botState + " targetClass: " + targetClass;
    }
}