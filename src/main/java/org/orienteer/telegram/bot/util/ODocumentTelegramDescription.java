package org.orienteer.telegram.bot.util;

import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.http.util.Args;
import org.orienteer.core.CustomAttribute;
import org.orienteer.telegram.bot.Cache;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.orienteer.telegram.bot.util.OTelegramUtil.*;

/**
 * Implements {@link IOTelegramDescription} for represents {@link ODocument} description in Telegram
 */
public class ODocumentTelegramDescription implements IOTelegramDescription<String> {
    private final String documentLink;
    private final ODocument document;
    private Boolean showAllFields;
    private boolean noDisplayableFields;
    private int embeddedIdCounter = 0;

    /**
     * Constructor
     * @param documentLink {@link String} string which contains Telegram link to {@link ODocument}
     * @param showAllFields if true all fields of document includes to description
     */
    public ODocumentTelegramDescription(String documentLink, final boolean showAllFields) {
        Args.notEmpty(documentLink, "documentLink");
        this.documentLink = documentLink;
        String linkBody = this.documentLink.substring(this.documentLink.indexOf(BotState.GO_TO_CLASS.getCommand()));
        String [] split = linkBody.split("_");
        if (this.documentLink.endsWith("_" + BotState.EMBEDDED.getCommand())) {
            int clusterID = Integer.valueOf(split[1]);
            int recordID = Integer.valueOf(split[2]);
            ODocument owner = OTelegramUtil.getDocumentByRecord(new ORecordId(clusterID, recordID));
            this.document = owner != null ?
                    getEmbeddedDocument(owner, Integer.valueOf(split[3]), BotState.of(split[4]), showAllFields) : null;
        } else {
            int clusterID = Integer.valueOf(split[1]);
            int recordID = Integer.valueOf(split[2]);
            this.document = getDocument(new ORecordId(clusterID, recordID), showAllFields);
        }
    }

    /**
     * Get {@link ODocument} by {@link ORecordId} and set if need to display all document fields
     * @param recordId {@link ORecordId}
     * @param showAllFields true if need to display all document fields
     * @return {@link ODocument}
     */
    private ODocument getDocument(final ORecordId recordId, final boolean showAllFields) {
        return new DBClosure<ODocument>() {
            @Override
            protected ODocument execute(ODatabaseDocument db) {
                ODocument document = db.getRecord(recordId);
                setShowAllFields(document, showAllFields);
                return document;
            }
        }.execute();
    }

    /**
     * Get embedded {@link ODocument} by id and {@link BotState}
     * @param owner {@link ODocument} owner document
     * @param id id of embedded document
     * @param state {@link BotState}
     * @param showAllFields true if need to show all fields of document
     * @return {@link ODocument}
     */
    private ODocument getEmbeddedDocument(final ODocument owner, final int id, final BotState state, final boolean showAllFields) {
        return new DBClosure<ODocument>() {
            @Override
            @SuppressWarnings("unchecked")
            protected ODocument execute(ODatabaseDocument db) {
                ODocument result = null;
                int counter = 0;
                for (String name : owner.fieldNames()) {
                    Object obj = owner.field(name);
                    OType type = OType.getTypeByValue(obj);
                    if (type != null && type.isEmbedded()) {
                        if (state == BotState.EMBEDDED && type == OType.EMBEDDED
                                || state == BotState.EMBEDDED_LIST && type == OType.EMBEDDEDLIST
                                || state == BotState.EMBEDDED_SET && type == OType.EMBEDDEDSET
                                || state == BotState.EMBEDDED_MAP && type == OType.EMBEDDEDMAP) {
                            if (counter == id) {
                                result = (ODocument) obj;
                                break;
                            }
                            counter++;
                        }
                    }
                }
                if (result != null) setShowAllFields(result, showAllFields);
                return result;
            }
        }.execute();
    }

    private void setShowAllFields(ODocument document, boolean showAllFields) {
        OClass oClass = document.getSchemaClass();
        for (String name : document.fieldNames()) {
            OProperty property = oClass.getProperty(name);
            boolean displayable = CustomAttribute.DISPLAYABLE.getValue(property);
            if (!displayable) {
                this.showAllFields = showAllFields;
                noDisplayableFields = true;
            }
        }
        if (this.showAllFields == null) {
            this.showAllFields = true;
        }
    }

    /**
     * @return {@link String} which contains description of document
     */
    @Override
    public String getDescription() {
        return new DBClosure<String>() {
            @Override
            protected String execute(ODatabaseDocument db) {
                StringBuilder sb = new StringBuilder();
                if (!isShowAllFields()) {
                    sb.append(Markdown.BOLD.toString(MessageKey.SHORT_DOCUMENT_DSCR_BUT.toLocaleString()));
                } else sb.append(Markdown.BOLD.toString(MessageKey.ALL_DOCUMENT_DSCR_BUT.toLocaleString()));
                sb.append("\n\n").append(Markdown.BOLD.toString(MessageKey.CLASS.toLocaleString())).append(" ");
                if (Cache.getClassCache().containsKey(document.getClassName())) {
                    sb.append(document.getClassName()).append(" ").append(BotState.GO_TO_CLASS.getCommand());
                }
                sb.append(document.getClassName()).append("\n\n");
                appendDocumentDescription(sb, document);
                return sb.toString();
            }
        }.execute();
    }

    private void appendDocumentDescription(StringBuilder sb, ODocument document) {
        OClass documentClass = document.getSchemaClass();
        List<String> names = Lists.newArrayList(document.fieldNames());
        for (String fieldName : names) {
            Object value = document.field(fieldName);
            OType type = OType.getTypeByValue(value);
            if (type != null && isPropertyDisplayable(documentClass.getProperty(fieldName))) {
                sb.append(Markdown.BOLD.toString(fieldName)).append(": ");
                if (type.isLink()) {
                    appendLinkValue(sb, type, document, value);
                } else if (type.isEmbedded()) {
                    appendEmbeddedValue(sb, type, document, value);
                } else {
                    sb.append(value.toString());
                }
                sb.append("\n");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void appendLinkValue(StringBuilder sb, OType type, ODocument owner, Object value) {
        switch (type) {
            case LINK:
                appendDocumentLink(sb, (ODocument) value);
                break;
            case LINKLIST:
                appendCollection(sb, owner, (Collection<Object>) value, true);
                break;
            case LINKSET:
                appendCollection(sb, owner, (Collection<Object>) value, false);
                break;
            case LINKMAP:
                appendMap(sb, owner, (Map<String, Object>) value);
                break;
        }
    }

    @SuppressWarnings("unchecked")
    private void appendEmbeddedValue(StringBuilder sb, OType type, ODocument owner, Object value) {
        switch (type) {
            case EMBEDDED:
                appendEmbeddedLink(sb, embeddedIdCounter++, owner, (ODocument) value);
                break;
            case EMBEDDEDLIST:
                appendCollection(sb, owner, (Collection<Object>) value, true);
                break;
            case EMBEDDEDSET:
                appendCollection(sb, owner, (Collection<Object>) value, false);
                break;
            case EMBEDDEDMAP:
                appendMap(sb, owner, (Map<String, Object>) value);
                break;
        }
    }

    @SuppressWarnings("unchecked")
    private void appendCollection(StringBuilder sb, ODocument owner, Collection<Object> collection, boolean list) {
        int counter = 0;
        for (Object obj : collection) {
            OType type = OType.getTypeByValue(obj);
            if (type != null && (type.isLink() || type.isEmbedded())) {
                sb.append("\n").append(counter + 1).append(") ");
                if (type.isLink()) {
                    appendDocumentLink(sb, (ODocument) obj);
                } else {
                    if (list) {
                        appendEmbeddedListLink(sb, embeddedIdCounter++, owner, (ODocument) obj);
                    } else appendEmbeddedSetLink(sb, embeddedIdCounter++, owner, (ODocument) obj);
                }
                counter++;
            } else if (obj != null) {
                sb.append("\n").append(counter + 1).append(") ").append(obj.toString());
                counter++;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void appendMap(StringBuilder sb, ODocument owner, Map<String, Object> map) {
        for (String key : map.keySet()) {
            Object value = map.get(key);
            OType type = OType.getTypeByValue(value);
            if (type != null && (type.isLink() || type.isEmbedded())) {
                sb.append(key).append(":\n");
                if (type.isLink()) {
                    appendDocumentLink(sb, (ODocument) value);
                } else {
                    appendEmbeddedMapLink(sb, embeddedIdCounter++, owner, (ODocument) value);
                }
            } else if (value != null) {
                sb.append(key).append(": ").append(value);
            }
        }
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

    @SuppressWarnings("unchecked")
    private boolean isPropertyDisplayable(OProperty property) {
        return property != null && ((Boolean) CustomAttribute.DISPLAYABLE.getValue(property) || isShowAllFields());
    }
}
