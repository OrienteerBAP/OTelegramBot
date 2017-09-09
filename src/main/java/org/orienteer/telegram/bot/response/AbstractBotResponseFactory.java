package org.orienteer.telegram.bot.response;

import org.orienteer.telegram.bot.OTelegramBot;
import org.orienteer.telegram.bot.UserSession;
import org.orienteer.telegram.bot.util.*;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;

import java.util.Locale;
import java.util.Map;

/**
 * Abstract factory which creates {@link OTelegramBotResponse}
 */
public abstract class AbstractBotResponseFactory {

    public static OTelegramBotResponse createResponse(OTelegramBot bot, Message message) {
        UserSession userSession = bot.getCurrentSession();
        BotState state = getBotState(bot, message.getText());
        SendMessage sendMessage;
        state = state == BotState.BACK ? userSession.getPreviousBotState() : state;
        if (state == null) {
            userSession.setBotState(BotState.NEW_CLASS_SEARCH);
            userSession.setPreviousBotState(BotState.START);
            sendMessage = AbstractResponseMessageFactory.createStartMenu(bot, message);
            return new OTelegramBotResponse(sendMessage);
        } else return new OTelegramBotResponse(createSendMessageFromState(bot, state, message, userSession));
    }

    private static SendMessage createSendMessageFromState(OTelegramBot bot, BotState state, Message message, UserSession userSession) {
        SendMessage sendMessage;
        switch (state) {
            case START:
                userSession.setBotState(BotState.NEW_CLASS_SEARCH);
                userSession.setPreviousBotState(BotState.START);
                sendMessage = AbstractResponseMessageFactory.createStartMenu(bot, message);
                break;
            case CLASS_SEARCH:
                userSession.setTargetClass(message.getText().substring(MessageKey.CLASS_BUT.toLocaleString(bot).length()));
                userSession.setBotState(BotState.SEARCH_IN_CLASS_GLOBAL);
                userSession.setPreviousBotState(BotState.START);
                sendMessage = AbstractResponseMessageFactory.createBackMenu(bot, message,
                        String.format(MessageKey.CLASS_SEARCH_MSG.toLocaleString(bot), "/" + userSession.getTargetClass()));
                break;
            case GO_TO_DOCUMENT_SHORT_DESCRIPTION:
                sendMessage = AbstractResponseMessageFactory.createDocumentDescription(bot,
                        new ODocumentTelegramDescription(message.getText(), false, bot), message, userSession, false);
                break;
            case GO_TO_CLASS:
                sendMessage = setResultOfSearch(bot,
                        new OClassTelegramDescription(message.getText(), bot).getDescription(), message, userSession);
                break;
            case CHANGE_LANGUAGE:
                userSession.setLocale(changeLanguage(bot, message));
                userSession.setPreviousBotState(BotState.START);
                userSession.setBotState(BotState.NEW_CLASS_SEARCH);
                sendMessage = AbstractResponseMessageFactory.createStartMenu(bot, message);
                break;
            case LANGUAGE:
                sendMessage = AbstractResponseMessageFactory.createLanguageMenu(bot, message);
                break;
            case ABOUT:
                sendMessage = AbstractResponseMessageFactory.createTextMessage(bot, message, MessageKey.ABOUT_MSG.toLocaleString(bot));
                break;
            default:
                sendMessage = handleSearchRequest(bot, message, userSession);
        }
        return sendMessage;
    }

    private static SendMessage handleSearchRequest(OTelegramBot bot, Message message, UserSession userSession) {
        Map<Integer, String> result = null;
        AbstractSearch search;
        SendMessage sendMessage;
        switch (userSession.getBotState()) {
            case SEARCH_IN_CLASS_GLOBAL:
                search = AbstractSearch.newSearch(bot, message.getText(), userSession.getTargetClass());
                result = search.search();
                break;
            case NEW_CLASS_SEARCH:
                search = AbstractSearch.newSearch(bot, message.getText(), null);
                result = search.search();
                break;
        }
        if (result != null) {
            sendMessage = setResultOfSearch(bot, result, message, userSession);
        } else sendMessage = AbstractResponseMessageFactory.createTextMessage(bot, message,
                Markdown.BOLD.toString(MessageKey.SEARCH_RESULT_FAILED_MSG.toLocaleString(bot)));

        return sendMessage;
    }

    private static SendMessage setResultOfSearch(OTelegramBot bot, Map<Integer, String> resultOfSearch, Message message, UserSession userSession) {
        SendMessage sendMessage;
        userSession.setResultOfSearch(resultOfSearch);
        if (resultOfSearch.size() > 1) {
            sendMessage = AbstractResponseMessageFactory.createPagingMenu(bot, message, userSession);
        } else {
            sendMessage = AbstractResponseMessageFactory.createTextMessage(bot, message, userSession.getResultInPage());
        }
        return sendMessage;
    }

    private static Locale changeLanguage(OTelegramBot bot, Message message) {
        String lang = message.getText().substring(MessageKey.LANGUAGE_BUT.toLocaleString(bot).length());
        if (lang.equals(MessageKey.ENGLISH.toString())) {
            return new Locale("en");
        } else if (lang.equals(MessageKey.RUSSIAN.toString())) {
            return new Locale("ru");
        } else {
            return new Locale("uk");
        }
    }

    private static BotState getBotState(OTelegramBot bot, String command) {
        BotState state = searchBotState(command);
        if (state == BotState.ERROR) {
            if (command.startsWith(BotState.GO_TO_CLASS.getCommand()) && command.endsWith(BotState.DETAILS.getCommand())) {
                state = BotState.GO_TO_DOCUMENT_ALL_DESCRIPTION;
            } else if (command.startsWith(MessageKey.LANGUAGE_BUT.toLocaleString(bot))) {
                state = BotState.CHANGE_LANGUAGE;
            } else if (command.startsWith(BotState.GO_TO_CLASS.getCommand()) &&
                    command.contains(BotState.GO_TO_DOCUMENT_SHORT_DESCRIPTION.getCommand())) {
                state = BotState.GO_TO_DOCUMENT_SHORT_DESCRIPTION;
            } else if (command.startsWith(BotState.GO_TO_CLASS.getCommand())) {
                state = BotState.GO_TO_CLASS;
            } else if (command.startsWith(MessageKey.CLASS_BUT.toLocaleString(bot))) {
                state = BotState.CLASS_SEARCH;
            } else if (command.equals(MessageKey.BACK.toLocaleString(bot))) {
                state = BotState.BACK;
            }
        }
        return state;
    }

    private static BotState searchBotState(String command) {
        BotState result = BotState.ERROR;
        for (BotState search : BotState.values()) {
            if (search.getCommand().equals(command)) {
                result = search;
                break;
            }
        }
        return result;
    }
}
