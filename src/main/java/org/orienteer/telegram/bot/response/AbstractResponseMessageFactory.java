package org.orienteer.telegram.bot.response;

import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import org.orienteer.telegram.bot.OTelegramBot;
import org.orienteer.telegram.bot.OTelegramCache;
import org.orienteer.telegram.bot.UserSession;
import org.orienteer.telegram.bot.util.BotState;
import org.orienteer.telegram.bot.util.MessageKey;
import org.orienteer.telegram.bot.util.ODocumentTelegramDescription;
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
 * Abstract factory which creates messages for response
 */
public abstract class AbstractResponseMessageFactory {

    public static SendMessage newSendMessage() {
        SendMessage msg = new SendMessage();
        msg.enableMarkdown(true);
        return msg;
    }

    public static SendMessage createTextMessage(OTelegramBot bot, Message message, String text) {
        SendMessage sendMessage = newSendMessage();
        if (bot.isGroupChat()) sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(text);
        return sendMessage;
    }

    public static SendMessage createStartMenu(OTelegramBot bot, Message message) {
        SendMessage sendMessage = newSendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        if (bot.isGroupChat()) sendMessage.setReplyToMessageId(message.getMessageId());

        List<String> buttonNames = Lists.newArrayList();
        for (OClass oClass: OTelegramCache.getClassCache().values()) {
            buttonNames.add(MessageKey.CLASS_BUT.toLocaleString(bot)+ oClass.getName());
        }
        if (!buttonNames.isEmpty()) {
            Collections.sort(buttonNames);
            sendMessage.setText(MessageKey.CLASS_MENU_MSG.toLocaleString(bot));
            sendMessage.setReplyMarkup(createMenuMarkup(buttonNames));
        } else sendMessage.setText(MessageKey.CLASS_MENU_EMPTY_MSG.toLocaleString(bot));
        return sendMessage;
    }

    public static SendMessage createLanguageMenu(OTelegramBot bot, Message message) {
        SendMessage sendMessage = newSendMessage();
        sendMessage.setChatId(message.getChatId().toString());
        if (bot.isGroupChat()) sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(MessageKey.LANGUAGE_MENU_MSG.toLocaleString(bot));

        List<String> buttonNames = new ArrayList<>();
        buttonNames.add(MessageKey.LANGUAGE_BUT.toLocaleString(bot) + MessageKey.ENGLISH.toString());
        buttonNames.add(MessageKey.LANGUAGE_BUT.toLocaleString(bot) + MessageKey.RUSSIAN.toString());
        buttonNames.add(MessageKey.LANGUAGE_BUT.toLocaleString(bot) + MessageKey.UKRAINIAN.toString());
        buttonNames.add(MessageKey.BACK.toLocaleString(bot));
        sendMessage.setReplyMarkup(createMenuMarkup(buttonNames));
        return sendMessage;
    }

    public static SendMessage createBackMenu(OTelegramBot bot, Message message, String text) {
        List<String> keyboard = new ArrayList<>(1);
        keyboard.add(MessageKey.BACK.toLocaleString(bot));
        SendMessage sendMessage = newSendMessage();
        if (bot.isGroupChat()) sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(createMenuMarkup(keyboard));
        return sendMessage;
    }

    public static SendMessage createPagingMenu(OTelegramBot bot, Message message, UserSession userSession) {
        SendMessage sendMessage = newSendMessage();
        if (bot.isGroupChat()) sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(userSession.getResultInPage());
        InlineKeyboardMarkup markup = createInlinePagingMarkup(userSession.getStart(), userSession.getEnd(), userSession.getPages());
        if (markup != null) {
            sendMessage.setReplyMarkup(markup);
        }
        return sendMessage;
    }

    public static SendMessage createDocumentDescription(OTelegramBot bot, ODocumentTelegramDescription link, Message message, UserSession userSession,
                                                        boolean isAllDescription) {
        SendMessage sendMessage = newSendMessage();
        if (bot.isGroupChat()) sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(link.getDescription());
        if (link.hasNonDisplayableFields()) {
            sendMessage.setReplyMarkup(createInlineDocumentMarkup(bot, link.getLinkInString(), isAllDescription, userSession));
        }
        return sendMessage;
    }

    public static InlineKeyboardMarkup createInlineDocumentMarkup(OTelegramBot bot, String documentLink, boolean isAllDescription, UserSession userSession) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttonList = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        if (isAllDescription) {
            button.setText(MessageKey.SHORT_DOCUMENT_DSCR_BUT.toLocaleString(bot));
            button.setCallbackData(documentLink.substring(0, documentLink.indexOf("_details")));
        } else {
            button.setText(MessageKey.ALL_DOCUMENT_DSCR_BUT.toLocaleString(bot));
            button.setCallbackData(documentLink + "_details");
        }
        buttonList.add(button);
        keyboard.add(buttonList);
        markup.setKeyboard(keyboard);
        return markup;
    }

    public static InlineKeyboardMarkup createInlinePagingMarkup(int start, int end, int size) {
        InlineKeyboardMarkup inlineMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> pageButtons = new ArrayList<>();
        List<InlineKeyboardButton> nextPreviousBut = new ArrayList<>();
        InlineKeyboardButton button;
        if (end - start == 1) {
            return null;
        }
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

    private static ReplyKeyboardMarkup createMenuMarkup(List<String> buttonNames) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
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
