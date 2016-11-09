package org.orienteer.telegram.bot;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.exception.ORecordNotFoundException;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.glassfish.jersey.internal.inject.Custom;
import org.orienteer.core.CustomAttribute;
import org.orienteer.telegram.CustomConstants;
import org.orienteer.telegram.module.OTelegramModule;
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

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Vitaliy Gonchar
 */
public class OTelegramBot extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(OTelegramBot.class);
    private final OTelegramModule.BotConfig BOT_CONFIG;
    private final LoadingCache<Integer, UserSession> SESSIONS;

    private static Map<String, OClass> CLASS_CACHE;
    private static Map<String, String> QUERY_CACHE;

    public static void createClassCache() {
        CLASS_CACHE = (Map<String, OClass>) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument db) {
                Map<String, OClass> classCache = new HashMap<>();
                for (OClass oClass : db.getMetadata().getSchema().getClasses()) {
                    String custom = oClass.getCustom(CustomConstants.CUSTOM_TELEGRAM_SEARCH);
                    LOG.debug("custom: " + custom);
                    if (custom != null && new Boolean(custom)) {
                        classCache.put(oClass.getName(), oClass);
                    }
                }
                return classCache;
            }
        }.execute();
        createQueryCache();
        LOG.debug("Class cache size: " + CLASS_CACHE.size());
    }

    public static void createQueryCache() {
        QUERY_CACHE = new HashMap<>();
        if (CLASS_CACHE == null) createClassCache();
        for (OClass oClass : CLASS_CACHE.values()) {
            QUERY_CACHE.put(oClass.getName(), oClass.getCustom(CustomConstants.CUSTOM_TELEGRAM_SEARCH_QUERY));
        }
    }

    public static Map<String, OClass> getClassCache() {
        return CLASS_CACHE;
    }

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
        OTelegramBot.createClassCache();
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
                    handleMenuRequest(message);
                } catch (TelegramApiException e) {
                    LOG.error("Cannot send message");
                    if (LOG.isDebugEnabled()) e.printStackTrace();
                }
            }
        }
    }

    private void handleMenuRequest(Message message) throws TelegramApiException {
        SendMessage sendResponseMessage = null;
        UserSession userSession = SESSIONS.getIfPresent(message.getFrom().getId());
        if (userSession == null) {
            userSession = new UserSession();
            userSession.setBotState(getBotState(message.getText()));
        }

        switch (getBotState(message.getText())) {
            case START:
                sendResponseMessage = getMainMenuMessage(message);
                userSession.setBotState(BotState.SEARCH_GLOBAL);
                break;
            case GLOBAL_FIELD_NAMES_SEARCH_BUT:
                sendResponseMessage = getTextMessage(message, BotMessage.SEARCH_FIELD_NAMES_MSG);
                userSession.setBotState(BotState.SEARCH_GLOBAL_FIELD_NAMES);
                break;
            case GLOBAL_FIELD_VALUES_SEARCH_BUT:
                sendResponseMessage = getTextMessage(message, BotMessage.SEARCH_FIELD_VALUES_MSG);
                userSession.setBotState(BotState.SEARCH_GLOBAL_FIELD_VALUES);
                break;
            case GLOBAL_DOC_NAMES_SEARCH_BUT:
                sendResponseMessage = getTextMessage(message, BotMessage.SEARCH_DOCUMENT_NAMES_MSG);
                userSession.setBotState(BotState.SEARCH_GLOBAL_DOC_NAMES);
                break;
            case CLASS_MENU_SEARCH_BUT:
                sendResponseMessage = getClassesMenuMessage(message);
                userSession.setBotState(BotState.ClASS_MENU_OPTIONS);
                userSession.setTargetClass(null);
                break;
            case ClASS_MENU_OPTIONS:
                if (userSession.getTargetClass() == null) {
                    userSession.setTargetClass(message.getText().substring(BotMessage.CLASS_BUT.length()));
                }
                sendResponseMessage = getClassOptionMenuMessage(message);
                userSession.setBotState(BotState.SEARCH_IN_CLASS_GLOBAL);
                break;
            case CLASS_FIELD_NAMES_SEARCH_BUT:
                sendResponseMessage = getTextMessage(message, BotMessage.SEARCH_CLASS_FIELD_NAMES_MSG);
                userSession.setBotState(BotState.SEARCH_CLASS_FIELD_NAMES);
                break;
            case CLASS_FIELD_VALUES_SEARCH_BUT:
                sendResponseMessage = getTextMessage(message, BotMessage.SEARCH_CLASS_FIELD_VALUES_MSG);
                userSession.setBotState(BotState.SEARCH_CLASS_FIELD_VALUES);
                break;
            case CLASS_DOC_NAMES_SEARCH_BUT:
                sendResponseMessage = getTextMessage(message, BotMessage.SEARCH_CLASS_DOCUMENT_NAMES_MSG);
                userSession.setBotState(BotState.SEARCH_CLASS_DOC_NAMES);
                break;
            case NEXT_RESULT:
                sendResponseMessage = getTextMessage(message, userSession.getNextResult());
                sendMessage(sendResponseMessage);
                sendResponseMessage = getNextPreviousMenuMessage(message);
                break;
            case PREVIOUS_RESULT:
                sendResponseMessage = getTextMessage(message, userSession.getPreviousResult());
                sendMessage(sendResponseMessage);
                sendResponseMessage = getNextPreviousMenuMessage(message);
                break;
            case GO_TO_DOCUMENT:
                sendResponseMessage = getTextMessage(message, goToTargetDocument(message.getText()));
                break;
            case GO_TO_CLASS:
                sendResponseMessage = getTextMessage(message, goToTargetClass(message.getText()));
                break;
            default:
                userSession = handleSearchRequest(message, userSession);
        }
        SESSIONS.put(message.getFrom().getId(), userSession);
        if (sendResponseMessage != null) sendMessage(sendResponseMessage);
    }

    private UserSession handleSearchRequest(Message message, UserSession userSession) throws TelegramApiException {
        SendMessage sendResponseMessage = null;
        List<String> result = null;
        Search search = new Search(message.getText());
        switch (userSession.getBotState()) {
            case SEARCH_GLOBAL:
                search.setGlobalSearch(true);
                result = search.getResultOfSearch();
                break;
            case SEARCH_GLOBAL_FIELD_NAMES:
                search.setGlobalFieldNamesSearch(true);
                result = search.getResultOfSearch();
                break;
            case SEARCH_GLOBAL_FIELD_VALUES:
                search.setGlobalFieldValuesSearch(true);
                result = search.getResultOfSearch();
                break;
            case SEARCH_GLOBAL_DOC_NAMES:
                search.setGlobalDocumentNamesSearch(true);
                result = search.getResultOfSearch();
                break;
            case SEARCH_IN_CLASS_GLOBAL:
                search = new Search(message.getText(), userSession.getTargetClass());
                search.setGlobalClassSearch(true);
                result = search.getResultOfSearch();
                break;
            case SEARCH_CLASS_FIELD_NAMES:
                search = new Search(message.getText(), userSession.getTargetClass());
                search.setClassFieldNamesSearch(true);
                result = search.getResultOfSearch();
                break;
            case SEARCH_CLASS_FIELD_VALUES:
                search = new Search(message.getText(), userSession.getTargetClass());
                search.setClassFieldValuesSearch(true);
                result = search.getResultOfSearch();
                break;
            case SEARCH_CLASS_DOC_NAMES:
                search = new Search(message.getText(), userSession.getTargetClass());
                search.setClassDocumentNamesSearch(true);
                result = search.getResultOfSearch();
                break;
        }
        if (result != null) {
            userSession.setResultOfSearch(result);
            if (result.size() > 1) {
                sendResponseMessage = getTextMessage(message, userSession.getNextResult());
                sendMessage(sendResponseMessage);
                sendResponseMessage = getNextPreviousMenuMessage(message);
            } else sendResponseMessage = getTextMessage(message, userSession.getNextResult());
        } else sendResponseMessage = getTextMessage(message, BotMessage.SEARCH_RESULT_FAILED_MSG);
        sendMessage(sendResponseMessage);
        return userSession;
    }

    /**
     * get description document by class name and RID
     * @param document string like "/<className><RID>"
     * @return string with description document
     */
    private String goToTargetDocument(final String document) {
        String [] split = document.substring(BotState.GO_TO_CLASS.command.length()).split("_");
        final int clusterID = Integer.valueOf(split[1]);
        final long recordID = Long.valueOf(split[2]);
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
                if (!CLASS_CACHE.containsKey(className)) {
                    return BotMessage.SEARCH_FAILED_CLASS_BY_NAME;
                }
                OClass oClass = CLASS_CACHE.get(className);
                builder.append("<strong>Name: </strong>");
                builder.append(oClass.getName());
                builder.append("\n");
                builder.append("<strong>Super classes: </strong>");
                List<String> superClasses = new ArrayList<>();
                for (OClass oClass1 : oClass.getSuperClasses()) {
                    if (CLASS_CACHE.containsKey(oClass.getName())) {
                        superClasses.add("/" + oClass1.getName() + " ");
                    }
                }
                if (superClasses.size() > 0) {
                    for (String str : superClasses) {
                        builder.append(str);
                    }
                } else builder.append("without superclasses");
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
                    String docId = BotState.GO_TO_CLASS.command + oDocument.getClassName()
                            + "_" + oDocument.getIdentity().getClusterId()
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

    private SendMessage getNextPreviousMenuMessage(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.enableHtml(true);
        sendMessage.enableMarkdown(true);
        sendMessage.setText(BotMessage.NEXT_PREVIOUS_MSG);
        List<String> buttons = new ArrayList<>();
        buttons.add(BotMessage.NEXT_RESULT_BUT);
        buttons.add(BotMessage.PREVIOUS_RESULT_BUT);
        sendMessage.setReplyMarkup(getMenuMarkup(buttons));
        return sendMessage;
    }

    private SendMessage getClassesMenuMessage(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.enableMarkdown(true);
        sendMessage.setText(BotMessage.CLASS_MENU_MSG);
        List<String> buttonNames = new ArrayList<>();
        for (OClass oClass: CLASS_CACHE.values()) {
            buttonNames.add(BotMessage.CLASS_BUT + oClass.getName());
        }
        Collections.sort(buttonNames);
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
        BotState state = BotState.ERROR;
        for (BotState search : BotState.values()) {
            if (search.command.equals(text)) {
                state = search;
                break;
            }
        }
        if (state == BotState.ERROR) {
            if (text.startsWith(BotState.GO_TO_CLASS.command) && text.contains("_")) {
                return BotState.GO_TO_DOCUMENT;
            } else if (text.startsWith(BotState.GO_TO_CLASS.command)) {
                return BotState.GO_TO_CLASS;
            } else if (text.startsWith("/")) {
                return BotState.GO_TO_CLASS;
            } else if (text.startsWith(BotMessage.CLASS_BUT)) {
                return BotState.ClASS_MENU_OPTIONS;
            }
        }
        return state;
    }
}
