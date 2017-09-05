package org.orienteer.telegram.bot;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.Application;
import org.apache.wicket.Localizer;
import org.apache.wicket.ThreadContext;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.util.CommonUtils;
import org.orienteer.telegram.bot.handler.LongPollingHandlerConfig;
import org.orienteer.telegram.bot.handler.OTelegramLongPollingHandler;
import org.orienteer.telegram.bot.handler.OTelegramWebHookHandler;
import org.orienteer.telegram.bot.handler.WebHookHandlerConfig;
import org.orienteer.telegram.bot.util.BotState;
import org.orienteer.telegram.bot.util.MessageKey;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Abstract class for manage telegram bot
 */
public abstract class AbstractOTelegramBot {

    private static Application application;
    private static UserSession currentSession;
    private static boolean groupChat;

    /**
     * Create new {@link OTelegramLongPollingHandler}
     * @param botConfig {@link LongPollingHandlerConfig} - bot config
     * @return {@link OTelegramLongPollingHandler}
     */
    public static synchronized OTelegramLongPollingHandler getLongPollingBot(LongPollingHandlerConfig botConfig) {
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

    @SuppressWarnings("unchecked")
    public static String getDocName(ODocument doc) {
        Locale locale = AbstractOTelegramBot.getCurrentLocale();
        OProperty nameProp = OrienteerWebApplication.lookupApplication().getOClassIntrospector().getNameProperty(doc.getSchemaClass());
        if (nameProp == null) return MessageKey.WITHOUT_NAME.getString();

        OType type = nameProp.getType();
        Object value = doc.field(nameProp.getName());
        if (value != null) {
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
        } else return "";
    }


    public static synchronized void setApplication(Application app) {
        application = app;
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
