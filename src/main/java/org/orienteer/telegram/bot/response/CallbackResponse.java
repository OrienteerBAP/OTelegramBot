package org.orienteer.telegram.bot.response;

import org.orienteer.telegram.bot.OTelegramBot;
import org.orienteer.telegram.bot.UserSession;
import org.orienteer.telegram.bot.link.Link;
import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;

/**
 * @author Vitaliy Gonchar
 */
public class CallbackResponse {

    private final CallbackQuery query;
    private final UserSession userSession;
    private boolean isDocument = false;
    private int numberOfLink;

    public CallbackResponse(CallbackQuery callbackQuery) {
        query = callbackQuery;
        userSession = OTelegramBot.getCurrentSession();
    }

    public AnswerCallbackQuery getCallbackAnswer() {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        String data = query.getData();
        answer.setCallbackQueryId(query.getId());
        if (data.equals(BotState.NEXT_RESULT.getCommand())) {
            answer.setText("" + (userSession.getNextPage() + 1));
        } else if (data.equals(BotState.PREVIOUS_RESULT.getCommand())) {
            answer.setText("" + (userSession.getPreviousPage() + 1));
        } else {
            numberOfLink = Integer.valueOf(query.getData()) - 1;
            isDocument = true;
            answer = null;
        }
        return answer;
    }

    public SendMessage getResponse() {
        return ResponseMessage.getPagingMenu(query.getMessage(), userSession);
    }

    public EditMessageText getEditMessage() {
        EditMessageText editText = new EditMessageText();
        editText.setMessageId(query.getMessage().getMessageId());
        editText.setChatId(query.getMessage().getChatId().toString());
        editText.setText(userSession.getResultInPage());
        editText.enableHtml(true);
        editText.setReplyMarkup(ResponseMessage.getInlineMarkup(userSession.getStart(), userSession.getEnd(), userSession.getResultSize()));
        return editText;
    }

    public SendMessage getDataResponse() {
        return ResponseMessage.getTextMessage(query.getMessage(), Link.getLink(userSession.getLink(numberOfLink), false).goTo());
    }

    public boolean isDocument() {
        return isDocument;
    }

    public UserSession getUserSession() {
        return userSession;
    }
}
