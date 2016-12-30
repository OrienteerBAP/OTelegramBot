package org.orienteer.telegram.bot.response;

import org.orienteer.telegram.bot.MessageKey;
import org.orienteer.telegram.bot.OTelegramBot;
import org.orienteer.telegram.bot.UserSession;
import org.orienteer.telegram.bot.link.Link;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;

/**
 * @author Vitaliy Gonchar
 */
public class CallbackResponse {

    private final CallbackQuery query;
    private final UserSession userSession;
    private int page;
    private boolean isDocumentDescription;
    private boolean isAllDescription;

    public CallbackResponse(CallbackQuery callbackQuery) {
        query = callbackQuery;
        userSession = OTelegramBot.getCurrentSession();
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
            answer.setText(MessageKey.DOCUMENT_DETAILS_MSG.getString(userSession.getLocale()));
        } else if (data.startsWith(BotState.GO_TO_CLASS.getCommand()) && data.contains("_")) {
            isDocumentDescription = true;
            isAllDescription = false;
            answer.setText(MessageKey.SHORT_DOCUMENT_DESCRIPTION_MSG.getString(userSession.getLocale()));
        } else {
            page = Integer.valueOf(query.getData()) - 1;
            answer.setText(query.getData());
        }
        return answer;
    }

    public SendMessage getResponse() {
        return ResponseMessage.getPagingMenu(query.getMessage(), userSession);
    }

    public EditMessageText getEditMessage() {
        String text;
        InlineKeyboardMarkup markup;
        if (isDocumentDescription) {
            Link link = Link.getLink(query.getData(), isAllDescription);
            text = link.goTo();
            markup = ResponseMessage.getInlineDocumentMarkup(link.getLinkInString(), isAllDescription, userSession);
        } else {
            text = userSession.getResultInPage(page);
            markup = ResponseMessage.getInlinePagingMarkup(userSession.getStart(), userSession.getEnd(), userSession.getPages());
        }

        return getEditMessage(text, markup);
    }

    private EditMessageText getEditMessage(String text, InlineKeyboardMarkup markup) {
        EditMessageText editText = new EditMessageText();
        editText.setMessageId(query.getMessage().getMessageId());
        editText.setChatId(query.getMessage().getChatId().toString());
        editText.enableHtml(true);
        editText.setText(text);
        editText.setReplyMarkup(markup);
        return editText;
    }

    public UserSession getUserSession() {
        return userSession;
    }
}
