package org.orienteer.telegram;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Vitaliy Gonchar
 */
public class OTelegramBot extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(OTelegramBot.class);
    private final OTelegramModule.BotConfig BOT_CONFIG;
    private final ODatabaseDocument DATABASE;


    public OTelegramBot(OTelegramModule.BotConfig botConfig, ODatabaseDocument db) {
        BOT_CONFIG = botConfig;
        DATABASE = db;
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
    public void onClosing() {

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
        BotState state = getBotState(message.getText());
        SendMessage sendRequestMessage;

        switch (state) {
            case START:
                sendRequestMessage = getMainMenuMessage(message);
                break;
            case INPUT:
                String className = message.getText();
                if (isClassExists(className)) {
                    sendRequestMessage = getTextMessage(message, BotMessage.SEARCH_RESULT_SUCCESS_MSG);
                    sendMessage(sendRequestMessage);
                    sendRequestMessage = getTextMessage(message, getInformationAboutClass(className));
                } else {
                    sendRequestMessage = getTextMessage(message, BotMessage.SEARCH_RESULT_FAILED_MSG);
                }
                break;
            case SEARCH:
                sendRequestMessage = getTextMessage(message, BotMessage.SEARCH_MSG);
                break;
            case GET:
                sendRequestMessage = getTextMessage(message, "Get class");
                break;
            default:
                sendRequestMessage = getTextMessage(message, BotMessage.ERROR_MSG);
                break;
        }
        sendMessage(sendRequestMessage);
    }

    private String getInformationAboutClass(String className) {
        OClass oClass = DATABASE.getMetadata().getSchema().getClass(className);
        StringBuilder stringBuilder = new StringBuilder();
        Collection<OProperty> properties = oClass.getIndexedProperties();
        stringBuilder.append(oClass.getName() + " properties:\n");
        for (OProperty property : properties) {
            stringBuilder.append("Name: " + property.getName()
                    + " default value" + property.getDefaultValue() + "\n");
        }

        return stringBuilder.toString();
    }

    private boolean isClassExists(String className) {
        return DATABASE.getMetadata().getSchema().existsClass(className);
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
     //   sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.enableMarkdown(true);
        sendMessage.setText(BotMessage.MAIN_MENU_MSG);
        sendMessage.setReplyMarkup(getMainMenuMarkup());
        return sendMessage;
    }

    private ReplyKeyboardMarkup getMainMenuMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstKeyboardRow = new KeyboardRow();
        firstKeyboardRow.add(BotMessage.SEARCH_BUTTON);
        keyboard.add(firstKeyboardRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }


    private BotState getBotState(String text) {
        BotState state = BotState.INPUT;
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
        GET("/get");

        private String command;
        BotState(String command) {
            this.command = command;
        }

    }

    private interface BotMessage {
        String MAIN_MENU_MSG = "Send me name of class and I will try to find it.";
        String ERROR_MSG = "Error message";
        String SEARCH_MSG = "Type your class name:";
        String SEARCH_RESULT_SUCCESS_MSG = "I found class!";
        String SEARCH_RESULT_FAILED_MSG = "I cannot found class!";

        String SEARCH_BUTTON = "New search";
    }
}
