package org.orienteer.telegram.bot.response;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import org.orienteer.telegram.bot.BotMessage;
import org.orienteer.telegram.bot.Cache;
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

    public static SendMessage getStartMenu(Message message, BotMessage botMessage) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.enableMarkdown(true);
        sendMessage.setText(botMessage.CLASS_MENU_MSG);

        List<String> buttonNames = new ArrayList<>();
        for (OClass oClass: Cache.getClassCache().values()) {
            buttonNames.add(botMessage.CLASS_BUT + oClass.getName());
        }
        Collections.sort(buttonNames);
        sendMessage.setReplyMarkup(getMenuMarkup(buttonNames));
        return sendMessage;
    }

    public static SendMessage getLanguageMenu(Message message, BotMessage botMessage) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.enableMarkdown(true);
        sendMessage.setText(botMessage.LANGUAGE_MENU_MSG);

        List<String> buttonNames = new ArrayList<>();
        buttonNames.add(botMessage.LANGUAGE_BUT + botMessage.ENGLISH);
        buttonNames.add(botMessage.LANGUAGE_BUT + botMessage.RUSSIAN);
        buttonNames.add(botMessage.LANGUAGE_BUT + botMessage.UKRAINIAN);
        buttonNames.add(botMessage.BACK);
        sendMessage.setReplyMarkup(getMenuMarkup(buttonNames));
        return sendMessage;
    }

    public static SendMessage getBackMenu(Message message, String text, BotMessage botMessage) {
        List<String> keyboard = new ArrayList<>(1);
        keyboard.add(botMessage.BACK);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.enableMarkdown(true);

        sendMessage.setText(text);
        sendMessage.setReplyMarkup(getMenuMarkup(keyboard));
        return sendMessage;
    }

    public static SendMessage getNextPreviousMenu(Message message, boolean hasNext, boolean hasPrevious, BotMessage botMessage) {
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
