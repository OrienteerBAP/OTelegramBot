package org.orienteer.telegram.bot.util.telegram;

import com.google.inject.ImplementedBy;
import org.apache.wicket.util.io.IClusterable;
import org.orienteer.telegram.bot.OTelegramBot;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

/**
 * Class which manage Telegram bots
 */
@ImplementedBy(OTelegramBotManager.class)
public interface IOTelegramBotManager extends IClusterable {

    /**
     * Register WebHook or LongPolling bot and start it
     * @param bot {@link OTelegramBot} bot
     */
    public void registerAndStartBot(OTelegramBot bot) throws TelegramApiRequestException;

    /**
     * Unregister bot and stop it by bot token
     * @param token {@link String} bot token
     */
    public void unregisterAndStopBot(String token);

    /**
     * @param token {@link String} bot token
     * @return true if bot with given token already registered
     */
    public boolean isBotAlreadyRegistered(String token);

    /**
     * @param token {@link String} bot token
     * @return {@link OTelegramBot} with given token or null
     */
    public OTelegramBot getBotByToken(String token);
}