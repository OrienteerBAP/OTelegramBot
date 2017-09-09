package org.orienteer.telegram.bot.handler;

import org.apache.wicket.util.io.IClusterable;
import org.orienteer.telegram.bot.util.OTelegramUpdateHandlerConfig;

/**
 * Contains information about Telegram update handler
 * @param <T> type which may be {@link org.telegram.telegrambots.generics.LongPollingBot} or {@link org.telegram.telegrambots.generics.WebhookBot}
 */
public interface IOTelegramBotUpdateHandler<T> extends IClusterable {
    public String getBotUsername();
    public String getBotToken();
    public OTelegramBotHandler getHandler();
    public OTelegramUpdateHandlerConfig getConfig();
    public T getTelegramHandler();
}