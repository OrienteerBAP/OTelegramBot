package org.orienteer.telegram.bot;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.wicket.Localizer;
import org.apache.wicket.ThreadContext;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.telegram.bot.response.BotState;
import org.orienteer.telegram.bot.response.Response;
import org.orienteer.telegram.module.OTelegramModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Vitaliy Gonchar
 */
public class OTelegramBot extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(OTelegramBot.class);
    private final OTelegramModule.BotConfig BOT_CONFIG;
    private final LoadingCache<Integer, UserSession> SESSIONS;

    private static OrienteerWebApplication application;
    private static UserSession currentSession;

    private OTelegramBot(OTelegramModule.BotConfig botConfig, LoadingCache<Integer, UserSession> sessions) {
        BOT_CONFIG = botConfig;
        SESSIONS = sessions;
    }

    public static OTelegramBot getOrienteerTelegramBot(OTelegramModule.BotConfig botConfig) {
        LoadingCache<Integer, UserSession> sessions = CacheBuilder.newBuilder()
                .expireAfterWrite(botConfig.USER_SESSION, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<Integer, UserSession>() {
                    @Override
                    public UserSession load(Integer key) {
                        return null;
                    }
                });
        Cache.initCache();
        return new OTelegramBot(botConfig, sessions);
    }

    @Override
    public String getBotToken() {
        return BOT_CONFIG.TOKEN;
    }

    @Override
    public String getBotUsername() {
        return BOT_CONFIG.USERNAME;
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                try {
                    handleMenuRequest(message);
                } catch (TelegramApiException e) {
                    LOG.error("Cannot send message");
                    if (LOG.isDebugEnabled()) e.printStackTrace();
                }
            }
        }
    }

    private void handleMenuRequest(Message message) throws TelegramApiException {
        UserSession userSession = SESSIONS.getIfPresent(message.getFrom().getId());
        currentSession = userSession == null ? new UserSession() : userSession;
        setApplication();
        List<SendMessage> responses = new Response(message).getResponse();
        SESSIONS.put(message.getFrom().getId(), currentSession);
        for (SendMessage sendMessage : responses) {
            if (sendMessage != null) sendMessage(sendMessage);
        }
    }

    public static Localizer getLocalizer() {
        return application.getResourceSettings().getLocalizer();
    }

    public static void setApplication() {
        application = OrienteerWebApplication.lookupApplication();
        ThreadContext.setApplication(application);
    }

    public static Locale getCurrentLocale() {
        return currentSession.getLocale();
    }

    public static void setCurrentLocale(Locale locale) {
        currentSession.setLocale(locale);
    }

    public static BotState getCurrentBotState() {
        return currentSession.getBotState();
    }

    public static void setCurrentBotState(BotState botState) {
        currentSession.setBotState(botState);
    }

    public static UserSession getCurrentSession() {
        return currentSession;
    }

    public static void setCurrentSession(UserSession userSession) {
        currentSession = userSession;
    }
}
