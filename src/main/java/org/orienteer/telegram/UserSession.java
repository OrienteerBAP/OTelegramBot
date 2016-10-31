package org.orienteer.telegram;

/**
 * Created by Vitaliy Gonchar on 31.10.16.
 */
class UserSession {
    private BotState botState;
    private String targetClass;


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
}