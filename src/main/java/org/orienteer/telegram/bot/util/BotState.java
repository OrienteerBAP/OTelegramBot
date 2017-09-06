package org.orienteer.telegram.bot.util;

/**
 * Enum which contains current bot state
 */
public enum BotState {
    NEW_SEARCH(""),
    CHANGE_LANGUAGE(""),
    NEW_GLOBAL_SEARCH("/newGlobalSearch"),
    NEW_CLASS_SEARCH("/newClassSearch"),
    CLASS_SEARCH("/classMenuOptions"),
    BACK("/back"),
    GO_TO_DOCUMENT_SHORT_DESCRIPTION("_"),
    GO_TO_DOCUMENT_ALL_DESCRIPTION(""),
    GO_TO_CLASS("/"),
    NEXT_RESULT("/next"),
    PREVIOUS_RESULT("/previous"),
    SEARCH_GLOBAL("/globalSearch"),
    SEARCH_IN_CLASS_GLOBAL("/searchInClassAll"),
    ERROR("/error"),

    LINK_LIST("l"),
    LINK_SET("s"),
    LINK_MAP("m"),

    EMBEDDED("e"),
    EMBEDDED_LIST("el"),
    EMBEDDED_SET("es"),
    EMBEDDED_MAP("em"),

    DETAILS("_details"),

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

    public static BotState of(String modifier) {
        for (BotState value : values()) {
            if (value.getCommand().equals(modifier))
                return value;
        }
        return null;
    }

    @Override
    public String toString() {
        return getCommand();
    }
}
