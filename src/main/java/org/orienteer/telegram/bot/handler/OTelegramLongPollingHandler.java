package org.orienteer.telegram.bot.handler;

import com.google.common.cache.LoadingCache;
import org.orienteer.telegram.bot.UserSession;
import org.orienteer.telegram.bot.response.OTelegramBotResponse;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 * Telegram bot handler which use <a href="https://core.telegram.org/bots/api#getupdates">long-polling</a> method for connections with users
 */
public class OTelegramLongPollingHandler extends TelegramLongPollingBot {

    private final LongPollingHandlerConfig longPollingHandlerConfig;
    private final OTelegramBotHandler botHandler;

    public OTelegramLongPollingHandler(LongPollingHandlerConfig longPollingHandlerConfig, LoadingCache<Integer, UserSession> sessions) {
        this.longPollingHandlerConfig = longPollingHandlerConfig;
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
                    handleRequest(botHandler.handleRequest(message));
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleRequest(OTelegramBotResponse response) throws TelegramApiException {
        SendMessage sendMessage = response.getSendMessage();
        AnswerCallbackQuery answerCallbackQuery = response.getAnswerCallbackQuery();
        EditMessageText editMessageText = response.getEditMessageText();
        if (sendMessage == null) {
            sendApiMethod(answerCallbackQuery);
            sendApiMethod(editMessageText);
        } else sendApiMethod(sendMessage);
    }

    @Override
    public String getBotToken() {
        return longPollingHandlerConfig.token;
    }

    @Override
    public String getBotUsername() {
        return longPollingHandlerConfig.username;
    }

}
