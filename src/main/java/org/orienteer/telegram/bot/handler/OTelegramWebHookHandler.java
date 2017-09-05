package org.orienteer.telegram.bot.handler;

import com.google.common.cache.LoadingCache;
import org.orienteer.telegram.bot.UserSession;
import org.orienteer.telegram.bot.response.OTelegramBotResponse;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 * Telegram bot handler which use WebHooks for connections with users
 */
public class OTelegramWebHookHandler extends TelegramWebhookBot {
    private final WebHookHandlerConfig botConfig;
    private final OTelegramBotHandler botHandler;

    public OTelegramWebHookHandler(WebHookHandlerConfig botConfig, LoadingCache<Integer, UserSession> sessions) {
        this.botConfig = botConfig;
        botHandler = new OTelegramBotHandler(sessions);
    }

    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            try {
                BotApiMethod botApiMethod = handleRequest(botHandler.handleRequest(callbackQuery));
                return botApiMethod;
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                try {
                    BotApiMethod result = handleRequest(botHandler.handleRequest(message));
                    return result;
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private BotApiMethod handleRequest(OTelegramBotResponse response) throws TelegramApiException {
        SendMessage sendMessage = response.getSendMessage();
        AnswerCallbackQuery answerCallbackQuery = response.getAnswerCallbackQuery();
        EditMessageText editMessageText = response.getEditMessageText();
        if (sendMessage == null) {
            sendApiMethod(answerCallbackQuery);
            return editMessageText;
        } else return sendMessage;
    }

    @Override
    public String getBotUsername() {
        return botConfig.username;
    }

    @Override
    public String getBotToken() {
        return botConfig.token;
    }

    @Override
    public String getBotPath() {
        return botConfig.username;
    }
}
