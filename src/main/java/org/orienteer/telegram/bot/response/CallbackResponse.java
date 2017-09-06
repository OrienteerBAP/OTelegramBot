package org.orienteer.telegram.bot.response;

import org.orienteer.telegram.bot.AbstractOTelegramBot;
import org.orienteer.telegram.bot.UserSession;
import org.orienteer.telegram.bot.util.BotState;
import org.orienteer.telegram.bot.util.MessageKey;
import org.orienteer.telegram.bot.util.ODocumentTelegramDescription;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;

/**
 * Class which contains callback response
 */
public class CallbackResponse {

    private final CallbackQuery query;
    private final UserSession userSession;
    private int page;
    private boolean isDocumentDescription;
    private boolean isAllDescription;

    public CallbackResponse(CallbackQuery callbackQuery) {
        query = callbackQuery;
        userSession = AbstractOTelegramBot.getCurrentSession();
    }

    public AnswerCallbackQuery getCallbackAnswer() {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        String data = query.getData();
        answer.setCallbackQueryId(query.getId());
        isDocumentDescription = false;
        if (data.equals(BotState.NEXT_RESULT.getCommand())) {
            answer.setText("" + (userSession.getNextPages() + 1));
            page = userSession.getStart();
        } else if (data.equals(BotState.PREVIOUS_RESULT.getCommand())) {
            answer.setText("" + (userSession.getPreviousPages() + 1));
            page = userSession.getStart();
        } else if (data.startsWith(BotState.GO_TO_CLASS.getCommand()) && data.contains("_details")) {
            isDocumentDescription = true;
            isAllDescription = true;
            answer.setText(MessageKey.ALL_DOCUMENT_DSCR_BUT.toLocaleString());
        } else if (data.startsWith(BotState.GO_TO_CLASS.getCommand()) && data.contains("_")) {
            isDocumentDescription = true;
            isAllDescription = false;
            answer.setText(MessageKey.SHORT_DOCUMENT_DSCR_BUT.toLocaleString());
        } else {
            page = Integer.valueOf(query.getData()) - 1;
            answer.setText(query.getData());
        }
        return answer;
    }

    public EditMessageText getEditMessage() {
        String text;
        InlineKeyboardMarkup markup;
        if (isDocumentDescription) {
            ODocumentTelegramDescription link = new ODocumentTelegramDescription(query.getData(), isAllDescription);
            text = link.getDescription();
            markup = AbstractResponseMessageFactory.createInlineDocumentMarkup(link.getLinkInString(), isAllDescription, userSession);
        } else {
            text = userSession.getResultInPage(page);
            markup = AbstractResponseMessageFactory.createInlinePagingMarkup(userSession.getStart(), userSession.getEnd(), userSession.getPages());
        }

        return getEditMessage(text, markup);
    }

    private EditMessageText getEditMessage(String text, InlineKeyboardMarkup markup) {
        EditMessageText editText = new EditMessageText();
        editText.setMessageId(query.getMessage().getMessageId());
        editText.setChatId(query.getMessage().getChatId().toString());
        editText.enableMarkdown(true);
        editText.setText(text);
        if (markup != null) {
            editText.setReplyMarkup(markup);
        }
        return editText;
    }

    public UserSession getUserSession() {
        return userSession;
    }
}
