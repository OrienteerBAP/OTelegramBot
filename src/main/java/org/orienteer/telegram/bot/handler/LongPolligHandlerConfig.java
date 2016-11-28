package org.orienteer.telegram.bot.handler;

/**
 * @author Vitaliy Gonchar
 */
public class LongPolligHandlerConfig {
    public final String username;
    public final String token;

    public final long userSession;

    public LongPolligHandlerConfig(String username, String token, long userSession) {
        this.username = username;
        this.token = token;
        this.userSession = userSession;
    }

    @Override
    public String toString() {
        return "LongPolligHandlerConfig:"
                + "\nUsername: " + username
                + "\nBot token: " + token
                + "\nUser session: " + userSession;
    }
}
