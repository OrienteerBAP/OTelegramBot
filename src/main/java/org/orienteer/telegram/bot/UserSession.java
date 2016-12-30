package org.orienteer.telegram.bot;

import org.orienteer.telegram.bot.response.BotState;

import java.util.*;

/**
 * @author  Vitaliy Gonchar
 */
public class UserSession {
    private BotState botState;
    private BotState previousBotState;
    private String targetClass;
    private Map<Integer, String> result;
    private Locale locale;
    private int start;
    private int end;
    private List<Integer> resultInPages;
    private final int resultsNumber = 10;


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

    public void setResultOfSearch(Map<Integer, String> result) {
        resultInPages = new ArrayList<>();
        this.result = new HashMap<>();
        int N = result.size();
        int number = 0;
        int counter = 0;
        while (N > 0) {
            int temp = N;
            N -= resultsNumber;
            if (N >= 0) {
                temp = resultsNumber;
                resultInPages.add(resultsNumber);
            } else resultInPages.add(temp);

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < temp; i++) {
                builder.append(result.get(counter));
                counter++;
            }
            this.result.put(number, builder.toString());
            number++;
        }

        start = 0;
        end = start + 8 < resultInPages.size() ? 8: resultInPages.size();
    }


    public int getNextPages() {
        start = start + 8 < resultInPages.size() ? start + 8 : start;
        end = end + 8 < resultInPages.size() ? end + 8: resultInPages.size();
        return start;
    }

    public int getPreviousPages() {
        start = start - 8 >= 0 ? start - 8 : start;
        end = start + 8 < resultInPages.size() ? start + 8: resultInPages.size();
        return start;
    }

    public String getResultInPage(int page) {
        return result.get(page);
    }

    public String getResultInPage() {
        return result.get(0);
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getPages() {
        return resultInPages.size();
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