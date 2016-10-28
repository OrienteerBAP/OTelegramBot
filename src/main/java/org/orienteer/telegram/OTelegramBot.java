package org.orienteer.telegram;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.core.module.IOrienteerModule;
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
        switch (botState) {
            case START:
                sendRequestMessage = getMainMenuMessage(message);
                botState = null;
                break;
            case SEARCH:
                sendRequestMessage = getTextMessage(message, BotMessage.SEARCH_MSG);
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
        switch (botState) {
            case SEARCH:
                String className = message.getText();
                if (isClassExists(className)) {
                    sendRequestMessage = getTextMessage(message, BotMessage.SEARCH_RESULT_SUCCESS_MSG);
                    sendMessage(sendRequestMessage);
                    sendRequestMessage = getTextMessage(message, getInformationAboutClass(className));
                    sendMessage(sendRequestMessage);
                    sendRequestMessage = getClassMenuMessage(message, className);
                } else {
                    sendRequestMessage = getTextMessage(message, BotMessage.SEARCH_RESULT_FAILED_MSG);
                }
                botState = null;
                break;
            default:
                sendRequestMessage = getTextMessage(message, BotMessage.ERROR_MSG);
                botState = null;
        }
        return sendRequestMessage;
    }

    private String getInformationAboutClass(final String className) {
        String result = (String) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                OClass oClass = oDatabaseDocument.getMetadata().getSchema().getClass(className);
                StringBuilder stringBuilder = new StringBuilder();
                Collection<OProperty> properties = oClass.properties();
                stringBuilder.append(oClass.getName() + " properties:\n");
                for (OProperty property : properties) {
                    stringBuilder.append("Name: " );
                    stringBuilder.append(property.getName() + "\n");
                    stringBuilder.append("Type: ");
                    stringBuilder.append(property.getType() + "\n");
                    stringBuilder.append("Default value: " + property.getDefaultValue() + "\n");
                }
                return stringBuilder.toString();
            }
        }.execute();

        return result;
    }

    private SendMessage getClassMenuMessage(Message message, final String className) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.enableMarkdown(true);
        sendMessage.setText(BotMessage.CLASS_MENU_MSG);
        List<String> buttonNames = (List<String>) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                List<String> result = new ArrayList<>();
                ORecordIteratorClass<ODocument> oDocuments = oDatabaseDocument.browseClass(className);
                for (ODocument document: oDocuments) {
                    if (document.field(IOrienteerModule.OMODULE_NAME) != null) {
                        result.add(document.field(IOrienteerModule.OMODULE_NAME).toString());
                    }
                }
                return result;
            }
        }.execute();
        sendMessage.setReplyMarkup(getMenuMarkup(buttonNames));
        return sendMessage;
    }

    private boolean isClassExists(final String className) {
        Boolean isExist = (Boolean) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                return oDatabaseDocument.getMetadata().getSchema().existsClass(className);
            }
        }.execute();

        return isExist;
    }

    private SendMessage getTextMessage(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
     //   sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(text);
        return sendMessage;
    }

    private SendMessage getMainMenuMessage(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.enableMarkdown(true);
        sendMessage.setText(BotMessage.MAIN_MENU_MSG);
        List<String> buttonNames = new ArrayList<>();
        buttonNames.add(BotMessage.SEARCH_BUTTON);
        sendMessage.setReplyMarkup(getMenuMarkup(buttonNames));
        return sendMessage;
    }

    private ReplyKeyboardMarkup getMenuMarkup(List<String> buttonNames) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
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
        SEARCH(BotMessage.SEARCH_BUTTON),
        GET("/get"),
        ERROR("/error");

        private String command;
        BotState(String command) {
            this.command = command;
        }

    }

    private interface BotMessage {
        String MAIN_MENU_MSG = "Send me name of class and I will try to find it.";
        String CLASS_MENU_MSG = "Choose document of class and I will send information about him.";
        String ERROR_MSG = "Error message";
        String SEARCH_MSG = "Type your class name:";
        String SEARCH_RESULT_SUCCESS_MSG = "I found class!";
        String SEARCH_RESULT_FAILED_MSG = "I cannot found class!";

        String SEARCH_BUTTON = "New search";
    }
}
