package org.orienteer.telegram.bot.util.telegram;

import com.google.inject.ImplementedBy;
import org.apache.wicket.util.io.IClusterable;
import org.telegram.telegrambots.generics.Webhook;
import org.telegram.telegrambots.generics.WebhookBot;

/**
 * Orienteer Telegram WebHook interface
 */
@ImplementedBy(OWebHook.class)
public interface IOWebHook extends Webhook, IClusterable {

    /**
     * REST url
     */
    public static final String REST_URL = "callback/";

    /**
     * Stop WebHook server
     */
    public void stopServer();

    /**
     * @return true if WebHook server is started
     */
    public boolean isServerStarted();

    /**
     * Unregister bot.
     * @param bot {@link WebhookBot} bot
     */
    public void unregisterWebHook(WebhookBot bot);

}
