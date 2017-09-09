package org.orienteer.telegram.bot.handler;

import org.orienteer.telegram.bot.OTelegramBot;
import org.orienteer.telegram.bot.response.OTelegramBotResponse;
import org.orienteer.telegram.bot.util.OTelegramUpdateHandlerConfig;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.generics.WebhookBot;

/**
 * Telegram bot handler which use WebHooks for connections with users
 */
public class OTelegramWebHookHandler extends TelegramWebhookBot implements IOTelegramBotUpdateHandler<WebhookBot> {
    private final OTelegramUpdateHandlerConfig config;
    private final OTelegramBotHandler handler;

    public OTelegramWebHookHandler(OTelegramUpdateHandlerConfig config, OTelegramBot bot) {
        this.config = config;
        handler = new OTelegramBotHandler(bot);
    }

    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {
        OTelegramBotResponse response = handler.handleRequest(update);
        return response != null ? getBotApiMethod(response) : null;
    }

    private BotApiMethod getBotApiMethod(OTelegramBotResponse response) {
        SendMessage sendMessage = response.getSendMessage();
        AnswerCallbackQuery answerCallbackQuery = response.getAnswerCallbackQuery();
        EditMessageText editMessageText = response.getEditMessageText();
        if (sendMessage == null) {
            try {
                sendApiMethod(answerCallbackQuery);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return editMessageText;
        } else return sendMessage;
    }

    @Override
    public String getBotUsername() {
        return config.getUsername();
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
    public WebhookBot getTelegramHandler() {
        return this;
    }

    @Override
    public String getBotPath() {
        return config.getUsername();
    }
}
