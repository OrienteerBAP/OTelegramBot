package org.orienteer.telegram.bot.handler;

import org.apache.wicket.ThreadContext;
import org.apache.wicket.util.io.IClusterable;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.telegram.bot.OTelegramBot;
import org.orienteer.telegram.bot.response.AbstractBotResponseFactory;
import org.orienteer.telegram.bot.response.CallbackResponse;
import org.orienteer.telegram.bot.response.OTelegramBotResponse;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;

import java.util.concurrent.ExecutionException;

/**
 * Class which handles user requests
 */
public class OTelegramBotHandler implements IClusterable {

    private final OTelegramBot bot;

    public OTelegramBotHandler(OTelegramBot bot) {
        this.bot = bot;
    }

    public OTelegramBotResponse handleRequest(Update update) {
        OTelegramBotResponse response = null;
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            response = handleCallbackQuery(callbackQuery);
        } else if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                response = handleMessage(message);
            }
        }
        return response;
    }

    private OTelegramBotResponse handleMessage(Message message) {
        setCurrentSession(message.getFrom().getId());
        OTelegramBotResponse response = AbstractBotResponseFactory.createResponse(bot, message);
        bot.setGroupChat(!message.getChat().isUserChat());
        bot.getUserSessions().put(message.getFrom().getId(), bot.getCurrentSession());
        bot.setCurrentSession(bot.getCurrentSession());
        return response;
    }

    private OTelegramBotResponse handleCallbackQuery(CallbackQuery callbackQuery) {
        setCurrentSession(callbackQuery.getFrom().getId());
        OTelegramBotResponse response;
        CallbackResponse callbackResponse = new CallbackResponse(bot, callbackQuery);
        AnswerCallbackQuery callbackAnswer = callbackResponse.getCallbackAnswer();
        response = new OTelegramBotResponse(callbackAnswer, callbackResponse.getEditMessage());
        bot.getUserSessions().put(callbackQuery.getFrom().getId(), callbackResponse.getUserSession());
        return response;
    }

    private void setCurrentSession(int id) {
        try {
            bot.setCurrentSession(bot.getUserSessions().get(id));
            ThreadContext.setApplication(OrienteerWebApplication.lookupApplication());
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
