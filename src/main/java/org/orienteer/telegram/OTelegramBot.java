package org.orienteer.telegram;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.exception.ORecordNotFoundException;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.ORecord;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Vitaliy Gonchar
 */
public class OTelegramBot extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(OTelegramBot.class);
    private final OTelegramModule.BotConfig BOT_CONFIG;
    private final LoadingCache<String, BotState> SESSIONS;
    private BotState botState;


    private OTelegramBot(OTelegramModule.BotConfig botConfig, LoadingCache<String, BotState> sessions) {
        BOT_CONFIG = botConfig;
        SESSIONS = sessions;
    }

    public static OTelegramBot getOrienteerTelegramBot(OTelegramModule.BotConfig botConfig) {
        LoadingCache<String, BotState> sessions = CacheBuilder.newBuilder()
                .expireAfterWrite(botConfig.USER_SESSION, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<String, BotState>() {
                            @Override
                            public BotState load(String key) {
                                return null;
                            }
                        }
                );
        return new OTelegramBot(botConfig, sessions);
    }

    @Override
    public String getBotToken() {
        return BOT_CONFIG.TOKEN;
    }

    @Override
    public String getBotUsername() {
        return BOT_CONFIG.USERNAME;
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                try {
                    handleIncomingMessage(message);
                } catch (TelegramApiException e) {
                    LOG.error("Cannot send message");
                    if (LOG.isDebugEnabled()) e.printStackTrace();
                }
            }
        }
    }

    private void handleIncomingMessage(Message message) throws TelegramApiException {
        SendMessage sendRequestMessage;
        if (message.getText().equals(BotState.START) || botState == null) {
            sendRequestMessage = handleNewBotState(message);
        } else {
            sendRequestMessage = handleOldBotState(message);
        }
        sendMessage(sendRequestMessage);
    }

    private SendMessage handleNewBotState(Message message) {
        SendMessage sendRequestMessage;
        botState = getBotState(message.getText());
        switch (botState) {
            case BACK_TO_MAIN_MENU:
            case START:
                sendRequestMessage = getMainMenuMessage(message);
                break;
            case FIELD_NAMES_SEARCH_BUT:
                sendRequestMessage = getTextMessage(message, BotMessage.SEARCH_FIELD_NAMES_MSG);
                break;
            case VALUES_SEARCH_BUT:
                sendRequestMessage = getTextMessage(message, BotMessage.SEARCH_FIELD_VALUES_MSG);
                break;
            case DOC_SEARCH_BUT:
                sendRequestMessage = getTextMessage(message, BotMessage.SEARCH_DOCUMENT_NAMES_MSG);
                break;
            case CLASS_SEARCH_BUT:
                sendRequestMessage = getClassesMenuMessage(message);
                break;
            case GO_TO_DOCUMENT:
                sendRequestMessage = getTextMessage(message, getTargetDocument(message.getText()));
                botState = null;
                break;
            case GO_TO_CLASS:
                sendRequestMessage = getTextMessage(message, getTargetClass(message.getText()));
                botState = null;
                break;
            case SEARCH_CLASS_FIELD_NAMES:
                sendRequestMessage = getTextMessage(message, BotMessage.SEARCH_MSG);
                break;
            default:
                sendRequestMessage = getTextMessage(message, BotMessage.ERROR_MSG);
                botState = null;
                break;
        }
        return sendRequestMessage;
    }

    private SendMessage handleOldBotState(Message message) throws TelegramApiException {
        SendMessage sendRequestMessage;
        switch (botState) {
            case BACK_TO_MAIN_MENU:
            case START:
                if (message.getText().length() < 2) {
                    sendRequestMessage = getTextMessage(message, BotMessage.ERROR_MSG);
                } else if (getBotState(message.getText()) == BotState.ERROR) {
                    String result = getResultOfGlobalSearch(message.getText());
                    LOG.debug("result length: " + result.length());
                    if (result.length() < BotMessage.MAX_LENGTH) {
                        sendRequestMessage = getTextMessage(message, result);
                    } else {
                        String[] split = result.split(BotMessage.SEARCH_FIELD_VALUES_RESULT);
                        sendMessage(getTextMessage(message, split[0]));
                        sendRequestMessage = getTextMessage(message, split[1]);
                    }
//                    botState = null;
                } else {
                    sendRequestMessage = handleNewBotState(message);
                }
                break;
            case FIELD_NAMES_SEARCH_BUT:
                if (getBotState(message.getText()) != BotState.START) {
                    sendRequestMessage = getTextMessage(message, getResultOfFieldNamesSearch(message.getText()));
                } else sendRequestMessage = handleNewBotState(message);
                break;
            case VALUES_SEARCH_BUT:
                if (getBotState(message.getText()) != BotState.START) {
                    sendRequestMessage = getTextMessage(message, getResultOfFieldValuesSearch(message.getText()));
                } else sendRequestMessage = handleNewBotState(message);
                break;
            case DOC_SEARCH_BUT:
                if (getBotState(message.getText()) != BotState.START) {
                    sendRequestMessage = getTextMessage(message, getResultOfSearchDocumentGlobal(message.getText()));
                } else sendRequestMessage = handleNewBotState(message);
                break;
            case CLASS_SEARCH_BUT:
                if (getBotState(message.getText()) != BotState.START) {
                    sendRequestMessage = getTextMessage(message, BotMessage.SEARCH_MSG);
                } else sendRequestMessage = handleNewBotState(message);
                break;
            default:
                sendRequestMessage = getTextMessage(message, BotMessage.ERROR_MSG);
                botState = null;
        }
        return sendRequestMessage;
    }

    /**
     * search word in all database
     * @param word search word
     * @return string with result of search
     */
    private String getResultOfGlobalSearch(final String word) {
        String result = (String) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                StringBuilder builderFieldNames = new StringBuilder();
                StringBuilder builderFieldValues = new StringBuilder();
                Collection<OClass> oClasses = oDatabaseDocument.getMetadata().getSchema().getClasses();
                for (OClass oClass : oClasses) {
                    ORecordIteratorClass<ODocument> oDocuments = oDatabaseDocument.browseClass(oClass.getName());
                    for (ODocument oDocument : oDocuments) {
                        builderFieldNames.append(searchInFieldNames(word, oDocument));
                        builderFieldValues.append(searchInFieldValues(word, oDocument));
                    }
                }
                StringBuilder builder = new StringBuilder();
                if (builderFieldNames.length() > word.length() || builderFieldValues.length() > word.length()) {
                    builder.append(String.format(BotMessage.HTML_STRONG_TEXT, BotMessage.SEARCH_RESULT_SUCCESS_MSG));
                    if (builderFieldNames.length() > word.length()) {
                        builder.append("\n" + BotMessage.SEARCH_FIELD_NAMES_RESULT + "\n");
                        builder.append(builderFieldNames.toString());
                    }
                    if (builderFieldValues.length() > word.length()) {
                        builder.append("\n" + BotMessage.SEARCH_FIELD_VALUES_RESULT + "\n");
                        builder.append(builderFieldValues.toString());
                    }
                } else builder.append(String.format(BotMessage.HTML_STRONG_TEXT, BotMessage.SEARCH_RESULT_FAILED_MSG));
                LOG.info("\nResult of search: \n" + builder.toString());
                return builder.toString();
            }
        }.execute();
        return result;
    }

    /**
     * search similar words in field names in all database
     * @param word search word
     * @return string with result of search
     */
    private String getResultOfFieldNamesSearch(final String word) {
        String result = (String) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                StringBuilder builder = new StringBuilder();
                Collection<OClass> oClasses = oDatabaseDocument.getMetadata().getSchema().getClasses();
                for (OClass oClass : oClasses) {
                    ORecordIteratorClass<ODocument> oDocuments = oDatabaseDocument.browseClass(oClass.getName());
                    for (ODocument oDocument : oDocuments) {
                        builder.append(searchInFieldNames(word, oDocument));
                    }
                }
                String result = builder.toString();
                if (result.length() > word.length()) {
                    result = "\n" + BotMessage.SEARCH_FIELD_NAMES_RESULT + "\n" + result;
                    result = String.format(BotMessage.HTML_STRONG_TEXT, BotMessage.SEARCH_RESULT_SUCCESS_MSG) + result;
                } else result = String.format(BotMessage.HTML_STRONG_TEXT, BotMessage.SEARCH_RESULT_FAILED_MSG);
                return result;
            }
        }.execute();
        return result;
    }

    /**
     * Search similar words with word in field values in all database
     * @param word search word
     * @return string with result of search
     */
    private String getResultOfFieldValuesSearch(final String word) {
        String result = (String) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                StringBuilder builder = new StringBuilder();
                Collection<OClass> oClasses = oDatabaseDocument.getMetadata().getSchema().getClasses();
                for (OClass oClass : oClasses) {
                    ORecordIteratorClass<ODocument> oDocuments = oDatabaseDocument.browseClass(oClass.getName());
                    for (ODocument oDocument : oDocuments) {
                        builder.append(searchInFieldValues(word, oDocument));
                    }
                }
                String result = builder.toString();
                if (result.length() > word.length()) {
                    result = "\n" + BotMessage.SEARCH_FIELD_VALUES_RESULT + "\n" + result;
                    result = String.format(BotMessage.HTML_STRONG_TEXT, BotMessage.SEARCH_RESULT_SUCCESS_MSG) + result;
                } else result = String.format(BotMessage.HTML_STRONG_TEXT, BotMessage.SEARCH_RESULT_FAILED_MSG);
                return result;
            }
        }.execute();

        return result;
    }

    /**
     * search similar words in field names
     * @param word search wor
     * @param oDocument document where is search
     * @return string with result of search
     */
    private String searchInFieldNames(final String word, ODocument oDocument) {
        StringBuilder builder = new StringBuilder();
        String [] fieldNames = oDocument.fieldNames();
        String classId = BotState.GO_TO_DOCUMENT.command + oDocument.getIdentity().getClusterId()
                + "_" + oDocument.getIdentity().getClusterPosition();
        for (String name : fieldNames) {
            if (isWordInLine(word, name)) {
                builder.append("- ");
                builder.append(name);
                builder.append(" : ");
                builder.append(oDocument.field(name, OType.STRING));
                builder.append(" ");
                builder.append(classId);
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    /**
     * search similar words in field values
     * @param word search word
     * @param oDocument document where is search
     * @return string with result of search
     */
    private String searchInFieldValues(final String word, ODocument oDocument) {
        StringBuilder builder = new StringBuilder();
        String[] fieldNames = oDocument.fieldNames();
        String classId = BotState.GO_TO_DOCUMENT.command + oDocument.getIdentity().getClusterId()
                + "_" + oDocument.getIdentity().getClusterPosition();
        for (String name : fieldNames) {
            String fieldValue = oDocument.field(name, OType.STRING);
            if (fieldValue != null && isWordInLine(word, fieldValue)) {
                builder.append("- ");
                builder.append(name);
                builder.append(" : ");
                builder.append(fieldValue);
                builder.append(" ");
                builder.append(classId);
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    /**
     * Search documents with similar names from all database
     * @param documentName name of target document
     * @return result of search
     */
    private String getResultOfSearchDocumentGlobal(final String documentName) {
        String resultOfSearch = (String) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                Collection<OClass> classes = oDatabaseDocument.getMetadata().getSchema().getClasses();
                StringBuilder builder = new StringBuilder();
                for (OClass oClass : classes) {
                    ORecordIteratorClass<ODocument> oDocuments = oDatabaseDocument.browseClass(oClass.getName());
                    builder.append(getDocumentString(oDocuments, documentName));
                }
                String result = builder.toString();
                if (result.length() > 0) {
                    result = String.format(BotMessage.HTML_STRONG_TEXT, BotMessage.SEARCH_RESULT_SUCCESS_MSG) + "\n" + result;
                }else result = String.format(BotMessage.HTML_STRONG_TEXT, BotMessage.SEARCH_RESULT_FAILED_MSG);
                return result;
            }
        }.execute();
        return resultOfSearch;
    }

    /**
     * Search documents with similar names from target class
     * @param documentName name of document
     * @param className name of target class
     * @return result of search
     */
    private String searchDocumentsInClass(final String documentName, final String className) {
        String resultOfSearch = (String) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                if (!oDatabaseDocument.getMetadata().getSchema().existsClass(className)) {
                    return BotMessage.SEARCH_RESULT_FAILED_MSG;
                }
                ORecordIteratorClass<ODocument> oDocuments = oDatabaseDocument.browseClass(className);
                String result = getDocumentString(oDocuments, documentName);
                if (result.length() > 0) result = BotMessage.SEARCH_RESULT_SUCCESS_MSG + result;
                return result;
            }
        }.execute();

        return resultOfSearch;
    }

    /**
     * Build string with result of searchAll
     * @param oDocuments list of documents where will be searchAll
     * @param word name of target document
     * @return string with result of searchAll
     */
    private String getDocumentString(ORecordIteratorClass<ODocument> oDocuments, final String word) {
        StringBuilder builder = new StringBuilder();
        for (ODocument oDocument : oDocuments) {
            String docName = oDocument.field("name", OType.STRING);
            if (docName != null && isWordInLine(word, docName)) {
                ORID identity = oDocument.getIdentity();
                builder.append("- ");
                builder.append(docName);
                builder.append(" ");
                builder.append(BotState.GO_TO_DOCUMENT.command);
                builder.append(identity.getClusterId());
                builder.append("_");
                builder.append(identity.getClusterPosition());
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    /**
     * search word in line
     * @param word search word
     * @param line string where word can be
     * @return true if word is in line
     */
    private boolean isWordInLine(final String word, String line) {
        boolean isIn = false;
        if (line.toLowerCase().contains(word.toLowerCase())) isIn = true;
        return isIn;
    }

    /**
     * get description document by class name and RID
     * @param document string like "/<className><RID>"
     * @return string with description document
     */
    private String getTargetDocument(final String document) {
        String [] split = document.substring(BotState.GO_TO_DOCUMENT.command.length()).split("_");
        final int clusterID = Integer.valueOf(split[0]);
        final long recordID = Long.valueOf(split[1]);
        final ORecordId oRecordId = new ORecordId(clusterID, recordID);
        String result = (String) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                StringBuilder builder = new StringBuilder(String.format(
                        BotMessage.HTML_STRONG_TEXT, BotMessage.DOCUMENT_DESCRIPTION_MSG) + "\n\n"
                        + String.format(BotMessage.HTML_STRONG_TEXT, "Class:  "));
                ODocument oDocument;
                try {
                    oDocument = oDatabaseDocument.getRecord(oRecordId);
                    builder.append(oDocument.getClassName());
                    builder.append(" " + BotState.GO_TO_CLASS.command);
                    builder.append(document.substring(BotState.GO_TO_DOCUMENT.command.length()));
                    builder.append("\n\n");
                    String[] fieldNames = oDocument.fieldNames();
                    List<String> resultList = new ArrayList<>();
                    for (String fieldName : fieldNames) {
                        resultList.add(String.format(BotMessage.HTML_STRONG_TEXT, fieldName) + ":  "
                                + oDocument.field(fieldName, OType.STRING) + "\n");
                    }
                    Collections.sort(resultList);
                    for (String str : resultList) {
                        builder.append(str);
                    }
                } catch (ORecordNotFoundException ex) {
                    LOG.warn("Record: " + oRecordId + " was not found.");
                    if (LOG.isDebugEnabled()) ex.printStackTrace();
                    builder = new StringBuilder(
                            String.format(BotMessage.HTML_STRONG_TEXT, BotMessage.FAILED_DOCUMENT_BY_RID));
                }
                return builder.toString();
            }
        }.execute();
        return result;
    }

    private String getTargetClass(final String targetClass) {
        String [] split = targetClass.substring(BotState.GO_TO_CLASS.command.length()).split("_");
        final int clusterID = Integer.valueOf(split[0]);
        final long recordID = Long.valueOf(split[1]);
        final ORecordId oRecordId = new ORecordId(clusterID, recordID);
        String result = (String) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                StringBuilder builder = new StringBuilder(
                        String.format(BotMessage.HTML_STRONG_TEXT, BotMessage.CLASS_DESCRIPTION_MSG) + "\n\n");
                OClass oClass = oDatabaseDocument.getRecord(oRecordId);
                builder.append("Name: ");
                builder.append(oClass.getName());
                Collection<OProperty> properties = oClass.properties();
                List<String> resultList = new ArrayList<>();
                for (OProperty property : properties) {
                    resultList.add(String.format(BotMessage.HTML_STRONG_TEXT, property.getName())
                        + ": default - " + property.getDefaultValue());
                }
                Collections.sort(resultList);
                for (String string : resultList) {
                    builder.append(string);
                    builder.append("\n");
                }
                builder.append("\n");
                builder.append(String.format(BotMessage.HTML_STRONG_TEXT, BotMessage.CLASS_DOCUMENTS));
                builder.append("\n");
                ORecordIteratorClass<ODocument> oDocuments = oDatabaseDocument.browseClass(oClass.getName());
                resultList = new ArrayList<>();
                for (ODocument oDocument : oDocuments) {
                    String docId = BotState.GO_TO_DOCUMENT.command + oDocument.getIdentity().getClusterId()
                            + "_" + oDocument.getIdentity().getClusterPosition();
                    resultList.add(oDocument.field("name") + " " + docId);
                }
                Collections.sort(resultList);
                for (String string : resultList) {
                    builder.append(string);
                    builder.append("\n");
                }
                return builder.toString();
            }
        }.execute();
        return result;
    }

    private SendMessage getClassesMenuMessage(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.enableMarkdown(true);
        sendMessage.setText(BotMessage.CLASS_MENU_MSG);
        List<String> buttonNames = (List<String>) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                List<String> result = new ArrayList<>();
                Collection<OClass> classes = oDatabaseDocument.getMetadata().getSchema().getClasses();
                for (OClass oClass: classes) {
                    result.add(oClass.getName());
                }
                Collections.sort(result);
                return result;
            }
        }.execute();
        sendMessage.setReplyMarkup(getMenuMarkup(buttonNames));
        return sendMessage;
    }

    private SendMessage getClassOptionMenuMessage(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.enableMarkdown(true);
        sendMessage.setText(BotMessage.CLASS_OPTION_MENU_MSG);
        List<String> buttons = new ArrayList<>();
        buttons.add(BotMessage.FIELDS_BUT);
        buttons.add(BotMessage.VALUES_BUT);
        buttons.add(BotMessage.DOCUMENTS_BUT);
        buttons.add(BotMessage.BACK_TO_MAIN_MENU_BUT);
        sendMessage.setReplyMarkup(getMenuMarkup(buttons));
        return sendMessage;
    }

    private SendMessage getDocumentMenuMessage(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(BotMessage.SEARCH_MSG);
        List<String> buttons = new ArrayList<>();
        buttons.add(BotMessage.BACK_TO_CLASS_SEARCH_BUT);
        sendMessage.setReplyMarkup(getMenuMarkup(buttons));
        return sendMessage;
    }

    private SendMessage getTextMessage(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.enableHtml(true);
        sendMessage.setText(text);
        return sendMessage;
    }

    private SendMessage getMainMenuMessage(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.enableMarkdown(true);
        sendMessage.setText(BotMessage.MAIN_MENU_MSG);
        List<String> buttonNames = new ArrayList<>();
        buttonNames.add(BotMessage.FIELDS_BUT);
        buttonNames.add(BotMessage.VALUES_BUT);
        buttonNames.add(BotMessage.DOCUMENTS_BUT);
        buttonNames.add(BotMessage.CLASS_SEARCH_BUT);
        sendMessage.setReplyMarkup(getMenuMarkup(buttonNames));
        return sendMessage;
    }

    private ReplyKeyboardMarkup getMenuMarkup(List<String> buttonNames) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setOneTimeKeyboad(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        for (String buttonName: buttonNames) {
            KeyboardRow keyboardRow = new KeyboardRow();
            keyboardRow.add(buttonName);
            keyboard.add(keyboardRow);
        }
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    private BotState getBotState(String text) {
        if (text.startsWith(BotState.GO_TO_DOCUMENT.command)) {
            return BotState.GO_TO_DOCUMENT;
        } else if (text.startsWith(BotState.GO_TO_CLASS.command)) {
            return BotState.GO_TO_CLASS;
        }
        BotState state = BotState.ERROR;
        for (BotState search : BotState.values()) {
            if (search.command.equals(text)) {
                state = search;
                break;
            }
        }
        return state;
    }

    private enum BotState {
        START("/start"),
        FIELD_NAMES_SEARCH_BUT(BotMessage.FIELDS_BUT),
        VALUES_SEARCH_BUT(BotMessage.VALUES_BUT),
        DOC_SEARCH_BUT(BotMessage.DOCUMENTS_BUT),
        CLASS_SEARCH_BUT(BotMessage.CLASS_SEARCH_BUT),
        BACK_TO_MAIN_MENU(BotMessage.BACK_TO_MAIN_MENU_BUT),
        BACK_TO_CLASS_SEARCH(BotMessage.BACK_TO_CLASS_SEARCH_BUT),
        GO_TO_DOCUMENT("/document"),
        GO_TO_CLASS("/class"),

        SEARCH_GLOBAL("/globalSearch"),

        SEARCH_GLOBAL_FIELD_NAMES("/searchGlobalFieldNames"),
        SEARCH_GLOBAL_FIELD_VALUES("/searchGlobalFieldValues"),
        SEARCH_GLOBAL_DOC_NAMES("/searchGlobalDocNames"),

        SEARCH_GLOBAL_CLASS_NAMES("/searchGlobalClassNames"),

        SEARCH_CLASS_FIELD_NAMES("/searchClassFieldNames"),
        SEARCH_CLASS_FIELD_VALUES("/searchClassFieldValues"),
        SEARCH_CLASS_DOC_NAMES("/searchClassDocNames"),
        ERROR("/error");

        private String command;
        BotState(String command) {
            this.command = command;
        }

    }

    private interface BotMessage {
        String MAIN_MENU_MSG = "Change options or send me word and I will try to find it.";
        String CLASS_MENU_MSG = "Choose class in the list.";
        String CLASS_OPTION_MENU_MSG = "Choose search option in class.";


        String ERROR_MSG = "I don't understand you :(";
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
        String FIELDS_BUT = "Search in field names";
        String VALUES_BUT = "Search in field values";
        String DOCUMENTS_BUT = "Search in document names";

        String BACK_TO_MAIN_MENU_BUT = "Back to main menu";

        String HTML_STRONG_TEXT = "<strong>%s</strong>";

        String SEARCH_FIELD_NAMES_RESULT = "In field names: ";
        String SEARCH_FIELD_VALUES_RESULT = "In field values: ";

        int MAX_LENGTH = 2048;
    }
}
