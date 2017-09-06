package org.orienteer.telegram.bot.util;

import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.telegram.bot.AbstractOTelegramBot;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for {@link org.orienteer.telegram.module.OTelegramModule}
 */
public abstract class OTelegramUtil {

    /**
     * Get {@link ODocument} by given {@link ORecordId}
     * @param recordId {@link ORecordId}
     * @return {@link ODocument} or null if no document with given recordId
     */
    public static ODocument getDocumentByRecord(final ORecordId recordId) {
        return new DBClosure<ODocument>() {
            @Override
            protected ODocument execute(ODatabaseDocument db) {
                return db.getRecord(recordId);
            }
        }.execute();
    }

    /**
     * Get name of document for Telegram message response
     * @param document {@link ODocument} which name will be create
     * @return {@link String} name of document
     */
    @SuppressWarnings("unchecked")
    public static String getDocumentName(final ODocument document) {
        return new DBClosure<String>() {
            @Override
            protected String execute(ODatabaseDocument oDatabaseDocument) {
                Locale locale = AbstractOTelegramBot.getCurrentLocale();
                OProperty nameProp = OrienteerWebApplication.lookupApplication().getOClassIntrospector().getNameProperty(document.getSchemaClass());
                if (nameProp == null) return MessageKey.WITHOUT_NAME.toLocaleString();

                OType type = nameProp.getType();
                Object value = document.field(nameProp.getName());
                if (value != null) {
                    switch (type) {
                        case DATE:
                            return OrienteerWebApplication.DATE_CONVERTER.convertToString((Date) value, locale);
                        case DATETIME:
                            return OrienteerWebApplication.DATE_TIME_CONVERTER.convertToString((Date) value, locale);
                        case LINK:
                            return getDocumentName((ODocument) value);
                        default:
                            return value.toString();
                    }
                } else return MessageKey.WITHOUT_NAME.toLocaleString();
            }
        }.execute();
    }

    /**
     * Makes string markdown non markdown.
     * Example:
     * input: hello_world_ (word 'world' will be display as italic)
     * output: hello\\_world\\_ (word 'world' will be display as default word)
     * @param str - {@link String} which contains some Markdown which is supports by Telegram
     * @return {@link String} with disabled Markdown
     */
    public static String makeStringNonMarkdown(String str) {
        List<Character> handledChars = Lists.newArrayList();
        for (Markdown markdown : Markdown.values()) {
            Character sym = markdown.getFirstChar();
            if (!handledChars.contains(sym) && str.contains(sym.toString())) {
                str = str.replaceAll(String.format("\\%s", sym.toString()), String.format("\\\\%s", sym.toString()));
                handledChars.add(sym);
            }
        }
        return str;
    }

    public static void appendDocumentLink(StringBuilder sb, ODocument link) {
        appendDocumentLink(sb, getDocumentName(link), link);
    }

    public static void appendDocumentLink(StringBuilder sb, String name, ODocument link) {
        sb.append(name).append(" ")
                .append(BotState.GO_TO_CLASS.getCommand()).append(link.getClassName())
                .append("\\_").append(link.getIdentity().getClusterId()).append("\\_")
                .append(link.getIdentity().getClusterPosition());
    }

    public static void appendEmbeddedLink(StringBuilder sb, int id, ODocument ownerDoc, ODocument embeddedDoc) {
        appendEmbeddedLink(sb, getDocumentName(embeddedDoc), id, ownerDoc, embeddedDoc);
    }

    public static void appendEmbeddedLink(StringBuilder sb, String name, int id, ODocument ownerDoc, ODocument embeddedDoc) {
        appendEmbeddedLink(sb, name, id, BotState.EMBEDDED, ownerDoc, embeddedDoc);
    }

    public static void appendEmbeddedListLink(StringBuilder sb, int id, ODocument ownerDoc, ODocument embeddedDoc) {
        appendEmbeddedListLink(sb, getDocumentName(embeddedDoc), id, ownerDoc, embeddedDoc);
    }

    public static void appendEmbeddedListLink(StringBuilder sb, String name, int id, ODocument ownerDoc, ODocument embeddedDoc) {
        appendEmbeddedLink(sb, name, id, BotState.EMBEDDED_LIST, ownerDoc, embeddedDoc);
    }

    public static void appendEmbeddedSetLink(StringBuilder sb, int id, ODocument ownerDoc, ODocument embeddedDoc) {
        appendEmbeddedSetLink(sb, getDocumentName(embeddedDoc), id, ownerDoc, embeddedDoc);
    }

    public static void appendEmbeddedSetLink(StringBuilder sb, String name, int id, ODocument ownerDoc, ODocument embeddedDoc) {
        appendEmbeddedLink(sb, name, id, BotState.EMBEDDED_SET, ownerDoc, embeddedDoc);
    }

    public static void appendEmbeddedMapLink(StringBuilder sb, int id, ODocument ownerDoc, ODocument embeddedDoc) {
        appendEmbeddedMapLink(sb, getDocumentName(embeddedDoc), id, ownerDoc, embeddedDoc);
    }

    public static void appendEmbeddedMapLink(StringBuilder sb, String name, int id, ODocument ownerDoc, ODocument embeddedDoc) {
        appendEmbeddedLink(sb, name, id, BotState.EMBEDDED_MAP, ownerDoc, embeddedDoc);
    }

    private static void appendEmbeddedLink(StringBuilder sb, String name, int id, BotState state, ODocument ownerDoc, ODocument embeddedDoc) {
        sb.append(name).append(" ").append(BotState.GO_TO_CLASS.getCommand())
                .append(embeddedDoc.getClassName())
                .append("\\_").append(ownerDoc.getIdentity().getClusterId())
                .append("\\_").append(ownerDoc.getIdentity().getClusterPosition())
                .append("\\_").append(id)
                .append("\\_").append(state.getCommand());
    }
}
