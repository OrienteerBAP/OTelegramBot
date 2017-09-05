package org.orienteer.telegram.bot.util;

/**
 * Enum which contains Markdown for create response messages
 */
public enum Markdown {
    BOLD("*%s*");

    private final String markdown;

    Markdown(String markdown) {
        this.markdown = markdown;
    }

    public String toString(String str) {
        return String.format(markdown, str);
    }

}
