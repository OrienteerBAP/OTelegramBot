package org.orienteer.telegram.bot.handler;

import com.google.common.cache.LoadingCache;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.telegram.bot.OTelegramBot;
import org.orienteer.telegram.bot.UserSession;
import org.orienteer.telegram.bot.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
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
    private final LoadingCache<Integer, UserSession> sessions;

    public OTelegramWebHookHandler(WebHookHandlerConfig botConfig, LoadingCache<Integer, UserSession> sessions) {
        this.botConfig = botConfig;
        this.sessions = sessions;
    }

    @Override
    public BotApiMethod onWebhookUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                try {
                    LOG.info("Get message from - " + message.getFrom().getFirstName() + " " + message.getFrom().getLastName());
                    SendMessage result = handleIncomingMessage(message);
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


    public SendMessage handleIncomingMessage(Message message) throws TelegramApiException {
        try {
            OTelegramBot.setCurrentSession(sessions.get(message.getFrom().getId()));
        } catch (ExecutionException e) {
            LOG.error("Cannot create user session");
            if (LOG.isDebugEnabled()) e.printStackTrace();
        }
        OTelegramBot.setApplication();
        SendMessage response = new Response(message).getResponse();
        sessions.put(message.getFrom().getId(), OTelegramBot.getCurrentSession());
        return response;
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
