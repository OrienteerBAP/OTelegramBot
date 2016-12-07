package org.orienteer.telegram.bot;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.Localizer;
import org.apache.wicket.ThreadContext;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.util.CommonUtils;
import org.orienteer.telegram.bot.handler.LongPolligHandlerConfig;
import org.orienteer.telegram.bot.handler.OTelegramLongPollingHandler;
import org.orienteer.telegram.bot.handler.OTelegramWebHookHandler;
import org.orienteer.telegram.bot.handler.WebHookHandlerConfig;
import org.orienteer.telegram.bot.response.BotState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Vitaliy Gonchar
 */
public abstract class OTelegramBot {

    private static final Logger LOG = LoggerFactory.getLogger(OTelegramBot.class);
    private static OrienteerWebApplication application;
    private static UserSession currentSession;
    private static boolean groupChat;

    public static synchronized OTelegramLongPollingHandler getLongPollingBot(LongPolligHandlerConfig botConfig) {
        LoadingCache<Integer, UserSession> sessions = setUpDefaultConfig(botConfig.userSession);
        return new OTelegramLongPollingHandler(botConfig, sessions);
    }

    public static synchronized OTelegramWebHookHandler getWebHookBot(WebHookHandlerConfig botConfig) {
        LoadingCache<Integer, UserSession> sessions = setUpDefaultConfig(botConfig.userSession);
        return new OTelegramWebHookHandler(botConfig, sessions);
    }

    private static synchronized LoadingCache<Integer, UserSession> setUpDefaultConfig(long userSession) {
        LoadingCache<Integer, UserSession> sessions = CacheBuilder.newBuilder()
                .expireAfterWrite(userSession, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<Integer, UserSession>() {
                            @Override
                            public UserSession load(Integer key) {
                                return new UserSession();
                            }
                        });
        return sessions;
    }

    public static String getDocName(ODocument doc) {
        Locale locale = OTelegramBot.getCurrentLocale();
        OProperty nameProp = OrienteerWebApplication.lookupApplication().getOClassIntrospector().getNameProperty(doc.getSchemaClass());
        if (nameProp == null) return MessageKey.WITHOUT_NAME.getString(locale);

        OType type = nameProp.getType();
        Object value = doc.field(nameProp.getName());

        switch (type) {
            case DATE:
                return OrienteerWebApplication.DATE_CONVERTER.convertToString((Date) value, locale);
            case DATETIME:
                return OrienteerWebApplication.DATE_TIME_CONVERTER.convertToString((Date) value, locale);
            case LINK:
                return getDocName((ODocument) value);
            case EMBEDDEDMAP:
                Map<String, Object> localizations = (Map<String, Object>) value;
                Object localized = CommonUtils.localizeByMap(localizations, true, locale.getLanguage(), Locale.getDefault().getLanguage());
                if (localized != null) return localized.toString();
            default:
                return value.toString();
        }
    }


    public static synchronized void setApplication() {
        application = OrienteerWebApplication.lookupApplication();
        ThreadContext.setApplication(application);
    }

    public static boolean isGroupChat() {
        return groupChat;
    }

    public static void setGroupChat(boolean isGroupChat) {
        groupChat = isGroupChat;
    }

    public static synchronized Localizer getLocalizer() {
        return application.getResourceSettings().getLocalizer();
    }

    public static synchronized Locale getCurrentLocale() {
        return currentSession.getLocale();
    }

    public static synchronized void setCurrentLocale(Locale locale) {
        currentSession.setLocale(locale);
    }

    public static synchronized BotState getCurrentBotState() {
        return currentSession.getBotState();
    }

    public static synchronized void setCurrentBotState(BotState botState) {
        currentSession.setBotState(botState);
    }

    public static synchronized UserSession getCurrentSession() {
        return currentSession;
    }

    public static synchronized void setCurrentSession(UserSession userSession) {
        currentSession = userSession;
    }
}
