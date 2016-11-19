package org.orienteer.telegram.bot;

import org.apache.wicket.Localizer;
import org.apache.wicket.Session;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.OrienteerWebSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * @author  Vitaliy Gonchar
 */
public class BotMessage {
    public final String CLASS_MENU_MSG;
    public final String LANGUAGE_MENU_MSG;
    public final String LANGUAGE_BUT;
    public final String LANGUAGE_CHANGED;
    public final String CLASS_BUT;
    public final String NEXT_RESULT_BUT;
    public final String PREVIOUS_RESULT_BUT;

    public final String START_SEARCH_MSG;
    public final String SEARCH_RESULT_SUCCESS_MSG;
    public final String SEARCH_RESULT_FAILED_MSG;
    public final String ERROR_MSG;
    public final String CLASS_SEARCH_MSG;
    public final String CLASS_DESCRIPTION_MSG;
    public final String CLASS_DOCUMENTS;
    public final String SHORT_DOCUMENT_DESCRIPTION_MSG;
    public final String DOCUMENT_DETAILS_MSG;
    public final String SEARCH_FIELD_VALUES_RESULT;
    public final String BACK;
    public final String SEARCH_FAILED_CLASS_BY_NAME;
    public final String FAILED_DOCUMENT_BY_RID;
    public final String SEARCH_DOCUMENT_NAMES_RESULT;
    public final String SEARCH_CLASS_NAMES_RESULT;

    public final String NAME;
    public final String SUPER_CLASSES;
    public final String WITHOUT_SUPER_CLASSES;
    public final String DEFAULT_VALUE;
    public final String CLASS;
    public final String WITHOUT_NAME;
    public final String CLASS_NAME;

    public final String ABOUT_MSG;

    public final String EMBEDDED = "_embedded";
    public final String HTML_STRONG_TEXT = "<strong>%s</strong>";
    public final String ENGLISH = "English";
    public final String RUSSIAN = "Русский";
    public final String UKRAINIAN = "Українська";

    private static final Logger LOG = LoggerFactory.getLogger(BotMessage.class);

    public BotMessage(String language) {
        OrienteerWebApplication application = OrienteerWebApplication.lookupApplication();
        Localizer localizer = application.getResourceSettings().getLocalizer();
        Locale locale = new Locale(language);
        CLASS_MENU_MSG = localizer.getString("telegram.bot.menu.classesMenu", null, null, locale, null, "<class menu message>");
        LANGUAGE_MENU_MSG = localizer.getString("telegram.bot.menu.language", null, null, locale, null, "<language>");
        LANGUAGE_BUT = localizer.getString("telegram.bot.button.language", null, null, locale, null, "<language button>");
        LANGUAGE_CHANGED = localizer.getString("telegram.bot.message.languageChanged", null, null, locale, null, "<language changed>");
        CLASS_BUT = localizer.getString("telegram.bot.button.class", null, null, locale, null, "<class button>");
        START_SEARCH_MSG = localizer.getString("telegram.bot.message.startSearch", null, null, locale, null, "<start search message>");
        SEARCH_RESULT_SUCCESS_MSG = localizer.getString("telegram.bot.message.searchSuccess", null, null, locale, null, "<search success message>");
        SEARCH_RESULT_FAILED_MSG = localizer.getString("telegram.bot.message.searchFailed", null, null, locale, null, "<search failed message>");
        ERROR_MSG = localizer.getString("telegram.bot.message.error", null, null, locale, null, "<error message>");
        CLASS_SEARCH_MSG = localizer.getString("telegram.bot.message.classSearch", null, null, locale, null, "<class search message>");
        CLASS_DESCRIPTION_MSG = localizer.getString("telegram.bot.message.classDescription", null, null, locale, null, "<class description>");
        CLASS_DOCUMENTS = localizer.getString("telegram.bot.message.classDocuments", null, null, locale, null, "<class documents>");
        SHORT_DOCUMENT_DESCRIPTION_MSG = localizer.getString("telegram.bot.message.shortDocumentDescription", null, null, locale, null, "<short document description>");
        DOCUMENT_DETAILS_MSG = localizer.getString("telegram.bot.message.detailDocumentDescription", null, null, locale, null, "<detail document description>");
        SEARCH_FIELD_VALUES_RESULT = localizer.getString("telegram.bot.message.searchFieldValues", null, null, locale, null, "<search field values message>");
        BACK = localizer.getString("telegram.bot.button.back", null, null, locale, null, "<back button>");
        SEARCH_FAILED_CLASS_BY_NAME = localizer.getString("telegram.bot.message.failedSearchClassByName", null, null, locale, null, "<failed search class by name>");
        FAILED_DOCUMENT_BY_RID = localizer.getString("telegram.bot.message.failedSearchDocumentByRID", null, null, locale, null, "<failed search document by rid>");
        SEARCH_DOCUMENT_NAMES_RESULT = localizer.getString("telegram.bot.message.searchDocumentNamesResult", null, null, locale, null, "<search document names result>");
        SEARCH_CLASS_NAMES_RESULT = localizer.getString("telegram.bot.message.searchClassNamesResult", null, null, locale, null, "<search class names result>");

        NAME = localizer.getString("telegram.bot.message.name", null, null, locale, null, "<name>");
        SUPER_CLASSES = localizer.getString("telegram.bot.message.superClasses", null, null, locale, null, "<super classes>");
        WITHOUT_SUPER_CLASSES = localizer.getString("telegram.bot.message.withoutSuperClasses", null, null, locale, null, "<without super classes>");
        DEFAULT_VALUE = localizer.getString("telegram.bot.message.defaultValue", null, null, locale, null, "<default value>");
        CLASS = localizer.getString("telegram.bot.message.class", null, null, locale, null, "<class>");
        WITHOUT_NAME = localizer.getString("telegram.bot.message.withoutName", null, null, locale, null, "<without name>");
        CLASS_NAME = localizer.getString("telegram.bot.message.className", null, null, locale, null, "<class name>");

        NEXT_RESULT_BUT = localizer.getString("telegram.bot.button.next", null, null, locale, null, "<next button>");
        PREVIOUS_RESULT_BUT = localizer.getString("telegram.bot.button.previous", null, null, locale, null, "<previous button>");

        ABOUT_MSG = localizer.getString("telegram.bot.message.about", null, null, locale, null, "<about message>");
    }

    @Override
    public String toString() {
        return "BotMessage{" +
                "CLASS_MENU_MSG='" + CLASS_MENU_MSG + '\'' +
                ", LANGUAGE_MENU_MSG='" + LANGUAGE_MENU_MSG + '\'' +
                ", LANGUAGE_BUT='" + LANGUAGE_BUT + '\'' +
                ", LANGUAGE_CHANGED='" + LANGUAGE_CHANGED + '\'' +
                ", CLASS_BUT='" + CLASS_BUT + '\'' +
                ", NEXT_RESULT_BUT='" + NEXT_RESULT_BUT + '\'' +
                ", PREVIOUS_RESULT_BUT='" + PREVIOUS_RESULT_BUT + '\'' +
                ", START_SEARCH_MSG='" + START_SEARCH_MSG + '\'' +
                ", SEARCH_RESULT_SUCCESS_MSG='" + SEARCH_RESULT_SUCCESS_MSG + '\'' +
                ", SEARCH_RESULT_FAILED_MSG='" + SEARCH_RESULT_FAILED_MSG + '\'' +
                ", ERROR_MSG='" + ERROR_MSG + '\'' +
                ", CLASS_SEARCH_MSG='" + CLASS_SEARCH_MSG + '\'' +
                ", CLASS_DESCRIPTION_MSG='" + CLASS_DESCRIPTION_MSG + '\'' +
                ", CLASS_DOCUMENTS='" + CLASS_DOCUMENTS + '\'' +
                ", SHORT_DOCUMENT_DESCRIPTION_MSG='" + SHORT_DOCUMENT_DESCRIPTION_MSG + '\'' +
                ", DOCUMENT_DETAILS_MSG='" + DOCUMENT_DETAILS_MSG + '\'' +
                ", SEARCH_FIELD_VALUES_RESULT='" + SEARCH_FIELD_VALUES_RESULT + '\'' +
                ", BACK='" + BACK + '\'' +
                ", SEARCH_FAILED_CLASS_BY_NAME='" + SEARCH_FAILED_CLASS_BY_NAME + '\'' +
                ", FAILED_DOCUMENT_BY_RID='" + FAILED_DOCUMENT_BY_RID + '\'' +
                ", SEARCH_DOCUMENT_NAMES_RESULT='" + SEARCH_DOCUMENT_NAMES_RESULT + '\'' +
                ", SEARCH_CLASS_NAMES_RESULT='" + SEARCH_CLASS_NAMES_RESULT + '\'' +
                ", NAME='" + NAME + '\'' +
                ", SUPER_CLASSES='" + SUPER_CLASSES + '\'' +
                ", WITHOUT_SUPER_CLASSES='" + WITHOUT_SUPER_CLASSES + '\'' +
                ", DEFAULT_VALUE='" + DEFAULT_VALUE + '\'' +
                ", CLASS='" + CLASS + '\'' +
                ", WITHOUT_NAME='" + WITHOUT_NAME + '\'' +
                ", CLASS_NAME='" + CLASS_NAME + '\'' +
                ", ABOUT_MSG='" + ABOUT_MSG + '\'' +
                ", EMBEDDED='" + EMBEDDED + '\'' +
                ", HTML_STRONG_TEXT='" + HTML_STRONG_TEXT + '\'' +
                ", ENGLISH='" + ENGLISH + '\'' +
                ", RUSSIAN='" + RUSSIAN + '\'' +
                ", UKRAINIAN='" + UKRAINIAN + '\'' +
                '}';
    }
}
