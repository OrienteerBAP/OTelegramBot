package org.orienteer.telegram;

/**
 * @author  Vitaliy Gonchar
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

    @Override
    public String toString() {
        return "BotState: " + botState + " targetClass: " + targetClass;
    }
}