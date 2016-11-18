package org.orienteer.telegram.bot.link;

import org.orienteer.telegram.bot.BotMessage;

/**
 * @author Vitaliy Gonchar
 */
public abstract class LinkFactory {

    public static Link getLink(String classLink, BotMessage botMessage) {
        ClassLink link = new ClassLink(classLink, botMessage);

        return link;
    }

    public static Link getLink(String documentLink, boolean isDisplayable, BotMessage botMessage) {
        DocumentLink link = new DocumentLink(documentLink, isDisplayable, botMessage);

        return link;
    }
}
