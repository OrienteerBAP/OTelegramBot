package org.orienteer.telegram.bot;

/**
 * @author Vitaliy Gonchar
 */
public enum BotState {
    NEW_SEARCH(""),
    NEW_GLOBAL_SEARCH("/newGlobalSearch"),
    NEW_CLASS_SEARCH("/newClassSearch"),
    CLASS_SEARCH("/classMenuOptions"),
    BACK("/back"),
    GO_TO_DOCUMENT_SHORT_DESCRIPTION("/_"),
    GO_TO_DOCUMENT_ALL_DESCRIPTION("/__details"),
    GO_TO_CLASS("/"),
    NEXT_RESULT("/next"),
    PREVIOUS_RESULT("/previous"),
    SEARCH_GLOBAL("/globalSearch"),
    SEARCH_IN_CLASS_GLOBAL("/searchInClassAll"),
    ERROR("/error"),

    START("/start"),
    LANGUAGE("/language"),
    ABOUT("/about");

    private String command;

    BotState(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
