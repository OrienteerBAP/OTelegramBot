package org.orienteer.telegram.bot.response;

import org.orienteer.telegram.bot.UserSession;
import org.telegram.telegrambots.api.methods.send.SendMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vitaliy Gonchar
 */
public class Response {

    private final List<SendMessage> responses;
    private final UserSession userSession;

    public Response(List<SendMessage> responses, UserSession userSession) {
        this.responses = responses;
        this.userSession = userSession;
    }

    public List<SendMessage> getResponses() {
        return responses;
    }

    public UserSession getNewUserSession() {
        return userSession;
    }
}
