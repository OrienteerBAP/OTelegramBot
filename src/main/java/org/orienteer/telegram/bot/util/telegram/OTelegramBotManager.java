package org.orienteer.telegram.bot.util.telegram;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.orienteer.telegram.bot.OTelegramBot;
import org.orienteer.telegram.bot.util.OTelegramUpdateHandlerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContext;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.generics.BotSession;
import org.telegram.telegrambots.generics.LongPollingBot;
import org.telegram.telegrambots.generics.WebhookBot;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link IOTelegramBotManager}
 */
@Singleton
public class OTelegramBotManager implements IOTelegramBotManager {

    private static final Logger LOG = LoggerFactory.getLogger(OTelegramBotManager.class);
    private static final Object LOCK = new Object();
    private final ConcurrentHashMap<String, OTelegramBot> telegramBots = new ConcurrentHashMap<>();

    @Inject
    private IOWebHook webHook;

    @Override
    public void registerAndStartBot(OTelegramBot bot) {
        synchronized (LOCK) {
            try {
                if (bot.useWebHook()) {
                    startWebHookBot(bot);
                } else {
                    startLongPollingBot(bot);
                }
                telegramBots.put(bot.getUpdateHandler().getBotToken(), bot);
                LOG.info("Start Telegram bot with name: {}", bot.getUpdateHandler().getBotUsername());
            } catch (TelegramApiRequestException e) {
                LOG.warn("Can't start Telegram bot with name: {}", bot.getUpdateHandler().getBotUsername());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void unregisterAndStopBot(String token) {
        synchronized (LOCK) {
            if (telegramBots.containsKey(token)) {
                OTelegramBot bot = telegramBots.remove(token);
                if (bot.useWebHook()) {
                    WebhookBot webhookBot = (WebhookBot) bot.getUpdateHandler().getTelegramHandler();
                    webHook.unregisterWebHook(webhookBot);
                } else {
                    bot.getBotSession().stop();
                }
                LOG.info("Stop Telegram bot with token: {}", bot.getUpdateHandler().getBotUsername());
            } else LOG.info("Telegram bot with token {} don't registered!", token);
        }
    }

    @Override
    public boolean isBotAlreadyRegistered(String token) {
        return telegramBots.get(token) != null;
    }

    private void startWebHookBot(OTelegramBot bot) throws TelegramApiRequestException {
        OTelegramUpdateHandlerConfig config = bot.getUpdateHandler().getConfig();
        WebhookBot webhookBot = (WebhookBot) bot.getUpdateHandler().getTelegramHandler();
        if (!webHook.isServerStarted()) {
            webHook.setInternalUrl(config.getInternalUrl());
            if (!Strings.isNullOrEmpty(config.getPathToCertificateStore()) &&
                    !Strings.isNullOrEmpty(config.getCertificateStorePassword())) {
                webHook.setKeyStore(config.getPathToCertificateStore(), config.getCertificateStorePassword());
            }
            webHook.startServer();
        }
        webHook.registerWebhook(webhookBot);
        webhookBot.setWebhook(getExternalWebHookUrl(config.getExternalUrl(), webhookBot.getBotUsername()),
                config.getPathToPublicKey());
    }

    private void startLongPollingBot(OTelegramBot telegramBot) throws TelegramApiRequestException {
        LongPollingBot bot = (LongPollingBot) telegramBot.getUpdateHandler();
        bot.clearWebhook();
        BotSession session = ApiContext.getInstance(BotSession.class);
        session.setToken(bot.getBotToken());
        session.setOptions(bot.getOptions());
        session.setCallback(bot);
        session.start();
    }

    private String getExternalWebHookUrl(String externalUrl, String name) {
        if (externalUrl != null && !externalUrl.endsWith("/")) {
            externalUrl += "/";
        }
        return externalUrl + webHook.REST_URL + name;
    }
}
