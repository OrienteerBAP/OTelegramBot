package org.orienteer.telegram;

/**
 * Created by Vitaliy Gonchar on 31.10.16.
 */
class UserSession {
    private BotState botState;
    private String targetClass;
    private boolean isSearch = false;

    public BotState getBotState() {
        return botState;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public boolean getSearch() { return isSearch; }

    public void setBotState(BotState botState) {
        this.botState = botState;
    }

    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }

    public void setSearch(boolean isSearch) { this.isSearch = isSearch; }

    @Override
    public String toString() {
        return "BotState: " + botState + " targetClass: " + targetClass + " isSearch: " + isSearch;
    }
}