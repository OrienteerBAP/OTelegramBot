package org.orienteer.telegram.bot.util;

import org.apache.wicket.Localizer;
import org.orienteer.telegram.bot.AbstractOTelegramBot;

/**
 * Contains keys for creating localized messages
 */
public enum MessageKey {
    CLASS_MENU_MSG("telegram.bot.menu.classesMenu"),
    CLASS_MENU_EMPTY_MSG("telegram.bot.menu.classesMenu.empty"),
    LANGUAGE_MENU_MSG("telegram.bot.menu.language"),
    LANGUAGE_BUT("telegram.bot.button.language"),
    LANGUAGE_CHANGED("telegram.bot.message.languageChanged"),
    CLASS_BUT("telegram.bot.button.class"),
    ALL_DOCUMENT_DSCR_BUT("telegram.bot.button.allDescription"),
    SHORT_DOCUMENT_DSCR_BUT("telegram.bot.button.shortDescription"),
    NEXT_RESULT_BUT("telegram.bot.button.next"),
    PREVIOUS_RESULT_BUT("telegram.bot.button.previous"),
    START_SEARCH_MSG("telegram.bot.message.startSearch"),
    SEARCH_RESULT_SUCCESS_MSG("telegram.bot.message.searchSuccess"),
    SEARCH_RESULT_FAILED_MSG("telegram.bot.message.searchFailed"),
    ERROR_MSG("telegram.bot.message.error"),
    CLASS_SEARCH_MSG("telegram.bot.message.classSearch"),
    CLASS_DESCRIPTION_MSG("telegram.bot.message.classDescription"),
    CLASS_DOCUMENTS("telegram.bot.message.classDocuments"),
    SHORT_DOCUMENT_DESCRIPTION_MSG("telegram.bot.message.shortDocumentDescription"),
    DOCUMENT_DETAILS_MSG("telegram.bot.message.detailDocumentDescription"),
    SEARCH_FIELD_VALUES_RESULT("telegram.bot.message.searchFieldValues"),
    SEARCH_RESULT("telegram.bot.message.searchResult"),
    BACK("telegram.bot.button.back"),
    SEARCH_FAILED_CLASS_BY_NAME("telegram.bot.message.failedSearchClassByName"),
    FAILED_DOCUMENT_BY_RID("telegram.bot.message.failedSearchDocumentByRID"),
    SEARCH_DOCUMENT_NAMES_RESULT("telegram.bot.message.searchDocumentNamesResult"),
    SEARCH_CLASS_NAMES_RESULT("telegram.bot.message.searchClassNamesResult"),
    NAME("telegram.bot.message.name"),
    SUPER_CLASSES("telegram.bot.message.superClasses"),
    WITHOUT_SUPER_CLASSES("telegram.bot.message.withoutSuperClasses"),
    WITHOUT_PROPERTIES("telegram.bot.message.withoutProperties"),
    WITHOUT_DOCUMENTS("telegram.bot.message.withoutDocuments"),
    DEFAULT_VALUE("telegram.bot.message.defaultValue"),
    CLASS("telegram.bot.message.class"),
    WITHOUT_NAME("telegram.bot.message.withoutName"),
    CLASS_NAME("telegram.bot.message.className"),
    ABOUT_MSG("telegram.bot.message.about"),
    ENGLISH("English"),
    RUSSIAN("Русский"),
    UKRAINIAN("Українська");


    private String key;

    MessageKey(String key) {
        this.key = key;
    }

    public String getString() {
        Localizer localizer = AbstractOTelegramBot.getLocalizer();
        return localizer.getString(key, null, null, AbstractOTelegramBot.getCurrentLocale(), null, "");
    }


    @Override
    public String toString() {
        return key;
    }
}
