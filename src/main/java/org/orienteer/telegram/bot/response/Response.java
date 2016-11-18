package org.orienteer.telegram.bot.response;

import org.orienteer.telegram.bot.UserSession;
import org.telegram.telegrambots.api.methods.send.SendMessage;

import java.util.ArrayList;

/**
 * @author Vitaliy Gonchar
 */
public class Response {

    private final ArrayList<SendMessage> responses;
    private final UserSession userSession;

    public Response(ArrayList<SendMessage> responses, UserSession userSession) {
        this.responses = responses;
        this.userSession = userSession;
    }

    public ArrayList<SendMessage> getResponses() {
        return responses;
    }

    public UserSession getNewUserSession() {
        return userSession;
    }
}
