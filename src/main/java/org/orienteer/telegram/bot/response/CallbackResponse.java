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
    private int page;

    public CallbackResponse(CallbackQuery callbackQuery) {
        query = callbackQuery;
        userSession = OTelegramBot.getCurrentSession();
    }

    public AnswerCallbackQuery getCallbackAnswer() {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        String data = query.getData();
        answer.setCallbackQueryId(query.getId());
        if (data.equals(BotState.NEXT_RESULT.getCommand())) {
            answer.setText("" + (userSession.getNextPages() + 1));
            page = userSession.getStart();
        } else if (data.equals(BotState.PREVIOUS_RESULT.getCommand())) {
            answer.setText("" + (userSession.getPreviousPages() + 1));
            page = userSession.getStart();
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
        EditMessageText editText = new EditMessageText();
        editText.setMessageId(query.getMessage().getMessageId());
        editText.setChatId(query.getMessage().getChatId().toString());
        editText.setText(userSession.getResultInPage(page));
        editText.enableHtml(true);
        editText.setReplyMarkup(ResponseMessage.getInlineMarkup(userSession.getStart(), userSession.getEnd(), userSession.getPages()));
        return editText;
    }

    public UserSession getUserSession() {
        return userSession;
    }
}
