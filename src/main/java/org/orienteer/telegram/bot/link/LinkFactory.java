package org.orienteer.telegram.bot.link;

import org.orienteer.telegram.bot.OTelegramBot;

/**
 * @author Vitaliy Gonchar
 */
public abstract class LinkFactory {

    public static Link getLink(String classLink) {
        ClassLink link = new ClassLink(classLink, OTelegramBot.getCurrentLocale());

        return link;
    }

    public static Link getLink(String documentLink, boolean isDisplayable) {
        DocumentLink link = new DocumentLink(documentLink, isDisplayable, OTelegramBot.getCurrentLocale());

        return link;
    }
}
