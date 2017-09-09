package org.orienteer.telegram.bot.util;

import com.google.inject.ImplementedBy;
import org.apache.wicket.util.io.IClusterable;
import org.orienteer.telegram.bot.OTelegramBot;

/**
 * Registry for register/unregister Orienteer Telegram bots
 */
@ImplementedBy(OTelegramBotRegistry.class)
public interface IOTelegramBotRegistry extends IClusterable {
    public OTelegramBot registerBot(String name, OTelegramBot bot);
    public OTelegramBot unregisterBot(String name);
    public OTelegramBot getBot(String name);
}
