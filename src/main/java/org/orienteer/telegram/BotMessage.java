package org.orienteer.telegram;

/**
 * Created by Vitaliy Gonchar on 31.10.16.
 */
interface BotMessage {
    String MAIN_MENU_MSG = "Change options or send me word and I will try to find it.";
    String CLASS_MENU_MSG = "Choose class in the list.";
    String CLASS_OPTION_MENU_MSG = "Choose search option in class.";


    String ERROR_MSG = "<strong>I don't understand you :(</strong>";
    String SEARCH_MSG = "Send me name of class or property or document and I will try to find it in .";
    String SEARCH_FIELD_NAMES_MSG = "Send me word and I will try to find it in field names.";
    String SEARCH_FIELD_VALUES_MSG = "Send me word and I will try to find it in field values.";
    String SEARCH_DOCUMENT_NAMES_MSG = "Send me word and I will try to find it in document names";

    String SEARCH_RESULT_SUCCESS_MSG = "To get information about document click on link.";
    String SEARCH_RESULT_FAILED_MSG = "I cannot found something!";

    String CLASS_DESCRIPTION_MSG = "Class description: ";
    String CLASS_DOCUMENTS = "Class documents: ";
    String FAILED_CLASS_BY_DIR = "Cannot found class by this id";
    String DOCUMENT_DESCRIPTION_MSG = "Document description: ";
    String FAILED_DOCUMENT_BY_RID = "Cannot found document by this id";

    String GLOBAL_SEARCH_BUT = "Global search";

    String BACK_TO_CLASS_SEARCH_BUT = "Back to class searchAll";


    String CLASS_SEARCH_BUT = "Search in classes";
    String CLASS_FIELD_NAMES_BUT = "Search in class field names";
    String CLASS_FIELD_VALUES_BUT = "Search in class field values";
    String CLASS_DOC_NAMES_BUT = "Search in class document names";

    String FIELD_NAMES_BUT = "Search in field names";
    String FIELD_VALUES_BUT = "Search in field values";
    String DOC_NAMES_BUT = "Search in document names";

    String BACK_TO_MAIN_MENU_BUT = "Back to main menu";

    String HTML_STRONG_TEXT = "<strong>%s</strong>";

    String SEARCH_FIELD_NAMES_RESULT = "In field names: ";
    String SEARCH_FIELD_VALUES_RESULT = "In field values: ";
    String SEARCH_CLASS_NAMES_RESULT = "In class names: ";
    int MAX_LENGTH = 2048;
}
