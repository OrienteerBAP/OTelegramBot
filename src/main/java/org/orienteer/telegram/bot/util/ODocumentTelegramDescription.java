package org.orienteer.telegram.bot.util;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.record.OTrackedList;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.core.CustomAttribute;
import org.orienteer.core.util.CommonUtils;
import org.orienteer.telegram.bot.AbstractOTelegramBot;
import org.orienteer.telegram.bot.Cache;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.*;

/**
 * Represents {@link ODocument} description in Telegram
 */
public class ODocumentTelegramDescription {
    private final String documentLink;
    private final ODocument document;
    private Boolean showAllFields;
    private boolean noDisplayableFields;
    private final long embeddedID;
    private long embeddedIdCounter = 0;

    public ODocumentTelegramDescription(String documentLink, final boolean showAllFields) {
        this.documentLink = documentLink.contains("@") ? documentLink.substring(0, documentLink.indexOf("@")) : documentLink;
        String [] split = this.documentLink.substring(BotState.GO_TO_CLASS.getCommand().length()).split("_");
        final int clusterID = Integer.valueOf(split[1]);
        final long recordID = Long.valueOf(split[2]);
        this.document = new DBClosure<ODocument>() {
            @Override
            protected ODocument execute(ODatabaseDocument db) {
                ODocument document = db.getRecord(new ORecordId(clusterID, recordID));
                OClass oClass = document.getSchemaClass();
                for (String name : document.fieldNames()) {
                    OProperty property = oClass.getProperty(name);
                    boolean displayable = CustomAttribute.DISPLAYABLE.getValue(property);
                    if (!displayable) {
                        ODocumentTelegramDescription.this.showAllFields = showAllFields;
                        ODocumentTelegramDescription.this.noDisplayableFields = true;
                    }
                }
                if (ODocumentTelegramDescription.this.showAllFields == null) {
                    ODocumentTelegramDescription.this.showAllFields = true;
                }
                return document;
            }
        }.execute();
        embeddedID = isDocumentEmbedded()?Long.valueOf(split[3]):-1;
    }

    public String getDescription() {
        return new DBClosure<String>() {
            @Override
            protected String execute(ODatabaseDocument db) {
                StringBuilder builder = new StringBuilder();

                if (!isShowAllFields()) {
                    builder.append(Markdown.BOLD.toString(MessageKey.SHORT_DOCUMENT_DESCRIPTION_MSG.getString()));
                } else builder.append(Markdown.BOLD.toString(MessageKey.DOCUMENT_DETAILS_MSG.getString()));
                builder.append("\n\n")
                        .append(Markdown.BOLD.toString(MessageKey.CLASS.getString()))
                        .append(" ");
                if (Cache.getClassCache().containsKey(document.getClassName())) {
                    builder.append(document.getClassName())
                            .append(" ")
                            .append(BotState.GO_TO_CLASS.getCommand());
                }
                builder.append(document.getClassName()).append("\n\n");
                List<String> result = isDocumentEmbedded() ? buildEmbeddedResultList(document) : buildResultList(document);
                if (result != null && !result.isEmpty()) {
                    for (String str : result) {
                        builder.append(str);
                    }
                }
                return builder.toString();
            }
        }.execute();
    }

    private List<String> buildResultList(ODocument doc) {
        List<String> result = new ArrayList<>();
        CustomAttribute displayable = CustomAttribute.DISPLAYABLE;
        Iterator<Map.Entry<String, Object>> iterator = doc.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> field = iterator.next();
            String fieldName = field.getKey();
            Object fieldValue = field.getValue();
            OType type = OType.getTypeByValue(fieldValue);
            if (type != null) {
                String fieldValueStr;
                if (!showAllFields) {
                    fieldValueStr = buildValueString(type, fieldValue, doc);
                    if (fieldValueStr != null) {
                        OProperty property = doc.getSchemaClass().getProperty(fieldName);
                        if (displayable.getValue(property)) {
                            result.add(Markdown.BOLD.toString(fieldName) + ": "
                                    + fieldValueStr + "\n");
                        }
                    }
                } else {
                    fieldValueStr = buildValueString(type, fieldValue, doc);
                    if (fieldValueStr != null) result.add(Markdown.BOLD.toString(fieldName) + ": "
                            + fieldValueStr + "\n");
                }
            }
        }
        Collections.sort(result);
        return result;
    }

    private String buildValueString(OType type, Object fieldValue, ODocument doc) {
        String fieldValueStr = null;
        switch (type) {
            case LINKSET:
            case LINKMAP:
            case LINKLIST:
                OTrackedList list = (OTrackedList) fieldValue;
                Iterator iterator = list.iterator();
                StringBuilder sb = new StringBuilder("\n");
                while (iterator.hasNext()) {
                    Object value = iterator.next();
                    OType oType = OType.getTypeByValue(value);
                    String result = buildValueString(oType, value, doc);
                    sb.append("  ").append(result).append("\n");
                }
                fieldValueStr = sb.toString();
                break;
            case LINK:
                ODocument linkDocument = null;
                if (fieldValue instanceof ORecordId) {
                    final ORecordId linkID = (ORecordId) fieldValue;
                    linkDocument = new DBClosure<ODocument>() {
                        @Override
                        protected ODocument execute(ODatabaseDocument db) {
                            return db.getRecord(linkID);
                        }
                    }.execute();
                } else if (fieldValue instanceof ODocument) {
                    linkDocument = (ODocument) fieldValue;
                }

                String linkName = AbstractOTelegramBot.getDocName(linkDocument);
                fieldValueStr = linkName + " " + BotState.GO_TO_CLASS.getCommand() + linkDocument.getClassName()
                        + "\\_" + linkDocument.getIdentity().getClusterId()
                        + "\\_" + linkDocument.getIdentity().getClusterPosition();
                break;
            case EMBEDDEDSET:
            case EMBEDDEDLIST:
            case EMBEDDEDMAP:
                if (fieldValue instanceof Map) {
                    Map<String, Object> localizations = (Map<String, Object>) fieldValue;
                    Object localized = CommonUtils.localizeByMap(localizations, true,
                            AbstractOTelegramBot.getCurrentLocale().getLanguage(), Locale.getDefault().getLanguage());
                    if (localized != null) fieldValueStr = localized.toString();
                }
                break;
            case EMBEDDED:
                ODocument value = (ODocument) fieldValue;
                String valueName = AbstractOTelegramBot.getDocName(value);
                fieldValueStr = valueName + " " + BotState.GO_TO_CLASS.getCommand() + doc.getClassName()
                        + "\\_" + doc.getIdentity().getClusterId()
                        + "\\_" + doc.getIdentity().getClusterPosition()
                        + "\\_" + embeddedIdCounter++
                        + BotState.EMBEDDED.getCommand();
                break;
            default:
                fieldValueStr = fieldValue.toString();
                break;
        }
        return fieldValueStr;
    }

    private List<String> buildEmbeddedResultList(ODocument doc) {
        Iterator<Map.Entry<String, Object>> iterator = doc.iterator();
        ODocument document = null;
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            Object value = entry.getValue();
            OType type = OType.getTypeByValue(value);
            if (type != null && type.isEmbedded()) {
                if (embeddedID == embeddedIdCounter) {
                    document = (ODocument) value;
                    break;
                } else embeddedIdCounter++;
            }
        }
        return document != null ? buildResultList(document) : null;
    }

    private boolean isDocumentEmbedded() {
        return document.isEmbedded();
    }

    public String getLinkInString() {
        return documentLink;
    }

    public boolean isShowAllFields() {
        return showAllFields;
    }

    public boolean hasNonDisplayableFields() {
        return noDisplayableFields;
    }
}
