package org.orienteer.telegram.bot;

/**
 * @author Vitaliy Gonchar
 */
enum BotState {
    NEW_SEARCH(""),
    NEW_GLOBAL_SEARCH(BotMessage.NEW_GLOBAL_SEARCH_BUT),
    NEW_CLASS_SEARCH(BotMessage.NEW_CLASS_SEARCH_BUT),
    CLASS_SEARCH("/classMenuOptions"),
    BACK(BotMessage.BACK),
    GO_TO_DOCUMENT("/_"),
    GO_TO_CLASS("/"),
    NEXT_RESULT(BotMessage.NEXT_RESULT_BUT),
    PREVIOUS_RESULT(BotMessage.PREVIOUS_RESULT_BUT),
    SEARCH_GLOBAL("/globalSearch"),
    SEARCH_IN_CLASS_GLOBAL("/searchInClassAll"),
    ERROR("/error"),

    START("/start"),
    LANGUAGE("/language"),
    ABOUT("/about");

    String command;

    BotState(String command) {
        this.command = command;
    }

}
