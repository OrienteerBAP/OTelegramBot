package org.orienteer.telegram.bot.handler;

import com.google.common.cache.LoadingCache;
import org.orienteer.telegram.bot.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 * @author Vitaliy Gonchar
 */
public class OTelegramLongPollingHandler extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(OTelegramLongPollingHandler.class);
    private final LongPolligHandlerConfig longPolligHandlerConfig;
    private final OTelegramBotHandler botHandler;

    public OTelegramLongPollingHandler(LongPolligHandlerConfig longPolligHandlerConfig, LoadingCache<Integer, UserSession> sessions) {
        this.longPolligHandlerConfig = longPolligHandlerConfig;
        botHandler = new OTelegramBotHandler(sessions);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            try {
                handleRequest(botHandler.handleRequest(callbackQuery));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                try {
                    LOG.info("Get message from - " + message.getFrom().getFirstName() + " " + message.getFrom().getLastName());
                    handleRequest(botHandler.handleRequest(message));
                    LOG.info("Send message to - " + message.getFrom().getFirstName() + " " + message.getFrom().getLastName());
                } catch (TelegramApiException e) {
                    LOG.error("Cannot send message");
                    if (LOG.isDebugEnabled()) e.printStackTrace();
                }
            }
        }
    }

    private void handleRequest(OTelegramBotResponse response) throws TelegramApiException {
        SendMessage sendMessage = response.getSendMessage();
        AnswerCallbackQuery answerCallbackQuery = response.getAnswerCallbackQuery();
        EditMessageText editMessageText = response.getEditMessageText();
        if (sendMessage == null) {
            answerCallbackQuery(answerCallbackQuery);
            editMessageText(editMessageText);
        } else sendMessage(sendMessage);
    }

    @Override
    public String getBotToken() {
        return longPolligHandlerConfig.token;
    }

    @Override
    public String getBotUsername() {
        return longPolligHandlerConfig.username;
    }

}
