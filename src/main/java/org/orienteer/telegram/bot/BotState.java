package org.orienteer.telegram.bot;

/**
 * @author Vitaliy Gonchar
 */
enum BotState {
    START("/start"),
    GLOBAL_FIELD_NAMES_SEARCH_BUT(BotMessage.FIELD_NAMES_BUT),
    GLOBAL_FIELD_VALUES_SEARCH_BUT(BotMessage.FIELD_VALUES_BUT),
    GLOBAL_DOC_NAMES_SEARCH_BUT(BotMessage.DOC_NAMES_BUT),

    CLASS_MENU_SEARCH_BUT(BotMessage.CLASS_SEARCH_BUT),
    CLASS_MENU_CHANGE_CLASS_BUT("/changeClass"),
    ClASS_MENU_OPTIONS("/classMenuOptions"),
    CLASS_FIELD_NAMES_SEARCH_BUT(BotMessage.CLASS_FIELD_NAMES_BUT),
    CLASS_FIELD_VALUES_SEARCH_BUT(BotMessage.CLASS_FIELD_VALUES_BUT),
    CLASS_DOC_NAMES_SEARCH_BUT(BotMessage.CLASS_DOC_NAMES_BUT),


    BACK_TO_MAIN_MENU(BotMessage.BACK_TO_MAIN_MENU_BUT),
    BACK_TO_CLASS_SEARCH(BotMessage.BACK_TO_CLASS_SEARCH_BUT),
    GO_TO_DOCUMENT("/_"),
    GO_TO_CLASS("/"),

    NEXT_RESULT(BotMessage.NEXT_RESULT_BUT),
    PREVIOUS_RESULT(BotMessage.PREVIOUS_RESULT_BUT),

    SEARCH_GLOBAL("/globalSearch"),

    SEARCH_GLOBAL_FIELD_NAMES("/searchGlobalFieldNames"),
    SEARCH_GLOBAL_FIELD_VALUES("/searchGlobalFieldValues"),
    SEARCH_GLOBAL_DOC_NAMES("/searchGlobalDocNames"),

    SEARCH_IN_CLASS_GLOBAL("/searchInClassAll"),

    SEARCH_CLASS_FIELD_NAMES("/searchClassFieldNames"),
    SEARCH_CLASS_FIELD_VALUES("/searchClassFieldValues"),
    SEARCH_CLASS_DOC_NAMES("/searchClassDocNames"),
    ERROR("/error");

    String command;

    BotState(String command) {
        this.command = command;
    }

}
