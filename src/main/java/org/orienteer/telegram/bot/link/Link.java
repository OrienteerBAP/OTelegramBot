package org.orienteer.telegram.bot.link;

import org.orienteer.telegram.bot.OTelegramBot;


/**
 * @author Vitaliy Gonchar
 */
public abstract class Link {
    public abstract String goTo();

    public static Link getLink(String classLink) {
        return new ClassLink(classLink, OTelegramBot.getCurrentLocale());
    }

    public static Link getLink(String documentLink, boolean isDisplayable) {
        return new DocumentLink(documentLink, isDisplayable, OTelegramBot.getCurrentLocale());
    }
}
