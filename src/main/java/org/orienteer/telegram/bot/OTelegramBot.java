package org.orienteer.telegram.bot;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.exception.ORecordNotFoundException;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.core.CustomAttribute;
import org.orienteer.telegram.bot.link.Link;
import org.orienteer.telegram.bot.link.LinkFactory;
import org.orienteer.telegram.bot.search.Search;
import org.orienteer.telegram.bot.search.SearchFactory;
import org.orienteer.telegram.module.OTelegramModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Vitaliy Gonchar
 */
public class OTelegramBot extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(OTelegramBot.class);
    private final OTelegramModule.BotConfig BOT_CONFIG;
    private final LoadingCache<Integer, UserSession> SESSIONS;

    private final BotMessage botMessage;

    private OTelegramBot(OTelegramModule.BotConfig botConfig, LoadingCache<Integer, UserSession> sessions) {
        BOT_CONFIG = botConfig;
        SESSIONS = sessions;
        botMessage = new BotMessage();
    }

    public static OTelegramBot getOrienteerTelegramBot(OTelegramModule.BotConfig botConfig) {
        LoadingCache<Integer, UserSession> sessions = CacheBuilder.newBuilder()
                .expireAfterWrite(botConfig.USER_SESSION, TimeUnit.MINUTES)
                .build(
                        new CacheLoader<Integer, UserSession>() {
                    @Override
                    public UserSession load(Integer key) {
                        return null;
                    }
                });
        Cache.initCache();
        return new OTelegramBot(botConfig, sessions);
    }

    @Override
    public String getBotToken() {
        return BOT_CONFIG.TOKEN;
    }

    @Override
    public String getBotUsername() {
        return BOT_CONFIG.USERNAME;
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                try {
                    handleMenuRequest(message);
                } catch (TelegramApiException e) {
                    LOG.error("Cannot send message");
                    if (LOG.isDebugEnabled()) e.printStackTrace();
                }
            }
        }
    }

    private void handleMenuRequest(Message message) throws TelegramApiException {
        SendMessage sendResponseMessage = null;
        UserSession userSession = SESSIONS.getIfPresent(message.getFrom().getId());
        if (userSession == null) {
            userSession = new UserSession();
            userSession.setBotState(getBotState(message.getText()));
        }
        BotState state = getBotState(message.getText());
        state = state == BotState.BACK ? userSession.getPreviousBotState() : state;
        switch (state) {
            case START:
                sendResponseMessage = getClassesMenuMessage(message);
                userSession.setBotState(BotState.NEW_CLASS_SEARCH);
                userSession.setPreviousBotState(BotState.START);
                break;
            case CLASS_SEARCH:
                userSession.setTargetClass(message.getText().substring(botMessage.CLASS_BUT.length()));
                userSession.setBotState(BotState.SEARCH_IN_CLASS_GLOBAL);
                userSession.setPreviousBotState(BotState.START);
                sendResponseMessage = getBackMenuMessage(message, String.format(botMessage.CLASS_SEARCH_MSG, "/" + userSession.getTargetClass()));
                break;
            case NEXT_RESULT:
                sendResponseMessage = getTextMessage(message, userSession.getNextResult());
                sendMessage(getNextPreviousMenuMessage(message, userSession.hasNextResult(), userSession.hasPreviousResult()));
                break;
            case PREVIOUS_RESULT:
                sendResponseMessage = getTextMessage(message, userSession.getPreviousResult());
                sendMessage(getNextPreviousMenuMessage(message, userSession.hasNextResult(), userSession.hasPreviousResult()));
                break;
            case GO_TO_DOCUMENT_SHORT_DESCRIPTION:
                sendResponseMessage = getTextMessage(message, LinkFactory.getLink(message.getText(), false, botMessage).goTo());
                break;
            case GO_TO_DOCUMENT_ALL_DESCRIPTION:
                sendResponseMessage = getTextMessage(message, LinkFactory.getLink(message.getText(), true, botMessage).goTo());
                break;
            case GO_TO_CLASS:
                sendResponseMessage = getTextMessage(message, LinkFactory.getLink(message.getText(), botMessage).goTo());
                break;
            case LANGUAGE:
                sendResponseMessage = getTextMessage(message, "<strong>Localization is in develop mode.</strong>");
                break;
            case ABOUT:
                sendResponseMessage = getTextMessage(message, botMessage.ABOUT_MSG);
                break;
            default:
                userSession = handleSearchRequest(message, userSession);
        }
        SESSIONS.put(message.getFrom().getId(), userSession);
        if (sendResponseMessage != null) sendMessage(sendResponseMessage);
    }

    private UserSession handleSearchRequest(Message message, UserSession userSession) throws TelegramApiException {
        SendMessage sendResponseMessage = null;
        ArrayList<String> result = null;
        Search search;
        switch (userSession.getBotState()) {
            case SEARCH_IN_CLASS_GLOBAL:
                search = SearchFactory.getSearch(message.getText(), userSession.getTargetClass(), botMessage);
                result = search.execute();
                break;
            case NEW_CLASS_SEARCH:
                search = SearchFactory.getSearch(message.getText(), botMessage);
                result = search.execute();
                break;
        }
        if (result != null) {
            userSession.setResultOfSearch(result);
            if (result.size() > 1) {
                sendResponseMessage = getTextMessage(message, userSession.getNextResult());
                sendMessage(getNextPreviousMenuMessage(message, userSession.hasNextResult(), userSession.hasPreviousResult()));
            } else {
                sendMessage(getTextMessage(message, botMessage.START_SEARCH_MSG));
                sendResponseMessage = getTextMessage(message, userSession.getNextResult());
            }
        } else sendResponseMessage = getTextMessage(message, botMessage.SEARCH_RESULT_FAILED_MSG);
        sendMessage(sendResponseMessage);
        return userSession;
    }

    private SendMessage getNextPreviousMenuMessage(Message message, boolean hasNext, boolean hasPrevious) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.enableHtml(true);
        sendMessage.enableMarkdown(true);
        sendMessage.setText(botMessage.START_SEARCH_MSG);
        List<String> buttons = new ArrayList<>();
        if (hasNext) buttons.add(botMessage.NEXT_RESULT_BUT);
        if (hasPrevious) buttons.add(botMessage.PREVIOUS_RESULT_BUT);
        buttons.add(botMessage.BACK);
        sendMessage.setReplyMarkup(getMenuMarkup(buttons));
        return sendMessage;
    }

    private SendMessage getClassesMenuMessage(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.enableMarkdown(true);
        sendMessage.setText(botMessage.CLASS_MENU_MSG);

        LOG.debug("menu: " + botMessage.CLASS_MENU_MSG);
        List<String> buttonNames = new ArrayList<>();
        for (OClass oClass: Cache.getClassCache().values()) {
            buttonNames.add(botMessage.CLASS_BUT + oClass.getName());
        }
        Collections.sort(buttonNames);
        sendMessage.setReplyMarkup(getMenuMarkup(buttonNames));
        return sendMessage;
    }

    private SendMessage getTextMessage(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.enableHtml(true);
        sendMessage.setText(text);
        return sendMessage;
    }

    private SendMessage getBackMenuMessage(Message message, String text) {
        List<String> keyboard = new ArrayList<>(1);
        keyboard.add(botMessage.BACK);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.enableMarkdown(true);

        sendMessage.setText(text);
        sendMessage.setReplyMarkup(getMenuMarkup(keyboard));
        return sendMessage;
    }

    private ReplyKeyboardMarkup getMenuMarkup(List<String> buttonNames) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setOneTimeKeyboad(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        for (String buttonName: buttonNames) {
            KeyboardRow keyboardRow = new KeyboardRow();
            keyboardRow.add(buttonName);
            keyboard.add(keyboardRow);
        }
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
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
            if (text.startsWith(BotState.GO_TO_CLASS.getCommand()) && text.contains("_")) {
                return BotState.GO_TO_DOCUMENT_SHORT_DESCRIPTION;
            } else if (text.startsWith(BotState.GO_TO_CLASS.getCommand())) {
                return BotState.GO_TO_CLASS;
            } else if (text.startsWith("/")) {
                return BotState.GO_TO_CLASS;
            } else if (text.startsWith(botMessage.CLASS_BUT)) {
                return BotState.CLASS_SEARCH;
            } else if (text.equals(botMessage.NEXT_RESULT_BUT)) {
                return BotState.NEXT_RESULT;
            } else if (text.endsWith(botMessage.PREVIOUS_RESULT_BUT)) {
                return BotState.PREVIOUS_RESULT;
            } else if (text.equals(botMessage.BACK)) {
                return BotState.BACK;
            }
        }
        return state;
    }
}
