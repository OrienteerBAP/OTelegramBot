package org.orienteer.telegram.bot.handler;

/**
 * Contains config for {@link OTelegramLongPollingHandler}
 */
public class LongPollingHandlerConfig {
    public final String username;
    public final String token;

    public final long userSession;

    public LongPollingHandlerConfig(String username, String token, long userSession) {
        this.username = username;
        this.token = token;
        this.userSession = userSession;
    }

    @Override
    public String toString() {
        return "LongPollingHandlerConfig:"
                + "\nUsername: " + username
                + "\nBot token: " + token
                + "\nUser session: " + userSession;
    }
}
