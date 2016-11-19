package org.orienteer.telegram.bot;

import org.orienteer.telegram.bot.response.BotState;

import java.util.List;
import java.util.Locale;

/**
 * @author  Vitaliy Gonchar
 */
public class UserSession {
    private BotState botState;
    private BotState previousBotState;
    private String targetClass;
    private List<String> resultList;
    private BotMessage botMessage;
    private int counter;

    public UserSession() {
        botMessage = new BotMessage("en");
    }

    public void setBotMessage(BotMessage botMessage) {
        this.botMessage = botMessage;
    }

    public BotMessage getBotMessage() {
        return botMessage;
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

    public void setResultOfSearch(List<String> resultList) {
        this.resultList = resultList;
        counter = -1;
    }

    public String getNextResult() {
        counter++;
        return resultList.get(counter);
    }

    public String getPreviousResult() {
        counter--;
        return resultList.get(counter);
    }

    public boolean hasNextResult() {
        return counter < resultList.size() - 1;
    }

    public boolean hasPreviousResult() {
        return counter > 0;
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