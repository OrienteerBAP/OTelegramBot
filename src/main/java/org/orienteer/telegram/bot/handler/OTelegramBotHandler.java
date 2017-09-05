package org.orienteer.telegram.bot.handler;

import com.google.common.cache.LoadingCache;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.telegram.bot.AbstractOTelegramBot;
import org.orienteer.telegram.bot.UserSession;
import org.orienteer.telegram.bot.response.AbstractBotResponseFactory;
import org.orienteer.telegram.bot.response.CallbackResponse;
import org.orienteer.telegram.bot.response.OTelegramBotResponse;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;

import java.util.concurrent.ExecutionException;

/**
 * Class which handles user requests
 */
public class OTelegramBotHandler {
    private final LoadingCache<Integer, UserSession> sessions;

    public OTelegramBotHandler(LoadingCache<Integer, UserSession> sessions) {
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
        OTelegramBotResponse response = AbstractBotResponseFactory.createResponse(message);
        AbstractOTelegramBot.setGroupChat(!message.getChat().isUserChat());
        sessions.put(message.getFrom().getId(), AbstractOTelegramBot.getCurrentSession());
        AbstractOTelegramBot.setCurrentSession(AbstractOTelegramBot.getCurrentSession());
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
            AbstractOTelegramBot.setCurrentSession(sessions.get(id));
            AbstractOTelegramBot.setApplication(OrienteerWebApplication.lookupApplication());
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
