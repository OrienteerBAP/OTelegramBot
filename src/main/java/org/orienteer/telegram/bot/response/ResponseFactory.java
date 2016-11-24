package org.orienteer.telegram.bot.response;

import org.orienteer.telegram.bot.MessageKey;
import org.orienteer.telegram.bot.OTelegramBot;
import org.orienteer.telegram.bot.UserSession;
import org.orienteer.telegram.bot.link.LinkFactory;
import org.orienteer.telegram.bot.search.Search;
import org.orienteer.telegram.bot.search.SearchFactory;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Vitaliy Gonchar
 */
public class ResponseFactory {
    private final Message message;

    public ResponseFactory(Message message) {
        this.message = message;
    }

    public Response getResponse() {
        BotState state = getBotState(message.getText());
        UserSession userSession = OTelegramBot.getCurrentSession();
        state = state == BotState.BACK ? userSession.getPreviousBotState() : state;
        List<SendMessage> responseList = new ArrayList<>(2);
        Response response = null;
        switch (state) {
            case START:
                userSession.setBotState(BotState.NEW_CLASS_SEARCH);
                userSession.setPreviousBotState(BotState.START);
                responseList.add(ResponseMessage.getStartMenu(message));
                break;
            case CLASS_SEARCH:
                userSession.setTargetClass(message.getText().substring(MessageKey.CLASS_BUT.getString(userSession.getLocale()).length()));
                userSession.setBotState(BotState.SEARCH_IN_CLASS_GLOBAL);
                userSession.setPreviousBotState(BotState.START);
                responseList.add(ResponseMessage.getBackMenu(message, String.format(MessageKey.CLASS_SEARCH_MSG.getString(userSession.getLocale()), "/" + userSession.getTargetClass())));
                break;
            case NEXT_RESULT:
                responseList.add(ResponseMessage.getTextMessage(message, userSession.getNextResult()));
                responseList.add(ResponseMessage.getNextPreviousMenu(message, userSession.hasNextResult(), userSession.hasPreviousResult()));
                break;
            case PREVIOUS_RESULT:
                responseList.add(ResponseMessage.getTextMessage(message, userSession.getPreviousResult()));
                responseList.add(ResponseMessage.getNextPreviousMenu(message, userSession.hasNextResult(), userSession.hasPreviousResult()));
                break;
            case GO_TO_DOCUMENT_SHORT_DESCRIPTION:
                responseList.add(ResponseMessage.getTextMessage(message, LinkFactory.getLink(message.getText(), false).goTo()));
                break;
            case GO_TO_DOCUMENT_ALL_DESCRIPTION:
                responseList.add(ResponseMessage.getTextMessage(message, LinkFactory.getLink(message.getText(), true).goTo()));
                break;
            case GO_TO_CLASS:
                responseList.add(ResponseMessage.getTextMessage(message, LinkFactory.getLink(message.getText()).goTo()));
                break;
            case CHANGE_LANGUAGE:
                userSession.setLocale(changeLanguage(message));
                userSession.setPreviousBotState(BotState.START);
                userSession.setBotState(BotState.NEW_CLASS_SEARCH);
                responseList.add(ResponseMessage.getTextMessage(message, MessageKey.LANGUAGE_CHANGED.getString(userSession.getLocale())));
                responseList.add(ResponseMessage.getStartMenu(message));
                break;
            case LANGUAGE:
                responseList.add(ResponseMessage.getLanguageMenu(message));
                break;
            case ABOUT:
                responseList.add(ResponseMessage.getTextMessage(message, MessageKey.ABOUT_MSG.getString(userSession.getLocale())));
                break;
            default:
                response = handleSearchRequest(message, userSession);
        }

        return response != null ? response : new Response(responseList, userSession);
    }

    private Response handleSearchRequest(Message message, UserSession userSession) {
        List<SendMessage> responseList = new ArrayList<>(2);
        List<String> result = null;
        Search search;
        switch (userSession.getBotState()) {
            case SEARCH_IN_CLASS_GLOBAL:
                search = SearchFactory.getSearch(message.getText(), userSession.getTargetClass(), userSession.getLocale());
                result = search.execute();
                break;
            case NEW_CLASS_SEARCH:
                search = SearchFactory.getSearch(message.getText(), userSession.getLocale());
                result = search.execute();
                break;
        }
        if (result != null) {
            userSession.setResultOfSearch(result);
            if (result.size() > 1) {
                responseList.add(ResponseMessage.getTextMessage(message, userSession.getNextResult()));
                responseList.add(ResponseMessage.getNextPreviousMenu(message, userSession.hasNextResult(), userSession.hasPreviousResult()));
            } else {
                responseList.add(ResponseMessage.getTextMessage(message, MessageKey.START_SEARCH_MSG.getString(userSession.getLocale())));
                responseList.add(ResponseMessage.getTextMessage(message, userSession.getNextResult()));

            }
        } else responseList.add(ResponseMessage.getTextMessage(message, MessageKey.SEARCH_RESULT_FAILED_MSG.getString(userSession.getLocale())));

        return new Response(responseList, userSession);
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
