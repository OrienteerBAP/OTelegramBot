package org.orienteer.telegram.bot;

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.http.util.Args;
import org.apache.wicket.Localizer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.io.IClusterable;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.telegram.bot.handler.IOTelegramBotUpdateHandler;
import org.orienteer.telegram.bot.handler.OTelegramLongPollingHandler;
import org.orienteer.telegram.bot.handler.OTelegramWebHookHandler;
import org.orienteer.telegram.bot.util.BotState;
import org.orienteer.telegram.bot.util.OTelegramUpdateHandlerConfig;
import org.orienteer.telegram.bot.util.OTelegramUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.generics.BotSession;
import org.telegram.telegrambots.generics.LongPollingBot;
import org.telegram.telegrambots.generics.WebhookBot;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Class for manage telegram bot
 */
public class OTelegramBot implements IClusterable {

    private static final Logger LOG = LoggerFactory.getLogger(OTelegramBot.class);

    private UserSession currentSession;
    private BotSession botSession;
    private final LoadingCache<Integer, UserSession> sessions;
    private final IOTelegramBotUpdateHandler updateHandler;
    private final boolean webHook;
    private boolean groupChat;

    public OTelegramBot(IModel<ODocument> botDocument, boolean webHook) {
        this(OTelegramUtil.readBotConfig(botDocument), webHook);
    }

    public OTelegramBot(OTelegramUpdateHandlerConfig config, boolean webHook) {
        Args.notNull(config, "config");
        this.sessions = setUpDefaultConfig(config.getUserSession());
        this.webHook = webHook;
        this.updateHandler = webHook ? new OTelegramWebHookHandler(config, this) : new OTelegramLongPollingHandler(config, this);
    }

    private synchronized LoadingCache<Integer, UserSession> setUpDefaultConfig(long userSession) {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(userSession, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<Integer, UserSession>() {
                            @Override
                            public UserSession load(Integer key) {
                                return new UserSession();
                            }
                        });
    }

    public synchronized boolean start() {
        boolean started = false;
        try {
            TelegramBotsApi api;
            if (webHook) {
                api = newWebHookBotApi(updateHandler.getConfig());
                api.registerBot((WebhookBot) updateHandler.getTelegramHandler());
            } else {
                api = new TelegramBotsApi();
                botSession = api.registerBot((LongPollingBot) updateHandler.getTelegramHandler());
            }
            LOG.info("Start Orienteer Telegram bot with username: {}", updateHandler.getBotUsername());
            started = true;
        } catch (TelegramApiRequestException e) {
            stop();
            LOG.error("Can't start Orienteer Telegram bot with username: {}", updateHandler.getBotUsername());
            if (LOG.isDebugEnabled()) e.printStackTrace();
        }
        return started;
    }

    public synchronized void stop() {
        if (webHook) {
            LOG.info("Stop WebHook Orienteer Telegram bot with username: {}", updateHandler.getBotUsername());
        } else {
            botSession.stop();
            LOG.info("Stop LongPolling Orienteer Telegram bot with username: {}", updateHandler.getBotUsername());
        }
    }


    private TelegramBotsApi newWebHookBotApi(OTelegramUpdateHandlerConfig config) throws TelegramApiRequestException {
        TelegramBotsApi result;
        if (Strings.isNullOrEmpty(config.getCertificateStorePassword()) ||
                Strings.isNullOrEmpty(config.getPathToCertificateStore()) ||
                Strings.isNullOrEmpty(config.getPathToPublicKey())) {
            result = new TelegramBotsApi(config.getExternalUrl(), config.getInternalUrl());
        } else {
            result = new TelegramBotsApi(
                    config.getPathToCertificateStore(),
                    config.getCertificateStorePassword(),
                    config.getExternalUrl(),
                    config.getInternalUrl(),
                    config.getPathToPublicKey());
        }

        return result;
    }

    public boolean isGroupChat() {
        return groupChat;
    }

    public synchronized void setGroupChat(boolean isGroupChat) {
        groupChat = isGroupChat;
    }

    public Localizer getLocalizer() {
        return OrienteerWebApplication.lookupApplication().getResourceSettings().getLocalizer();
    }

    public Locale getCurrentLocale() {
        return currentSession.getLocale();
    }

    public synchronized void setCurrentLocale(Locale locale) {
        currentSession.setLocale(locale);
    }

    public BotState getCurrentBotState() {
        return currentSession.getBotState();
    }

    public synchronized void setCurrentBotState(BotState botState) {
        currentSession.setBotState(botState);
    }

    public UserSession getCurrentSession() {
        return currentSession;
    }

    public synchronized void setCurrentSession(UserSession userSession) {
        currentSession = userSession;
    }

    public LoadingCache<Integer, UserSession> getUserSessions() {
        return sessions;
    }
}
