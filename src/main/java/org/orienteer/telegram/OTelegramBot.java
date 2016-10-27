package org.orienteer.telegram;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

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
                sendRequestMessage = getTextMessage(message, "Orienteer bot");
                break;
            case INPUT:
                sendRequestMessage = getTextMessage(message, "User input");
                break;
            case SEARCH:
                sendRequestMessage = getTextMessage(message, "Search class");
                break;
            case GET:
                sendRequestMessage = getTextMessage(message, "Get class");
                break;
            default:
                sendRequestMessage = getTextMessage(message, "Error");
                break;
        }
        sendMessage(sendRequestMessage);
    }

    private SendMessage getTextMessage(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(text);
        return sendMessage;
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
        SEARCH("/search"),
        GET("/get"),
        ERROR("__error");

        private String command;
        BotState(String command) {
            this.command = command;
        }

    }
}
