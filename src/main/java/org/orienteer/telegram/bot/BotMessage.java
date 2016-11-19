package org.orienteer.telegram.bot;

import org.apache.wicket.model.ResourceModel;

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

    public final String ABOUT_MSG;

    public final String EMBEDDED = "_embedded";
    public final String HTML_STRONG_TEXT = "<strong>%s</strong>";

    BotMessage() {
        CLASS_MENU_MSG = new ResourceModel("telegram.bot.menu.classesMenu").getObject();
        CLASS_BUT = new ResourceModel("telegram.bot.button.class").getObject();
        START_SEARCH_MSG = new ResourceModel("telegram.bot.message.startSearch").getObject();
        SEARCH_RESULT_SUCCESS_MSG = new ResourceModel("telegram.bot.message.searchSuccess").getObject();
        SEARCH_RESULT_FAILED_MSG = new ResourceModel("telegram.bot.message.searchFailed").getObject();
        ERROR_MSG = new ResourceModel("telegram.bot.message.error").getObject();
        CLASS_SEARCH_MSG = new ResourceModel("telegram.bot.message.classSearch").getObject();
        CLASS_DESCRIPTION_MSG = new ResourceModel("telegram.bot.message.classDescription").getObject();
        CLASS_DOCUMENTS = new ResourceModel("telegram.bot.message.classDocuments").getObject();
        SHORT_DOCUMENT_DESCRIPTION_MSG = new ResourceModel("telegram.bot.message.shortDocumentDescription").getObject();
        DOCUMENT_DETAILS_MSG = new ResourceModel("telegram.bot.message.detailDocumentDescription").getObject();
        SEARCH_FIELD_VALUES_RESULT = new ResourceModel("telegram.bot.message.searchFieldValues").getObject();
        BACK = new ResourceModel("telegram.bot.button.back").getObject();
        SEARCH_FAILED_CLASS_BY_NAME = new ResourceModel("telegram.bot.message.failedSearchClassByName").getObject();
        FAILED_DOCUMENT_BY_RID = new ResourceModel("telegram.bot.message.failedSearchDocumentByRID").getObject();
        SEARCH_DOCUMENT_NAMES_RESULT = new ResourceModel("telegram.bot.message.searchDocumentNamesResult").getObject();
        SEARCH_CLASS_NAMES_RESULT = new ResourceModel("telegram.bot.message.searchClassNamesResult").getObject();

        NEXT_RESULT_BUT = new ResourceModel("telegram.bot.button.next").getObject();
        PREVIOUS_RESULT_BUT = new ResourceModel("telegram.bot.button.previous").getObject();

        ABOUT_MSG = new ResourceModel("telegram.bot.message.about").getObject();
    }
}
