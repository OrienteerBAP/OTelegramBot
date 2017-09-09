package org.orienteer.telegram.bot.util;

import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import org.orienteer.telegram.bot.OTelegramBot;

import java.util.Map;

/**
 * Default implementation {@link IOTelegramBotRegistry}
 */
@Singleton
public class OTelegramBotRegistry implements IOTelegramBotRegistry {

    private final Map<String, OTelegramBot> registry;

    public OTelegramBotRegistry() {
        registry = Maps.newHashMap();
    }

    @Override
    public OTelegramBot registerBot(String name, OTelegramBot bot) {
        if (!registry.containsKey(name)) {
            return registry.put(name, bot);
        } else throw new IllegalStateException("Telegram bot with name '" + name + "' is already registered!");
    }

    @Override
    public OTelegramBot unregisterBot(String name) {
        if (registry.containsKey(name)) {
            return registry.remove(name);
        } else throw new IllegalStateException("Telegram bot with name '" + name + "' don't registered!");
    }

    @Override
    public OTelegramBot getBot(String name) {
        return registry.get(name);
    }
}
