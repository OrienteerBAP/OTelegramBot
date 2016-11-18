package org.orienteer.telegram.bot.link;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.exception.ORecordNotFoundException;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.core.CustomAttribute;
import org.orienteer.telegram.bot.BotMessage;
import org.orienteer.telegram.bot.BotState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Vitaliy Gonchar
 */
public class DocumentLink implements Link {
    private final String documentLink;
    private final BotMessage botMessage;
    private final ORecordId oRecordId;
    private final boolean isDisplayable;

    private static final Logger LOG = LoggerFactory.getLogger(DocumentLink.class);

    public DocumentLink(String documentLink, boolean isDisplayable, BotMessage botMessage) {
        this.documentLink = documentLink;
        this.isDisplayable = isDisplayable;
        this.botMessage = botMessage;
        String [] split = documentLink.substring(BotState.GO_TO_CLASS.getCommand().length()).split("_");
        int clusterID = Integer.valueOf(split[1]);
        long recordID = Long.valueOf(split[2]);
        oRecordId = new ORecordId(clusterID, recordID);
    }

    @Override
    public String goTo() {
        return  (String) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                StringBuilder builder = new StringBuilder();
                StringBuilder resultBuilder;
                ODocument oDocument;
                try {
                    oDocument = oDatabaseDocument.getRecord(oRecordId);
                    builder.append(oDocument.getClassName());
                    builder.append(" " + BotState.GO_TO_CLASS.getCommand());
                    builder.append(oDocument.getClassName());
                    builder.append("\n\n");
                    String[] fieldNames = oDocument.fieldNames();
                    List<String> resultList = new ArrayList<>();
                    OClass oClass = oDocument.getSchemaClass();
                    CustomAttribute displayable = CustomAttribute.DISPLAYABLE;
                    boolean isWihoutDetails = false;
                    for (String fieldName : fieldNames) {
                        if (!isDisplayable) {
                            OProperty property = oClass.getProperty(fieldName);
                            if (displayable.getValue(property)) {
                                resultList.add(String.format(botMessage.HTML_STRONG_TEXT, fieldName) + ":  "
                                        + oDocument.field(fieldName, OType.STRING) + "\n");
                            } else isWihoutDetails = true;
                        } else  resultList.add(String.format(botMessage.HTML_STRONG_TEXT, fieldName) + ":  "
                                + oDocument.field(fieldName, OType.STRING) + "\n");
                    }
                    Collections.sort(resultList);
                    for (String str : resultList) {
                        builder.append(str);
                    }
                    resultBuilder = new StringBuilder(String.format(
                            botMessage.HTML_STRONG_TEXT, botMessage.DOCUMENT_DETAILS_MSG) + "\n\n"
                            + String.format(botMessage.HTML_STRONG_TEXT, "Class:  "));
                    if (isWihoutDetails) {
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
}
