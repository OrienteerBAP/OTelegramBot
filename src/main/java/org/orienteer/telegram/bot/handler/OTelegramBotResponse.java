package org.orienteer.telegram.bot.handler;

import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;

/**
 * @author Vitaliy Gonchar
 */
class OTelegramBotResponse {
    private final SendMessage sendMessage;
    private final AnswerCallbackQuery answerCallbackQuery;
    private final EditMessageText editMessageText;

    OTelegramBotResponse(SendMessage sendMessage) {
        this.sendMessage = sendMessage;
        answerCallbackQuery = null;
        editMessageText = null;
    }

    OTelegramBotResponse(AnswerCallbackQuery answerCallbackQuery, EditMessageText editMessageText) {
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
