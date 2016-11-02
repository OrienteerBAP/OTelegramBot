package org.orienteer.telegram;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author  Vitaliy Gonchar
 */
class UserSession {
    private BotState botState;
    private String targetClass;
    private ListIterator<String> resultIterator;

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

    public void setResultIterator(List<String> resultOfSearch) {
        this.resultIterator = resultOfSearch.listIterator();
    }

    public String getNextResult() {
        if (resultIterator.hasNext()) {
            return resultIterator.next();
        } else return BotMessage.END_OF_RESULT;
    }


    public String getPerviousResult() {
        if (resultIterator.hasPrevious()) {
            return resultIterator.previous();
        } else return BotMessage.START_OF_RESULT;
    }

    @Override
    public String toString() {
        return "BotState: " + botState + " targetClass: " + targetClass;
    }
}