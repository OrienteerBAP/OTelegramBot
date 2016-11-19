package org.orienteer.telegram.bot;

import org.apache.wicket.Localizer;
import org.apache.wicket.Session;
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

    private static final Logger LOG = LoggerFactory.getLogger(BotMessage.class);

    BotMessage() {
        OrienteerWebApplication application = OrienteerWebApplication.lookupApplication();
        Localizer localizer = application.getResourceSettings().getLocalizer();

        CLASS_MENU_MSG = localizer.getString("telegram.bot.menu.classesMenu", null);
        CLASS_BUT = localizer.getString("telegram.bot.button.class", null);
        START_SEARCH_MSG = localizer.getString("telegram.bot.message.startSearch", null);
        SEARCH_RESULT_SUCCESS_MSG = localizer.getString("telegram.bot.message.searchSuccess", null);
        SEARCH_RESULT_FAILED_MSG = localizer.getString("telegram.bot.message.searchFailed", null);
        ERROR_MSG = localizer.getString("telegram.bot.message.error", null);
        CLASS_SEARCH_MSG = localizer.getString("telegram.bot.message.classSearch", null);
        CLASS_DESCRIPTION_MSG = localizer.getString("telegram.bot.message.classDescription", null);
        CLASS_DOCUMENTS = localizer.getString("telegram.bot.message.classDocuments", null);
        SHORT_DOCUMENT_DESCRIPTION_MSG = localizer.getString("telegram.bot.message.shortDocumentDescription", null);
        DOCUMENT_DETAILS_MSG = localizer.getString("telegram.bot.message.detailDocumentDescription", null);
        SEARCH_FIELD_VALUES_RESULT = localizer.getString("telegram.bot.message.searchFieldValues", null);
        BACK = localizer.getString("telegram.bot.button.back", null);
        SEARCH_FAILED_CLASS_BY_NAME = localizer.getString("telegram.bot.message.failedSearchClassByName", null);
        FAILED_DOCUMENT_BY_RID = localizer.getString("telegram.bot.message.failedSearchDocumentByRID", null);
        SEARCH_DOCUMENT_NAMES_RESULT = localizer.getString("telegram.bot.message.searchDocumentNamesResult", null);
        SEARCH_CLASS_NAMES_RESULT = localizer.getString("telegram.bot.message.searchClassNamesResult", null);

        NAME = localizer.getString("telegram.bot.message.name", null);
        SUPER_CLASSES = localizer.getString("telegram.bot.message.superClasses", null);
        WITHOUT_SUPER_CLASSES = localizer.getString("telegram.bot.message.withoutSuperClasses", null);
        DEFAULT_VALUE = localizer.getString("telegram.bot.message.defaultValue", null);
        CLASS = localizer.getString("telegram.bot.message.class", null);
        WITHOUT_NAME = localizer.getString("telegram.bot.message.withoutName", null);
        CLASS_NAME = localizer.getString("telegram.bot.message.className", null);

        NEXT_RESULT_BUT = localizer.getString("telegram.bot.button.next", null);
        PREVIOUS_RESULT_BUT = localizer.getString("telegram.bot.button.previous", null);

        ABOUT_MSG = localizer.getString("telegram.bot.message.about", null);
    }
}
