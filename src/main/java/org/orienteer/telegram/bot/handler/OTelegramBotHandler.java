package org.orienteer.telegram.bot.handler;

import com.google.common.cache.LoadingCache;
import org.orienteer.telegram.bot.OTelegramBot;
import org.orienteer.telegram.bot.UserSession;
import org.orienteer.telegram.bot.response.CallbackResponse;
import org.orienteer.telegram.bot.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.concurrent.ExecutionException;

/**
 * @author Vitaliy Gonchar
 */
class OTelegramBotHandler {
    private final LoadingCache<Integer, UserSession> sessions;

    private static final Logger LOG = LoggerFactory.getLogger(OTelegramBotHandler.class);

    OTelegramBotHandler(LoadingCache<Integer, UserSession> sessions) {
        this.sessions = sessions;
    }

    public OTelegramBotResponse handleRequest(Message message) {
        return handleMessage(message);
    }

    public OTelegramBotResponse handleRequest(CallbackQuery callbackQuery) {
        return handleCallbackQuery(callbackQuery);
    }

    private OTelegramBotResponse handleMessage(Message message) {
        setCurrentSession(message.getFrom().getId());
        Response response1 = new Response(message);
        SendMessage response2 = response1.getResponse();
        OTelegramBotResponse response = new OTelegramBotResponse(response2);
        OTelegramBot.setGroupChat(!message.getChat().isUserChat());
        LOG.debug("Is user chat : " + message.getChat().isUserChat());
        sessions.put(message.getFrom().getId(), OTelegramBot.getCurrentSession());
        return response;
    }

    private OTelegramBotResponse handleCallbackQuery(CallbackQuery callbackQuery) {
        setCurrentSession(callbackQuery.getFrom().getId());
        OTelegramBotResponse response;
        CallbackResponse callbackResponse = new CallbackResponse(callbackQuery);
        AnswerCallbackQuery callbackAnswer = callbackResponse.getCallbackAnswer();
        response = new OTelegramBotResponse(callbackAnswer, callbackResponse.getEditMessage());
        sessions.put(callbackQuery.getFrom().getId(), callbackResponse.getUserSession());
        return response;
    }

    private void setCurrentSession(int id) {
        try {
            OTelegramBot.setCurrentSession(sessions.get(id));
            OTelegramBot.setApplication();
        } catch (ExecutionException e) {
            LOG.error("Cannot create user session");
            if (LOG.isDebugEnabled()) e.printStackTrace();
        }
    }
}
