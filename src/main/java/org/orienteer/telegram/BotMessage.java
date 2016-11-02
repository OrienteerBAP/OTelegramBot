package org.orienteer.telegram;

/**
 * @author  Vitaliy Gonchar
 */
interface BotMessage {
    String MAIN_MENU_MSG = "Change options or send me word and I will try to find it.";
    String CLASS_MENU_MSG = "Choose class in the list.";
    String CLASS_OPTION_MENU_MSG = "Choose getResultOfSearch option in class.\nOr type word for global getResultOfSearch in class";


    String ERROR_MSG = "<strong>I don't understand you :(</strong>";
    String SEARCH_MSG = "Send me name of class or property or document and I will try to find it in .";
    String SEARCH_FIELD_NAMES_MSG = "Send me word and I will try to find it in field names.";
    String SEARCH_FIELD_VALUES_MSG = "Send me word and I will try to find it in field values.";
    String SEARCH_DOCUMENT_NAMES_MSG = "Send me word and I will try to find it in document names";

    String SEARCH_CLASS_FIELD_NAMES_MSG = "Send me word and I will try to find it in class field names.";
    String SEARCH_CLASS_FIELD_VALUES_MSG = "Send me word and I will try to find it in class field values.";
    String SEARCH_CLASS_DOCUMENT_NAMES_MSG = "Send me word and I will try to find it in class document names.";

    String SEARCH_RESULT_SUCCESS_MSG = "<strong>To get information about document click on link.</strong>";
    String SEARCH_RESULT_FAILED_MSG = "<strong>I cannot found something!</strong>";

    String CLASS_DESCRIPTION_MSG = "Class description: ";
    String CLASS_NAME = "Class name: ";
    String CLASS_DOCUMENTS = "Class documents: ";
    String SEARCH_FAILED_CLASS_BY_NAME = "<strong>Cannot found class by this class name</strong>";
    String DOCUMENT_DESCRIPTION_MSG = "Document description: ";
    String FAILED_DOCUMENT_BY_RID = "Cannot found document by this id";

    String GLOBAL_SEARCH_BUT = "Global getResultOfSearch";

    String BACK_TO_CLASS_SEARCH_BUT = "Back to class searchAll";


    String CLASS_SEARCH_BUT = "Search in classes";
    String CLASS_FIELD_NAMES_BUT = "Search in class field names";
    String CLASS_FIELD_VALUES_BUT = "Search in class field values";
    String CLASS_DOC_NAMES_BUT = "Search in class document names";
    String CLASS_BUT = "Class: ";

    String FIELD_NAMES_BUT = "Search in field names";
    String FIELD_VALUES_BUT = "Search in field values";
    String DOC_NAMES_BUT = "Search in document names";

    String BACK_TO_MAIN_MENU_BUT = "Back to main menu";

    String HTML_STRONG_TEXT = "<strong>%s</strong>";

    String SEARCH_FIELD_NAMES_RESULT = "In field names: ";
    String SEARCH_FIELD_VALUES_RESULT = "In field values: ";
    String SEARCH_DOCUMENT_NAMES_RESULT = "In document names: ";
    String SEARCH_CLASS_NAMES_RESULT = "In class names: ";

    String NEXT_RESULT_BUT = "Next result of search";
    String PREVIOUS_RESULT_BUT = "Previous result of search";
    String NEXT_PREVIOUS_MSG = "Click on 'Next result of search' and I will send you next 10 results of search." +
            " Or click on 'Previous result of search' and I will send you previous 10 results of search";
    String START_OF_RESULT = "<strong>Start of result</strong>";
    String END_OF_RESULT = "<strong>End of result</strong>";
    int MAX_LENGTH = 4096;
}
