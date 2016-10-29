package org.orienteer.telegram;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.id.ORID;
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
import java.util.List;

/**
 * @author Vitaliy Gonchar
 */
public class OTelegramBot extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(OTelegramBot.class);
    private final OTelegramModule.BotConfig BOT_CONFIG;

    private BotState botState;
    private String targetClass;

    public OTelegramBot(OTelegramModule.BotConfig botConfig) {
        BOT_CONFIG = botConfig;
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
        if (botState == null) {
            sendRequestMessage = handleNewBotState(message);
        } else {
            sendRequestMessage = handleOldBotState(message);
        }
        sendMessage(sendRequestMessage);
    }

    private SendMessage handleNewBotState(Message message) {
        SendMessage sendRequestMessage;
        botState = getBotState(message.getText());
        LOG.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        LOG.debug("botState: " + botState);
        LOG.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        switch (botState) {
            case BACK_TO_MAIN_MENU:
            case START:
                sendRequestMessage = getMainMenuMessage(message);
                botState = null;
                break;
            case GLOBAL_SEARCH:
                sendRequestMessage = getTextMessage(message, BotMessage.SEARCH_MSG);
                break;
            case CLASS_SEARCH_MENU:
                sendRequestMessage = getClassesMenuMessage(message);
                break;
            case GET:
                sendRequestMessage = getTextMessage(message, "Get class");
                botState = null;
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
        LOG.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        LOG.debug("start botState in handleOldState: " + botState);
        LOG.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        switch (botState) {
            case GLOBAL_SEARCH:
                sendRequestMessage = getTextMessage(message, searchDocumentsInGlobal(message.getText()));
                botState = null;
                break;
            case CLASS_SEARCH_MENU:
                if (!message.getText().equals(BotMessage.BACK_TO_MAIN_MENU_BUT)) {
                    targetClass = message.getText();
                    LOG.debug("target class: " + targetClass);
                    sendRequestMessage = getDocumentSearchMenu(message);
                    botState = BotState.DOC_CLASS_SEARCH;
                } else {
                    sendRequestMessage = getMainMenuMessage(message);
                    botState = null;
                }
                break;
            case DOC_CLASS_SEARCH:
                if (!message.getText().equals(BotMessage.BACK_TO_CLASS_SEARCH_BUT)) {
                    sendRequestMessage = getTextMessage(message, searchDocumentsInClass(message.getText(), targetClass));
                } else sendRequestMessage = getClassesMenuMessage(message);
                targetClass = null;
                botState = null;
                break;
            default:
                sendRequestMessage = getTextMessage(message, BotMessage.ERROR_MSG);
                botState = null;
        }
        LOG.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        LOG.debug("finish botState in handleOldState: " + botState);
        LOG.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return sendRequestMessage;
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
     * Search documents with similar names from all database
     * @param documentName name of target document
     * @return result of search
     */
    private String searchDocumentsInGlobal(final String documentName) {
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
                if (result.length() > 0) result = BotMessage.SEARCH_RESULT_SUCCESS_MSG + result;
                return result;
            }
        }.execute();
        return resultOfSearch;
    }


    private List<String> search(final String word) {
        List<String> result = ;

        return result;
    }

    /**
     * Build string with result of search
     * @param oDocuments list of documents where will be search
     * @param documentName name of target document
     * @return string with result of search
     */
    private String getDocumentString(ORecordIteratorClass<ODocument> oDocuments, String documentName) {
        StringBuilder builder = new StringBuilder();
        for (ODocument oDocument : oDocuments) {
            String docName = oDocument.field("name", OType.STRING);
            if (docName != null && docName.contains(documentName)) {
                ORID identity = oDocument.getIdentity();
                builder.append(docName);
                builder.append(" ");
                builder.append("/");
                builder.append(oDocument.getClassName());
                builder.append(identity.getClusterId());
                builder.append("_");
                builder.append(identity.getClusterPosition());
                builder.append("\n");
            }
        }
        return builder.toString();
    }

//    private String getInformationAboutDocument(final String className) {
//        String result = (String) new DBClosure() {
//            @Override
//            protected Object execute(ODatabaseDocument oDatabaseDocument) {
//                StringBuilder stringBuilder = new StringBuilder();
//                OClass oClass = oDatabaseDocument.getMetadata().getSchema().getClass(className);
//                Collection<OProperty> properties = oClass.properties();
//                List<OClass> superClasses = oClass.getSuperClasses();
//                stringBuilder.append(oClass.getName());
//                stringBuilder.append(" properties:\n");
//                for (OProperty property : properties) {
//                    stringBuilder.append("Name: " );
//                    stringBuilder.append(property.getName());
//                    stringBuilder.append("\n");
//                    stringBuilder.append("Type: ");
//                    stringBuilder.append(property.getType());
//                    stringBuilder.append("\n");
//                    stringBuilder.append("Default value: ");
//                    stringBuilder.append(property.getDefaultValue());
//                    stringBuilder.append("\n");
//                }
//                return stringBuilder.toString();
//            }
//        }.execute();
//
//        return result;
//    }

    private SendMessage getClassesMenuMessage(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
       // sendMessage.setReplyToMessageId(message.getMessageId());
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
                return result;
            }
        }.execute();
        buttonNames.add(BotMessage.BACK_TO_MAIN_MENU_BUT);
        sendMessage.setReplyMarkup(getMenuMarkup(buttonNames));
        return sendMessage;
    }

    private SendMessage getDocumentSearchMenu(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(BotMessage.SEARCH_MSG);
        //sendMessage.setReplyToMessageId(message.getMessageId());
        List<String> buttons = new ArrayList<>();
        buttons.add(BotMessage.BACK_TO_CLASS_SEARCH_BUT);
        sendMessage.setReplyMarkup(getMenuMarkup(buttons));
        return sendMessage;
    }

    private SendMessage getTextMessage(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
      //  sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(text);
        return sendMessage;
    }

    private SendMessage getMainMenuMessage(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        //sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.enableMarkdown(true);
        sendMessage.setText(BotMessage.MAIN_MENU_MSG);
        List<String> buttonNames = new ArrayList<>();
        buttonNames.add(BotMessage.CLASS_SEARCH_BUT);
        buttonNames.add(BotMessage.GLOBAL_SEARCH_BUT);
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
        return state;
    }

    private enum BotState {
        START("/start"),
        INPUT("/input"),
        GLOBAL_SEARCH(BotMessage.GLOBAL_SEARCH_BUT),
        CLASS_SEARCH_MENU(BotMessage.CLASS_SEARCH_BUT),
        DOC_CLASS_SEARCH("/docclasssearch"),
        DOC_SEARCH("/docsearch"),
        BACK_TO_MAIN_MENU(BotMessage.BACK_TO_MAIN_MENU_BUT),
        BACK_TO_CLASS_SEARCH(BotMessage.BACK_TO_CLASS_SEARCH_BUT),
        CLASS("/class"),
        GET("/get"),
        ERROR("/error");

        private String command;
        BotState(String command) {
            this.command = command;
        }

    }

    private interface BotMessage {
        String MAIN_MENU_MSG = "Choose search option and send me name of document. I will try to find it.";
        String CLASS_MENU_MSG = "Choose class in the list.";
        String ERROR_MSG = "I don't understand you :(";
        String SEARCH_MSG = "Send me name of document and I will try to find it:";
        String SEARCH_RESULT_SUCCESS_MSG = "I found:\n\n";
        String SEARCH_RESULT_FAILED_MSG = "I cannot found something!";

        String GLOBAL_SEARCH_BUT = "Global search";
        String CLASS_SEARCH_BUT = "Class search";
        String BACK_TO_CLASS_SEARCH_BUT = "Back to class search";
        String BACK_TO_MAIN_MENU_BUT = "Back to main menu";
    }
}
