package org.orienteer.telegram.bot.response;

import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;

/**
 * Class which contains bot response
 */
public class OTelegramBotResponse {
    private final SendMessage sendMessage;
    private final AnswerCallbackQuery answerCallbackQuery;
    private final EditMessageText editMessageText;

    public OTelegramBotResponse(SendMessage sendMessage) {
        this.sendMessage = sendMessage;
        answerCallbackQuery = null;
        editMessageText = null;
    }

    public OTelegramBotResponse(AnswerCallbackQuery answerCallbackQuery, EditMessageText editMessageText) {
        this.sendMessage = null;
        this.answerCallbackQuery = answerCallbackQuery;
        this.editMessageText = editMessageText;
    }

    public SendMessage getSendMessage() {
        return sendMessage;
    }

    public AnswerCallbackQuery getAnswerCallbackQuery() {
        return answerCallbackQuery;
    }

    public EditMessageText getEditMessageText() {
        return editMessageText;
    }
}
