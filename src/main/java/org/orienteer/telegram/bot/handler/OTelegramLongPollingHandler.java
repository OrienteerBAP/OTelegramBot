package org.orienteer.telegram.bot.handler;

import org.orienteer.telegram.bot.OTelegramBot;
import org.orienteer.telegram.bot.response.OTelegramBotResponse;
import org.orienteer.telegram.bot.util.OTelegramUpdateHandlerConfig;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.generics.LongPollingBot;

/**
 * Telegram bot handler which use <a href="https://core.telegram.org/bots/api#getupdates">long-polling</a> method for connections with users
 */
public class OTelegramLongPollingHandler extends TelegramLongPollingBot implements IOTelegramBotUpdateHandler<LongPollingBot> {

    private final OTelegramUpdateHandlerConfig config;
    private final OTelegramBotHandler handler;

    public OTelegramLongPollingHandler(OTelegramUpdateHandlerConfig config, OTelegramBot bot) {
        this.config = config;
        handler = new OTelegramBotHandler(bot);
    }

    @Override
    public void onUpdateReceived(Update update) {
        OTelegramBotResponse response = handler.handleRequest(update);
        try {
            SendMessage sendMessage = response.getSendMessage();
            AnswerCallbackQuery answerCallbackQuery = response.getAnswerCallbackQuery();
            EditMessageText editMessageText = response.getEditMessageText();
            if (sendMessage == null) {
                sendApiMethod(answerCallbackQuery);
                sendApiMethod(editMessageText);
            } else sendApiMethod(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public OTelegramBotHandler getHandler() {
        return handler;
    }

    @Override
    public OTelegramUpdateHandlerConfig getConfig() {
        return config;
    }

    @Override
    public LongPollingBot getTelegramHandler() {
        return this;
    }

    @Override
    public String getBotUsername() {
        return config.getUsername();
    }

}
