package org.orienteer.telegram.bot.handler;

import com.google.common.cache.LoadingCache;
import org.orienteer.telegram.bot.OTelegramBot;
import org.orienteer.telegram.bot.UserSession;
import org.orienteer.telegram.bot.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.concurrent.ExecutionException;

/**
 * @author Vitaliy Gonchar
 */
public class OTelegramWebHookHandler extends TelegramWebhookBot {
    private static final Logger LOG = LoggerFactory.getLogger(OTelegramWebHookHandler.class);
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
                    LOG.info("Get message from - " + message.getFrom().getFirstName() + " " + message.getFrom().getLastName());
                    BotApiMethod result = handleRequest(botHandler.handleRequest(message));
                    LOG.info("Send message to - " + message.getFrom().getFirstName() + " " + message.getFrom().getLastName());
                    return result;
                } catch (TelegramApiException e) {
                    LOG.error("Cannot send message");
                    if (LOG.isDebugEnabled()) e.printStackTrace();
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
            answerCallbackQuery(answerCallbackQuery);
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
