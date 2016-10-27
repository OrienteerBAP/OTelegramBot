package org.orienteer.telegram;

import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

/**
 * Created by Vitaliy Gonchar on 27.10.16.
 */
public class OTelegramBot extends TelegramLongPollingBot {

    @Override
    public String getBotToken() {
        return null;
    }

    @Override
    public String getBotUsername() {
        return null;
    }


    @Override
    public void onUpdateReceived(Update update) {

    }


    @Override
    public void onClosing() {

    }
}
