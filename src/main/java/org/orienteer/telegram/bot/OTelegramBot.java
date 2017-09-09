package org.orienteer.telegram.bot;

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
import org.orienteer.telegram.bot.util.OTelegramUpdateHandlerConfig;
import org.orienteer.telegram.bot.util.OTelegramUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.generics.BotSession;

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

    public void setBotSession(BotSession session) {
        this.botSession = session;
    }

    public BotSession getBotSession() {
        return botSession;
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

    public UserSession getCurrentSession() {
        return currentSession;
    }

    public synchronized void setCurrentSession(UserSession userSession) {
        currentSession = userSession;
    }

    public LoadingCache<Integer, UserSession> getUserSessions() {
        return sessions;
    }

    public boolean useWebHook() {
        return webHook;
    }

    public IOTelegramBotUpdateHandler getUpdateHandler() {
        return updateHandler;
    }
}
