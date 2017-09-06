package org.orienteer.telegram.bot.util;

/**
 * Enum which contains Markdown for create response messages
 */
public enum Markdown {
    BOLD("*%s*"),
    ITALIC("_%s_"),
    INLINE_FIXED_WIDTH_CODE("`%s`"),
    BLOCK_LANGUAGE("```%s```");

    private final String markdown;

    Markdown(String markdown) {
        this.markdown = markdown;
    }

    public String toString(String str) {
        return String.format(markdown, str);
    }

    public static boolean containsMarkdown(String str) {
        for (Markdown m : values()) {
            if (str.contains(m.getStartMarkdownStr()))
                return true;
        }
        return false;
    }

    public String getStartMarkdownStr() {
        return this.markdown.substring(0, this.markdown.indexOf("%s"));
    }

    public char getFirstChar() {
        return this.markdown.charAt(0);
    }
}
