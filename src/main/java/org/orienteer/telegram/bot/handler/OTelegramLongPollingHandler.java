package org.orienteer.telegram.bot.handler;

import com.google.common.cache.LoadingCache;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.Application;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.cycle.RequestCycleContext;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.model.ODocumentNameModel;
import org.orienteer.telegram.bot.OTelegramBot;
import org.orienteer.telegram.bot.UserSession;
import org.orienteer.telegram.bot.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import ru.ydn.wicket.wicketorientdb.OrientDbWebApplication;
import ru.ydn.wicket.wicketorientdb.OrientDbWebSession;
import ru.ydn.wicket.wicketorientdb.OrientDefaultExceptionsHandlingListener;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.concurrent.ExecutionException;

/**
 * @author Vitaliy Gonchar
 */
public class OTelegramLongPollingHandler extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(OTelegramLongPollingHandler.class);
    private final LongPolligHandlerConfig longPolligHandlerConfig;
    private final LoadingCache<Integer, UserSession> sessions;

    public OTelegramLongPollingHandler(LongPolligHandlerConfig longPolligHandlerConfig, LoadingCache<Integer, UserSession> sessions) {
        this.longPolligHandlerConfig = longPolligHandlerConfig;
        this.sessions = sessions;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                try {
                    LOG.info("Get message from - " + message.getFrom().getFirstName() + " " + message.getFrom().getLastName());
                    handleIncomingMessage(message);
                    LOG.info("Send message to - " + message.getFrom().getFirstName() + " " + message.getFrom().getLastName());
                } catch (TelegramApiException e) {
                    LOG.error("Cannot send message");
                    if (LOG.isDebugEnabled()) e.printStackTrace();
                }
            }
        }
    }

    public void handleIncomingMessage(Message message) throws TelegramApiException {
        try {
            OTelegramBot.setCurrentSession(sessions.get(message.getFrom().getId()));
        } catch (ExecutionException e) {
            LOG.error("Cannot create user session");
            if (LOG.isDebugEnabled()) e.printStackTrace();
        }
        OTelegramBot.setApplication();
        OTelegramBot.setGroupChat(!message.getChat().isUserChat());
        LOG.info("Is user chat : " + message.getChat().isUserChat());

        SendMessage response = new Response(message).getResponse();
        sessions.put(message.getFrom().getId(), OTelegramBot.getCurrentSession());
        sendMessage(response);
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
