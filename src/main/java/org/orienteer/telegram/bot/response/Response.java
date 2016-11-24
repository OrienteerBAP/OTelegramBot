package org.orienteer.telegram.bot.response;

import org.orienteer.telegram.bot.MessageKey;
import org.orienteer.telegram.bot.OTelegramBot;
import org.orienteer.telegram.bot.UserSession;
import org.orienteer.telegram.bot.link.Link;
import org.orienteer.telegram.bot.search.Search;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Vitaliy Gonchar
 */
public class Response {
    private final Message message;

    private List<SendMessage> responses;
    private UserSession userSession;

    public Response(Message message) {
        this.message = message;
        userSession = OTelegramBot.getCurrentSession();
        responses = new ArrayList<>(2);
    }

    public List<SendMessage> getResponse() {
        BotState state = getBotState(message.getText());

        state = state == BotState.BACK ? userSession.getPreviousBotState() : state;
        switch (state) {
            case START:
                userSession.setBotState(BotState.NEW_CLASS_SEARCH);
                userSession.setPreviousBotState(BotState.START);
                responses.add(ResponseMessage.getStartMenu(message));
                break;
            case CLASS_SEARCH:
                userSession.setTargetClass(message.getText().substring(MessageKey.CLASS_BUT.getString(userSession.getLocale()).length()));
                userSession.setBotState(BotState.SEARCH_IN_CLASS_GLOBAL);
                userSession.setPreviousBotState(BotState.START);
                responses.add(ResponseMessage.getBackMenu(message, String.format(MessageKey.CLASS_SEARCH_MSG.getString(userSession.getLocale()), "/" + userSession.getTargetClass())));
                break;
            case NEXT_RESULT:
                String next = userSession.getNextResult();
                responses.add(ResponseMessage.getNextPreviousMenu(message, userSession.hasNextResult(), userSession.hasPreviousResult()));
                responses.add(ResponseMessage.getTextMessage(message, next));
                break;
            case PREVIOUS_RESULT:
                String previous = userSession.getPreviousResult();
                responses.add(ResponseMessage.getNextPreviousMenu(message, userSession.hasNextResult(), userSession.hasPreviousResult()));
                responses.add(ResponseMessage.getTextMessage(message, previous));
                break;
            case GO_TO_DOCUMENT_SHORT_DESCRIPTION:
                responses.add(ResponseMessage.getTextMessage(message, Link.getLink(message.getText(), false).goTo()));
                break;
            case GO_TO_DOCUMENT_ALL_DESCRIPTION:
                responses.add(ResponseMessage.getTextMessage(message, Link.getLink(message.getText(), true).goTo()));
                break;
            case GO_TO_CLASS:
                responses.add(ResponseMessage.getTextMessage(message, Link.getLink(message.getText()).goTo()));
                break;
            case CHANGE_LANGUAGE:
                userSession.setLocale(changeLanguage(message));
                userSession.setPreviousBotState(BotState.START);
                userSession.setBotState(BotState.NEW_CLASS_SEARCH);
                responses.add(ResponseMessage.getTextMessage(message, MessageKey.LANGUAGE_CHANGED.getString(userSession.getLocale())));
                responses.add(ResponseMessage.getStartMenu(message));
                break;
            case LANGUAGE:
                responses.add(ResponseMessage.getLanguageMenu(message));
                break;
            case ABOUT:
                responses.add(ResponseMessage.getTextMessage(message, MessageKey.ABOUT_MSG.getString(userSession.getLocale())));
                break;
            default:
                handleSearchRequest(message);
        }
        OTelegramBot.setCurrentSession(userSession);
        return responses;
    }

    private void handleSearchRequest(Message message) {
        List<String> result = null;
        Search search;
        switch (userSession.getBotState()) {
            case SEARCH_IN_CLASS_GLOBAL:
                search = Search.getSearch(message.getText(), userSession.getTargetClass(), userSession.getLocale());
                result = search.execute();
                break;
            case NEW_CLASS_SEARCH:
                search = Search.getSearch(message.getText(), null, userSession.getLocale());
                result = search.execute();
                break;
        }
        if (result != null) {
            userSession.setResultOfSearch(result);
            if (result.size() > 1) {
                responses.add(ResponseMessage.getNextPreviousMenu(message, userSession.hasNextResult(), userSession.hasPreviousResult()));
                responses.add(ResponseMessage.getTextMessage(message, userSession.getNextResult()));
            } else {
                responses.add(ResponseMessage.getTextMessage(message, MessageKey.START_SEARCH_MSG.getString(userSession.getLocale())));
                responses.add(ResponseMessage.getTextMessage(message, userSession.getNextResult()));
            }
        } else responses.add(ResponseMessage.getTextMessage(message, MessageKey.SEARCH_RESULT_FAILED_MSG.getString(userSession.getLocale())));
    }

    private Locale changeLanguage(Message message) {
        String lang = message.getText().substring(MessageKey.LANGUAGE_BUT.getString(OTelegramBot.getCurrentLocale()).length());
        if (lang.equals(MessageKey.ENGLISH.toString())) {
            return new Locale("en");
        } else if (lang.equals(MessageKey.RUSSIAN.toString())) {
            return new Locale("ru");
        } else {
            return new Locale("uk");
        }
    }

    private BotState getBotState(String text) {
        BotState state = BotState.ERROR;
        for (BotState search : BotState.values()) {
            if (search.getCommand().equals(text)) {
                state = search;
                break;
            }
        }
        if (state == BotState.ERROR) {
            if (text.startsWith(BotState.GO_TO_CLASS.getCommand()) && text.endsWith("_details")) {
                return BotState.GO_TO_DOCUMENT_ALL_DESCRIPTION;
            }
            if (text.startsWith(MessageKey.LANGUAGE_BUT.getString(OTelegramBot.getCurrentLocale()))) {
                return BotState.CHANGE_LANGUAGE;
            }
            if (text.startsWith(BotState.GO_TO_CLASS.getCommand()) && text.contains("_")) {
                return BotState.GO_TO_DOCUMENT_SHORT_DESCRIPTION;
            } else if (text.startsWith(BotState.GO_TO_CLASS.getCommand())) {
                return BotState.GO_TO_CLASS;
            } else if (text.startsWith("/")) {
                return BotState.GO_TO_CLASS;
            } else if (text.startsWith(MessageKey.CLASS_BUT.getString(OTelegramBot.getCurrentLocale()))) {
                return BotState.CLASS_SEARCH;
            } else if (text.equals(MessageKey.NEXT_RESULT_BUT.getString(OTelegramBot.getCurrentLocale()))) {
                return BotState.NEXT_RESULT;
            } else if (text.endsWith(MessageKey.PREVIOUS_RESULT_BUT.getString(OTelegramBot.getCurrentLocale()))) {
                return BotState.PREVIOUS_RESULT;
            } else if (text.equals(MessageKey.BACK.getString(OTelegramBot.getCurrentLocale()))) {
                return BotState.BACK;
            }
        }
        return state;
    }
}
