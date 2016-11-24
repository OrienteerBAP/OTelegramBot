package org.orienteer.telegram.bot.response;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import org.orienteer.telegram.bot.Cache;
import org.orienteer.telegram.bot.MessageKey;
import org.orienteer.telegram.bot.OTelegramBot;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
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
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.enableHtml(true);
        sendMessage.setText(text);
        return sendMessage;
    }

    public static SendMessage getStartMenu(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.enableMarkdown(true);
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
        sendMessage.enableMarkdown(true);
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
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.enableMarkdown(true);

        sendMessage.setText(text);
        sendMessage.setReplyMarkup(getMenuMarkup(keyboard));
        return sendMessage;
    }

    public static SendMessage getNextPreviousMenu(Message message, boolean hasNext, boolean hasPrevious) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.enableHtml(true);
        sendMessage.enableMarkdown(true);
        sendMessage.setText(MessageKey.START_SEARCH_MSG.getString(OTelegramBot.getCurrentLocale()));
        List<String> buttons = new ArrayList<>();
        if (hasNext) buttons.add(MessageKey.NEXT_RESULT_BUT.getString(OTelegramBot.getCurrentLocale()));
        if (hasPrevious) buttons.add(MessageKey.PREVIOUS_RESULT_BUT.getString(OTelegramBot.getCurrentLocale()));
        buttons.add(MessageKey.BACK.getString(OTelegramBot.getCurrentLocale()));
        sendMessage.setReplyMarkup(getMenuMarkup(buttons));
        return sendMessage;
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
