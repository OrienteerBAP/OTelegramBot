package org.orienteer.telegram.bot.response;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import org.orienteer.telegram.bot.Cache;
import org.orienteer.telegram.bot.MessageKey;
import org.orienteer.telegram.bot.OTelegramBot;
import org.orienteer.telegram.bot.UserSession;
import org.orienteer.telegram.bot.link.Link;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Vitaliy Gonchar
 */
public abstract class ResponseMessage {

    public static SendMessage getTextMessage(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        if (OTelegramBot.isGroupChat()) sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.enableHtml(true);
        sendMessage.setText(text);
        return sendMessage;
    }

    public static SendMessage getStartMenu(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        if (OTelegramBot.isGroupChat()) sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(MessageKey.CLASS_MENU_MSG.getString(OTelegramBot.getCurrentLocale()));

        List<String> buttonNames = new ArrayList<>();
        for (OClass oClass: Cache.getClassCache().values()) {
            buttonNames.add(MessageKey.CLASS_BUT.getString(OTelegramBot.getCurrentLocale())+ oClass.getName());
        }
        Collections.sort(buttonNames);
        sendMessage.setReplyMarkup(getMenuMarkup(buttonNames));
        return sendMessage;
    }

    public static SendMessage getLanguageMenu(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        if (OTelegramBot.isGroupChat()) sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(MessageKey.LANGUAGE_MENU_MSG.getString(OTelegramBot.getCurrentLocale()));

        List<String> buttonNames = new ArrayList<>();
        buttonNames.add(MessageKey.LANGUAGE_BUT.getString(OTelegramBot.getCurrentLocale()) + MessageKey.ENGLISH.toString());
        buttonNames.add(MessageKey.LANGUAGE_BUT.getString(OTelegramBot.getCurrentLocale()) + MessageKey.RUSSIAN.toString());
        buttonNames.add(MessageKey.LANGUAGE_BUT.getString(OTelegramBot.getCurrentLocale()) + MessageKey.UKRAINIAN.toString());
        buttonNames.add(MessageKey.BACK.getString(OTelegramBot.getCurrentLocale()));
        sendMessage.setReplyMarkup(getMenuMarkup(buttonNames));
        return sendMessage;
    }

    public static SendMessage getBackMenu(Message message, String text) {
        List<String> keyboard = new ArrayList<>(1);
        keyboard.add(MessageKey.BACK.getString(OTelegramBot.getCurrentLocale()));
        SendMessage sendMessage = new SendMessage();
        if (OTelegramBot.isGroupChat()) sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(getMenuMarkup(keyboard));
        return sendMessage;
    }

    public static SendMessage getNextPreviousMenu(Message message, String text, boolean hasNext, boolean hasPrevious) {
        SendMessage sendMessage = new SendMessage();
        if (OTelegramBot.isGroupChat()) sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.enableHtml(true);
        sendMessage.setText(text);
        List<String> buttons = new ArrayList<>();
        if (hasNext) buttons.add(MessageKey.NEXT_RESULT_BUT.getString(OTelegramBot.getCurrentLocale()));
        if (hasPrevious) buttons.add(MessageKey.PREVIOUS_RESULT_BUT.getString(OTelegramBot.getCurrentLocale()));
        buttons.add(MessageKey.BACK.getString(OTelegramBot.getCurrentLocale()));
        sendMessage.setReplyMarkup(getMenuMarkup(buttons));
        return sendMessage;
    }

    public static SendMessage getPagingMenu(Message message, UserSession userSession) {
        SendMessage sendMessage = new SendMessage();
        if (OTelegramBot.isGroupChat()) sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.enableHtml(true);
        sendMessage.setText(userSession.getResultInPage());
        sendMessage.setReplyMarkup(getInlinePagingMarkup(userSession.getStart(), userSession.getEnd(), userSession.getPages()));
        return sendMessage;
    }

    public static SendMessage getDocumentDescription(Link link, Message message, UserSession userSession,
                                                     boolean isAllDescription) {
        SendMessage sendMessage = new SendMessage();
        if (OTelegramBot.isGroupChat()) sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.enableHtml(true);
        sendMessage.setText(link.goTo());
        if (link.isWithoutDetails()) {
            sendMessage.setReplyMarkup(getInlineDocumentMarkup(link.getLinkInString(), isAllDescription, userSession));
        }
        return sendMessage;
    }

    public static InlineKeyboardMarkup getInlineDocumentMarkup(String documentLink, boolean isAllDescription, UserSession userSession) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttonList = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        if (isAllDescription) {
            button.setText(MessageKey.SHORT_DOCUMENT_DSCR_BUT.getString(userSession.getLocale()));
            button.setCallbackData(documentLink.substring(0, documentLink.indexOf("_details")));
        } else {
            button.setText(MessageKey.ALL_DOCUMENT_DSCR_BUT.getString(userSession.getLocale()));
            button.setCallbackData(documentLink + "_details");
        }
        buttonList.add(button);
        keyboard.add(buttonList);
        markup.setKeyboard(keyboard);
        return markup;
    }

    public static InlineKeyboardMarkup getInlinePagingMarkup(int start, int end, int size) {
        InlineKeyboardMarkup inlineMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> pageButtons = new ArrayList<>();
        List<InlineKeyboardButton> nextPreviousBut = new ArrayList<>();
        InlineKeyboardButton button;
        int i = start;
        while (i < end) {
            button = new InlineKeyboardButton();
            button.setText(" " + (i + 1));
            button.setCallbackData("" + (i + 1));
            pageButtons.add(button);
            i++;
        }

        if (start != 0) {
            button = new InlineKeyboardButton();
            button.setText("" + '\u23EA');
            button.setCallbackData(BotState.PREVIOUS_RESULT.getCommand());
            nextPreviousBut.add(button);
        }
        if (end != size) {
            button = new InlineKeyboardButton();
            button.setText("" + '\u23E9');
            button.setCallbackData(BotState.NEXT_RESULT.getCommand());
            nextPreviousBut.add(button);
        }
        buttons.add(pageButtons);
        buttons.add(nextPreviousBut);
        inlineMarkup.setKeyboard(buttons);
        return inlineMarkup;
    }

    private static ReplyKeyboardMarkup getMenuMarkup(List<String> buttonNames) {
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
}
