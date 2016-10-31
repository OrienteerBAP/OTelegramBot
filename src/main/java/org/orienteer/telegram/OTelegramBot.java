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
import java.util.concurrent.TimeUnit;

/**
 * @author Vitaliy Gonchar
 */
public class OTelegramBot extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(OTelegramBot.class);
    private final OTelegramModule.BotConfig BOT_CONFIG;
    private final LoadingCache<Integer, UserSession> SESSIONS;


    private OTelegramBot(OTelegramModule.BotConfig botConfig, LoadingCache<Integer, UserSession> sessions) {
        BOT_CONFIG = botConfig;
        SESSIONS = sessions;
    }

    public static OTelegramBot getOrienteerTelegramBot(OTelegramModule.BotConfig botConfig) {
        LoadingCache<Integer, UserSession> sessions = CacheBuilder.newBuilder()
                .expireAfterWrite(botConfig.USER_SESSION, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<Integer, UserSession>() {
                    @Override
                    public UserSession load(Integer key) {
                        return null;
                    }
                });
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
        SendMessage sendResponseMessage = null;
        UserSession userSession = SESSIONS.getIfPresent(message.getFrom().getId());
        if (userSession == null) {
            userSession = new UserSession();
            userSession.setBotState(getBotState(message.getText()));
        }
        LOG.debug("botState before handleIncomingMessage: " + userSession.getBotState());
        LOG.debug("UserSession: \n" + userSession);
        switch (getBotState(message.getText())) {
            case BACK_TO_MAIN_MENU:
            case START:
                sendResponseMessage = getMainMenuMessage(message);
                userSession.setBotState(BotState.SEARCH_GLOBAL);
                userSession.setSearch(true);
                break;
            case GLOBAL_FIELD_NAMES_SEARCH_BUT:
                sendResponseMessage = getTextMessage(message, BotMessage.SEARCH_FIELD_NAMES_MSG);
                userSession.setBotState(BotState.SEARCH_GLOBAL_FIELD_NAMES);
                userSession.setSearch(true);
                break;
            case GLOBAL_FIELD_VALUES_SEARCH_BUT:
                sendResponseMessage = getTextMessage(message, BotMessage.SEARCH_FIELD_VALUES_MSG);
                userSession.setBotState(BotState.SEARCH_GLOBAL_FIELD_VALUES);
                userSession.setSearch(true);
                break;
            case GLOBAL_DOC_NAMES_SEARCH_BUT:
                sendResponseMessage = getTextMessage(message, BotMessage.SEARCH_DOCUMENT_NAMES_MSG);
                userSession.setBotState(BotState.SEARCH_GLOBAL_DOC_NAMES);
                userSession.setSearch(true);
                break;
            case CLASS_MENU_SEARCH_BUT:
                sendResponseMessage = getClassesMenuMessage(message);
                userSession.setBotState(BotState.ClASS_MENU_OPTIONS);
                userSession.setTargetClass(null);
                userSession.setSearch(false);
                break;
            case GO_TO_DOCUMENT:
                sendResponseMessage = getTextMessage(message, goToTargetDocument(message.getText()));
                break;
            case GO_TO_CLASS:
                sendResponseMessage = getTextMessage(message, goToTargetClass(message.getText()));
                break;
            default:
                if (userSession.getSearch()) {
                    userSession = handleSearchRequest(message, userSession);
                } else userSession = handleClassMenu(message, userSession);
        }
        if (sendResponseMessage != null) sendMessage(sendResponseMessage);
        LOG.debug("botState after handleIncomingMessage:  " + userSession.getBotState());
        SESSIONS.put(message.getFrom().getId(), userSession);
        LOG.debug("Get from SESSIONS: " + SESSIONS.getUnchecked(message.getFrom().getId()));
    }

    private UserSession handleClassMenu(Message message, UserSession userSession) throws TelegramApiException {
        SendMessage sendResponseMessage = null;
        if (getBotState(message.getText()) != BotState.ERROR) {
            userSession.setBotState(getBotState(message.getText()));
        }
        switch (userSession.getBotState()) {
            case ClASS_MENU_OPTIONS:
                userSession.setTargetClass(message.getText());
                sendResponseMessage = getClassOptionMenuMessage(message);
                userSession.setBotState(BotState.SEARCH_IN_CLASS_GLOBAL);
                userSession.setSearch(true);
//                botState = BotState.ClASS_MENU_OPTIONS;
                break;
            case CLASS_FIELD_NAMES_SEARCH_BUT:
                sendResponseMessage = getTextMessage(message, BotMessage.SEARCH_CLASS_FIELD_NAMES_MSG);
                userSession.setBotState(BotState.SEARCH_CLASS_FIELD_NAMES);
                userSession.setSearch(true);
                break;
            case CLASS_FIELD_VALUES_SEARCH_BUT:
                sendResponseMessage = getTextMessage(message, BotMessage.SEARCH_CLASS_FIELD_VALUES_MSG);
                userSession.setBotState(BotState.SEARCH_CLASS_FIELD_VALUES);
                userSession.setSearch(true);
                break;
            case CLASS_DOC_NAMES_SEARCH_BUT:
                sendResponseMessage = getTextMessage(message, BotMessage.SEARCH_CLASS_DOCUMENT_NAMES_MSG);
                userSession.setBotState(BotState.SEARCH_CLASS_DOC_NAMES);
                userSession.setSearch(true);
                break;
        }
        if (sendResponseMessage != null) sendMessage(sendResponseMessage);
        return userSession;
    }

    private UserSession handleSearchRequest(Message message, UserSession userSession) throws TelegramApiException {
        SendMessage sendResponseMessage;
        switch (userSession.getBotState()) {
            case SEARCH_GLOBAL:
                sendResponseMessage = getTextMessage(message, getResultOfGlobalSearch(message.getText()));
                break;
            case SEARCH_GLOBAL_FIELD_NAMES:
                sendResponseMessage = getTextMessage(message, getResultOfFieldNamesSearch(message.getText()));
                break;
            case SEARCH_GLOBAL_FIELD_VALUES:
                sendResponseMessage = getTextMessage(message, getResultOfFieldValuesSearch(message.getText()));
                break;
            case SEARCH_GLOBAL_DOC_NAMES:
                sendResponseMessage = getTextMessage(message, getResultOfSearchDocumentGlobal(message.getText()));
                break;
            case SEARCH_IN_CLASS_GLOBAL:
                sendResponseMessage = getTextMessage(
                        message, getResultOfSearchInClassAllOptions(message.getText(), userSession.getTargetClass()));
                break;
            case SEARCH_CLASS_FIELD_NAMES:
                sendResponseMessage = getTextMessage(
                        message, getResultOfSearchFieldNamesInClass(message.getText(), userSession.getTargetClass()));
                break;
            case SEARCH_CLASS_FIELD_VALUES:
                sendResponseMessage = getTextMessage(
                        message, getResultOfSearchFieldValuesInClass(message.getText(), userSession.getTargetClass()));
                break;
            case SEARCH_CLASS_DOC_NAMES:
                sendResponseMessage = getTextMessage(
                        message, getResultOfSearchDocumentsInClass(message.getText(), userSession.getTargetClass()));
                break;
            default:
                sendResponseMessage = getTextMessage(message, BotMessage.ERROR_MSG);
        }
        sendMessage(sendResponseMessage);
        return userSession;
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
                String classes;
                Collection<OClass> oClasses = oDatabaseDocument.getMetadata().getSchema().getClasses();
                classes = searchInClassNames(word, oClasses);
                for (OClass oClass : oClasses) {
                    ORecordIteratorClass<ODocument> oDocuments = oDatabaseDocument.browseClass(oClass.getName());
                    for (ODocument oDocument : oDocuments) {
                        builderFieldNames.append(searchInFieldNames(word, oDocument));
                        builderFieldValues.append(searchInFieldValues(word, oDocument));
                    }
                }
                StringBuilder builder = new StringBuilder();
                if (builderFieldNames.length() > word.length() || builderFieldValues.length() > word.length()) {
                    builder.append(BotMessage.SEARCH_RESULT_SUCCESS_MSG);
                    if (classes.length() > word.length()) {
                        builder.append("\n" + BotMessage.SEARCH_CLASS_NAMES_RESULT + "\n");
                        builder.append(classes);
                    }
                    if (builderFieldNames.length() > word.length()) {
                        builder.append("\n" + BotMessage.SEARCH_FIELD_NAMES_RESULT + "\n");
                        builder.append(builderFieldNames.toString());
                    }
                    if (builderFieldValues.length() > word.length()) {
                        builder.append("\n" + BotMessage.SEARCH_FIELD_VALUES_RESULT + "\n");
                        builder.append(builderFieldValues.toString());
                    }
                } else builder.append(BotMessage.SEARCH_RESULT_FAILED_MSG);
                LOG.debug("\nResult of search: \n" + builder.toString());
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
                    result = BotMessage.SEARCH_RESULT_SUCCESS_MSG + result;
                } else result = BotMessage.SEARCH_RESULT_FAILED_MSG;
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
                    result = BotMessage.SEARCH_RESULT_SUCCESS_MSG + result;
                } else result = BotMessage.SEARCH_RESULT_FAILED_MSG;
                return result;
            }
        }.execute();

        return result;
    }

    /**
     * search similar class names with word
     * @param word search word
     * @param oClasses collection of classes where is search
     * @return result of search
     */
    private String searchInClassNames(final String word, Collection<OClass> oClasses) {
        StringBuilder builder = new StringBuilder();
        for (OClass oClass : oClasses) {
            if (isWordInLine(word, oClass.getName())) {
                builder.append("-  class name: ");
                builder.append(oClass.getName());
                builder.append(" ");
                builder.append(BotState.GO_TO_CLASS.command + oClass.getName());
                builder.append("\n");
            }
        }
        return builder.toString();
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
        String documentLink = BotState.GO_TO_DOCUMENT.command + oDocument.getIdentity().getClusterId()
                + "_" + oDocument.getIdentity().getClusterPosition();
        for (String name : fieldNames) {
            if (isWordInLine(word, name)) {
                builder.append("- ");
                builder.append(name);
                builder.append(" : ");
                builder.append(oDocument.field(name, OType.STRING));
                builder.append(" ");
                builder.append(documentLink);
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
                    result = BotMessage.SEARCH_RESULT_SUCCESS_MSG + "\n" + result;
                }else result = BotMessage.SEARCH_RESULT_FAILED_MSG;
                return result;
            }
        }.execute();
        return resultOfSearch;
    }

    /**
     * search similar words with word in class
     * @param word search word
     * @param className class where is search
     * @return result of search
     */
    private String getResultOfSearchInClassAllOptions(final String word, final String className) {
        String result = (String) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                StringBuilder builderFieldNames = new StringBuilder();
                StringBuilder builderFieldValues = new StringBuilder();
                ORecordIteratorClass<ODocument> oDocuments = oDatabaseDocument.browseClass(className);
                for (ODocument oDocument : oDocuments) {
                    builderFieldNames.append(searchInFieldNames(word, oDocument));
                    builderFieldValues.append(searchInFieldValues(word, oDocument));
                }

                StringBuilder resultBuilder = new StringBuilder();
                if (builderFieldNames.length() > word.length() || builderFieldValues.length() > word.length()) {
                resultBuilder.append(BotMessage.SEARCH_RESULT_SUCCESS_MSG);
                if (builderFieldNames.length() > word.length()) {
                    resultBuilder.append("\n" + BotMessage.SEARCH_FIELD_NAMES_RESULT + "\n");
                    resultBuilder.append(builderFieldNames.toString());
                }
                if (builderFieldValues.length() > word.length()) {
                    resultBuilder.append("\n" + BotMessage.SEARCH_FIELD_VALUES_RESULT + "\n");
                    resultBuilder.append(builderFieldValues.toString());
                }
            } else resultBuilder.append(BotMessage.SEARCH_RESULT_FAILED_MSG);
                LOG.debug("\nResult of search: \n" + resultBuilder.toString());
                return resultBuilder.toString();
            }
        }.execute();
        return result;
    }

    /**
     * search similar field names with fieldName
     * @param fieldName name of search field
     * @param className name of target class
     * @return result of search
     */
    private String getResultOfSearchFieldNamesInClass(final String fieldName, final String className) {
        String result = (String) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                ORecordIteratorClass<ODocument> oDocuments = oDatabaseDocument.browseClass(className);
                StringBuilder builder = new StringBuilder();
                for (ODocument oDocument : oDocuments) {
                    builder.append(searchInFieldNames(fieldName, oDocument));
                }
                StringBuilder resultBuilder = new StringBuilder();
                if (builder.length() > fieldName.length()) {
                    resultBuilder.append(BotMessage.SEARCH_RESULT_SUCCESS_MSG);
                    resultBuilder.append("\n" + BotMessage.SEARCH_FIELD_NAMES_RESULT + "\n");
                    resultBuilder.append(builder.toString());
                } else resultBuilder.append(BotMessage.SEARCH_RESULT_FAILED_MSG);
                return resultBuilder.toString();
            }
        }.execute();
        return result;
    }

    /**
     * search similar field values with valueName
     * @param valueName search word
     * @param className class where is search
     * @return result of search
     */
    private String getResultOfSearchFieldValuesInClass(final String valueName, final String className) {
        String result = (String) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                ORecordIteratorClass<ODocument> oDocuments = oDatabaseDocument.browseClass(className);
                StringBuilder builder = new StringBuilder();
                for (ODocument oDocument : oDocuments) {
                    builder.append(searchInFieldValues(valueName, oDocument));
                }
                StringBuilder resultBuilder = new StringBuilder();
                if (builder.length() > valueName.length()) {
                    resultBuilder.append(BotMessage.SEARCH_RESULT_SUCCESS_MSG);
                    resultBuilder.append("\n" + BotMessage.SEARCH_FIELD_VALUES_RESULT + "\n");
                    resultBuilder.append(builder.toString());
                } else resultBuilder.append(BotMessage.SEARCH_RESULT_FAILED_MSG);
                return resultBuilder.toString();
            }
        }.execute();
        return result;
    }

    /**
     * Search documents with similar names from target class
     * @param documentName name of document
     * @param className name of target class
     * @return result of search
     */
    private String getResultOfSearchDocumentsInClass(final String documentName, final String className) {
        String resultOfSearch = (String) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                if (!oDatabaseDocument.getMetadata().getSchema().existsClass(className)) {
                    return BotMessage.SEARCH_RESULT_FAILED_MSG;
                }
                ORecordIteratorClass<ODocument> oDocuments = oDatabaseDocument.browseClass(className);
                String result = getDocumentString(oDocuments, documentName);
                if (result.length() > 0) {
                    result = BotMessage.SEARCH_RESULT_SUCCESS_MSG
                            + "\n\n" + String.format(BotMessage.HTML_STRONG_TEXT, BotMessage.CLASS_NAME)
                            + className
                            + "\n\n" + BotMessage.HTML_STRONG_TEXT + BotMessage.CLASS_DOCUMENTS
                            + "\n" + result;
                } else result = BotMessage.SEARCH_RESULT_FAILED_MSG;
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
    private String goToTargetDocument(final String document) {
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
                    builder.append(oDocument.getClassName());
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

    private String goToTargetClass(final String targetClass) {
        final String className = targetClass.substring(BotState.GO_TO_CLASS.command.length());

        String result = (String) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                StringBuilder builder = new StringBuilder(
                        String.format(BotMessage.HTML_STRONG_TEXT, BotMessage.CLASS_DESCRIPTION_MSG) + "\n\n");
                OClass oClass = oDatabaseDocument.getMetadata().getSchema().getClass(className);
                builder.append("Name: ");
                builder.append(oClass.getName());
                builder.append("\n");
                Collection<OProperty> properties = oClass.properties();
                List<String> resultList = new ArrayList<>();
                for (OProperty property : properties) {
                    resultList.add(String.format(BotMessage.HTML_STRONG_TEXT, property.getName())
                        + ": " + property.getDefaultValue() + " (default value)");
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
        buttons.add(BotMessage.CLASS_FIELD_NAMES_BUT);
        buttons.add(BotMessage.CLASS_FIELD_VALUES_BUT);
        buttons.add(BotMessage.CLASS_DOC_NAMES_BUT);
//        buttons.add(BotMessage.BACK_TO_MAIN_MENU_BUT);
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
        buttonNames.add(BotMessage.FIELD_NAMES_BUT);
        buttonNames.add(BotMessage.FIELD_VALUES_BUT);
        buttonNames.add(BotMessage.DOC_NAMES_BUT);
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
}
