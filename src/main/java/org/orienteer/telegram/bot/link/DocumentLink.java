package org.orienteer.telegram.bot.link;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.exception.ORecordNotFoundException;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.core.CustomAttribute;
import org.orienteer.telegram.bot.BotMessage;
import org.orienteer.telegram.bot.BotState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.*;

/**
 * @author Vitaliy Gonchar
 */
public class DocumentLink implements Link {
    private final String documentLink;
    private final BotMessage botMessage;
    private final ORecordId oRecordId;
    private final boolean isDisplayable;
    private boolean isWithoutDetails;
    private static final Logger LOG = LoggerFactory.getLogger(DocumentLink.class);

    private final long embeddedID;
    private final boolean isEmbeddedLink;

    private long embeddedIdCounter = 0;

    public DocumentLink(String documentLink, boolean isDisplayable, BotMessage botMessage) {
        this.documentLink = documentLink;
        this.isDisplayable = isDisplayable;
        this.botMessage = botMessage;
        String [] split = documentLink.substring(BotState.GO_TO_CLASS.getCommand().length()).split("_");
        int clusterID = Integer.valueOf(split[1]);
        long recordID = Long.valueOf(split[2]);
        oRecordId = new ORecordId(clusterID, recordID);
        isEmbeddedLink = documentLink.contains(botMessage.EMBEDDED);
        embeddedID = isEmbeddedLink?Long.valueOf(split[3]):-1;
    }

    @Override
    public String goTo() {
        return  (String) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument db) {
                StringBuilder builder = new StringBuilder();
                StringBuilder resultBuilder;
                ODocument oDocument;
                try {
                    oDocument = db.getRecord(oRecordId);
                    builder.append(oDocument.getClassName());
                    builder.append(" " + BotState.GO_TO_CLASS.getCommand());
                    builder.append(oDocument.getClassName());
                    builder.append("\n\n");
                    List<String> result = isEmbeddedLink ? buildEmbeddedResultList(oDocument) : buildResultList(oDocument);
                    if (result == null) throw new ORecordNotFoundException("Not found");
                    for (String str : result) {
                        builder.append(str);
                    }
                    resultBuilder = new StringBuilder(String.format(
                            botMessage.HTML_STRONG_TEXT, botMessage.DOCUMENT_DETAILS_MSG) + "\n\n"
                            + String.format(botMessage.HTML_STRONG_TEXT, "Class:  "));
                    if (isWithoutDetails) {
                        resultBuilder = new StringBuilder(String.format(
                                botMessage.HTML_STRONG_TEXT, botMessage.SHORT_DOCUMENT_DESCRIPTION_MSG) + "\n\n"
                                + String.format(botMessage.HTML_STRONG_TEXT, "Class:  "));
                        builder.append("\n" + botMessage.DOCUMENT_DETAILS_MSG + documentLink + "_details");
                    }
                    resultBuilder.append(builder.toString());
                } catch (ORecordNotFoundException ex) {
                    LOG.warn("Record: " + oRecordId + " was not found.");
                    if (LOG.isDebugEnabled()) ex.printStackTrace();
                    resultBuilder = new StringBuilder(
                            String.format(botMessage.HTML_STRONG_TEXT, botMessage.FAILED_DOCUMENT_BY_RID));
                }
                return resultBuilder.toString();
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
                String fieldValueStr = buildValueString(type, fieldValue, doc);
                if (!isDisplayable) {
                    OProperty property = doc.getSchemaClass().getProperty(fieldName);
                    if (displayable.getValue(property)) {
                        result.add(String.format(botMessage.HTML_STRONG_TEXT, fieldName) + ":  "
                                + fieldValueStr + "\n");
                    } else isWithoutDetails = true;
                } else result.add(String.format(botMessage.HTML_STRONG_TEXT, fieldName) + ":  "
                        + fieldValueStr + "\n");
            }
        }
        Collections.sort(result);
        return result;
    }

    private String buildValueString(OType type, Object fieldValue, ODocument doc) {
        String fieldValueStr;
        if (type.isLink()) {
            final ORecordId linkID = (ORecordId) fieldValue;
            ODocument linkDocument = (ODocument) new DBClosure() {
                @Override
                protected Object execute(ODatabaseDocument db) {
                    return db.getRecord(linkID);
                }
            }.execute();
            String linkName = linkDocument.field("name")!=null?(String)linkDocument.field("name"):"without name";
            fieldValueStr = linkName + "  " + BotState.GO_TO_CLASS.getCommand() + linkDocument.getClassName()
                    + "_" + linkDocument.getIdentity().getClusterId()
                    + "_" + linkDocument.getIdentity().getClusterPosition();
        } else if (type.isEmbedded()) {
            ODocument value = (ODocument) fieldValue;
            String valueName = value.field("name")!=null?(String) value.field("name"):"without name";
            fieldValueStr = valueName + "  " + BotState.GO_TO_CLASS.getCommand() + doc.getClassName()
                    + "_" + doc.getIdentity().getClusterId()
                    + "_" + doc.getIdentity().getClusterPosition()
                    + "_" + embeddedIdCounter++
                    + botMessage.EMBEDDED;
        } else if (type.isMultiValue()) {
            fieldValueStr = fieldValue.getClass().toString();
        } else fieldValueStr = fieldValue.toString();

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
}
