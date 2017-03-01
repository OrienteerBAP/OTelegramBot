package org.orienteer.telegram.bot.search;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.record.OTrackedList;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.orienteer.core.util.CommonUtils;
import org.orienteer.telegram.bot.MessageKey;
import org.orienteer.telegram.bot.OTelegramBot;
import org.orienteer.telegram.bot.response.BotState;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.*;

/**
 * @author Vitaliy Gonchar
 */
public class ClassSearch extends Search {

    private final String searchWord;
    private final String className;
    private final List<String> docLinks;
    private int embeddedId = 0;

    public ClassSearch(String searchWord, String className, Locale locale) {
        super(locale);
        this.searchWord = searchWord;
        this.className = className;
        docLinks = new ArrayList<>();
    }

    @Override
    public Result execute() {
        return new DBClosure<Result>() {
            @Override
            protected Result execute(ODatabaseDocument db) {
                List<String> fieldValuesList = new ArrayList<>();
                List<String> docNamesList = new ArrayList<>();
                OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>(queryCache.get(className));
                Iterable<ODocument> oDocuments = (Iterable<ODocument>) (query.getText().contains("?") ? db.query(query, searchWord): db.query(query));

                for (ODocument oDocument : oDocuments) {
                    List<String> result = searchInFieldValues(oDocument);
                    if (result != null) fieldValuesList.addAll(result);
                    //String doc = searchDocument(oDocument);
                  //  if (doc != null) docNamesList.add(doc);
                }
                return getResultOfSearch(fieldValuesList, null, null, docLinks);
            }
        }.execute();
    }

  /*  /**
     * Build string with result of searchAll
     * @param oDocument getResultOfSearch document
     * @return string with result of searchAll

    private String searchDocument(ODocument oDocument) {
        StringBuilder builder = new StringBuilder();
        String docName = OTelegramBot.getDocName(oDocument);
        if (docName == null) return null;
        String documentLink = BotState.GO_TO_CLASS.getCommand() + oDocument.getClassName()
                + "_" + oDocument.getIdentity().getClusterId()
                + "_" + oDocument.getIdentity().getClusterPosition();

        if (isWordInLine(searchWord, docName)) {
            builder.append(docName);
            builder.append(" ");
            builder.append("\n");
            docLinks.add(documentLink);
        }
        return builder.length() > 0 ? builder.toString() : null;
    }
*/
    /**
     * getResultOfSearch similar words in field values
     * @param oDocument document where is getResultOfSearch
     * @return list of strings with result of getResultOfSearch
     */
    private List<String> searchInFieldValues(ODocument oDocument) {
        String searchValue = null;
        List<String> resultList = new ArrayList<>();
        String docName = OTelegramBot.getDocName(oDocument);
        if (docName == null) docName = MessageKey.WITHOUT_NAME.getString(locale);
        String documentLink = BotState.GO_TO_CLASS.getCommand() + oDocument.getClassName()
                + "_" + oDocument.getIdentity().getClusterId()
                + "_" + oDocument.getIdentity().getClusterPosition();
        Iterator<Map.Entry<String, Object>> iterator = oDocument.iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            String name = entry.getKey() != null ? entry.getKey() : MessageKey.WITHOUT_NAME.getString(locale);
            Object value = entry.getValue();
            OType type = OType.getTypeByValue(value);
            if (type != null) {
                searchValue = getFieldValueInString(type, value, oDocument);
                if (isWordInLine(searchWord, searchValue)) {
                    searchValue = name + " : " + searchValue;
                    resultList.add(String.format(MessageKey.HTML_STRONG_TEXT.toString(), docName) + "  (" + searchValue + ")  " + documentLink + "\n");
                    docLinks.add(documentLink);
                    break;
                }
            }
        }
        return resultList.size() > 0 ? resultList : null;
    }

    private String getFieldValueInString(OType type, Object fieldValue, ODocument doc) {
        String fieldValueStr = " ";
        switch (type) {
            case LINKSET:
            case LINKMAP:
            case LINKLIST:
                OTrackedList list = (OTrackedList) fieldValue;
                Iterator iterator = list.iterator();
                fieldValueStr = "\n";
                while (iterator.hasNext()) {
                    Object value = iterator.next();
                    OType oType = OType.getTypeByValue(value);
                    String result = getFieldValueInString(oType, value, doc);
                    fieldValueStr += "  " + result + "\n";
                }
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

                String linkName = OTelegramBot.getDocName(linkDocument);
                fieldValueStr = linkName + " " + BotState.GO_TO_CLASS.getCommand() + linkDocument.getClassName()
                        + "_" + linkDocument.getIdentity().getClusterId()
                        + "_" + linkDocument.getIdentity().getClusterPosition();
                break;
            case EMBEDDEDSET:
            case EMBEDDEDLIST:
            case EMBEDDEDMAP:
                Map<String, Object> localizations = (Map<String, Object>) fieldValue;
                Object localized = CommonUtils.localizeByMap(localizations, true, locale.getLanguage(), Locale.getDefault().getLanguage());
                if (localized != null) fieldValueStr = localized.toString();
                break;
            case EMBEDDED:
                ODocument value = (ODocument) fieldValue;
                String valueName = OTelegramBot.getDocName(value);
                fieldValueStr = valueName + " " + BotState.GO_TO_CLASS.getCommand() + doc.getClassName()
                        + "_" + doc.getIdentity().getClusterId()
                        + "_" + doc.getIdentity().getClusterPosition()
                        + "_" + embeddedId++
                        + MessageKey.EMBEDDED.toString();
                break;
            default:
                fieldValueStr = fieldValue.toString();
                break;
        }
        return fieldValueStr.equals(" ") ? null : fieldValueStr;
    }

    private String getSimilarDocumentValues(ODocument doc) {
        String similarValue = null;
        Iterator<Map.Entry<String, Object>> iterator = doc.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            final Object value = entry.getValue();
            OType type = OType.getTypeByValue(value);
            if (type.isEmbedded()) {
                similarValue = getSimilarDocumentValues((ODocument) value);
                if (similarValue != null) break;
            } else if (type.isLink()) {
                ODocument document = new DBClosure<ODocument>() {
                    @Override
                    protected ODocument execute(ODatabaseDocument db) {
                        return db.getRecord((ORecordId) value);
                    }
                }.execute();
                similarValue = getSimilarDocumentValues(document);
                if (similarValue != null) break;
            } else {
                similarValue = isWordInLine(searchWord, value.toString())?value.toString():null;
                if (similarValue != null) break;
            }
        }
        return similarValue;
    }
}
