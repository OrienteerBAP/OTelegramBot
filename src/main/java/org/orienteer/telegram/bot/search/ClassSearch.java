package org.orienteer.telegram.bot.search;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.orienteer.telegram.bot.BotMessage;
import org.orienteer.telegram.bot.BotState;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Vitaliy Gonchar
 */
public class ClassSearch extends Search {

    private final String searchWord;
    private final String className;

    public ClassSearch(String searchWord, String className, BotMessage botMessage) {
        super(botMessage);
        this.searchWord = searchWord;
        this.className = className;
    }

    @Override
    public ArrayList<String> execute() {
        return (ArrayList<String>) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument db) {
                ArrayList<String> fieldValuesList = new ArrayList<>();
                ArrayList<String> docNamesList = new ArrayList<>();
                OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>(QUERY_CACHE.get(className));
                Iterable<ODocument> oDocuments = (Iterable<ODocument>) (query.getText().contains("?") ? db.query(query, searchWord): db.query(query));

                for (ODocument oDocument : oDocuments) {
                    ArrayList<String> result = searchInFieldValues(oDocument);
                    if (result != null) fieldValuesList.addAll(result);
                    String doc = searchDocument(oDocument);
                    if (doc != null) docNamesList.add(doc);
                }
                return getResultListOfSearch(fieldValuesList, docNamesList, null);
            }
        }.execute();
    }

    /**
     * Build string with result of searchAll
     * @param oDocument getResultOfSearch document
     * @return string with result of searchAll
     */
    private String searchDocument(ODocument oDocument) {
        StringBuilder builder = new StringBuilder();
        String docName = oDocument.field("name", OType.STRING);
        if (docName == null) return null;
        String documentLink = BotState.GO_TO_CLASS.getCommand() + oDocument.getClassName()
                + "_" + oDocument.getIdentity().getClusterId()
                + "_" + oDocument.getIdentity().getClusterPosition()
                + " : " + docName;
        if (isWordInLine(searchWord, docName)) {
            builder.append("• ");
            builder.append(docName);
            builder.append(" ");
            builder.append(documentLink);
            builder.append("\n");
        }
        return builder.length() > 0 ? builder.toString() : null;
    }

    /**
     * getResultOfSearch similar words in field values
     * @param oDocument document where is getResultOfSearch
     * @return list of strings with result of getResultOfSearch
     */
    private ArrayList<String> searchInFieldValues(ODocument oDocument) {
        String searchValue = null;
        ArrayList<String> resultList = new ArrayList<>();
        String docName = oDocument.field("name", OType.STRING);
        if (docName == null) docName = botMessage.WITHOUT_NAME;
        String documentLink = BotState.GO_TO_CLASS.getCommand() + oDocument.getClassName()
                + "_" + oDocument.getIdentity().getClusterId()
                + "_" + oDocument.getIdentity().getClusterPosition()
                + " : " + docName;
        Iterator<Map.Entry<String, Object>> iterator = oDocument.iterator();
        long embeddedId = 0;
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            String name = entry.getKey() != null ? entry.getKey() : botMessage.WITHOUT_NAME;
            Object value = entry.getValue();
            OType type = OType.getTypeByValue(value);
            if (type != null) {
                if (type.isEmbedded()) {
                    ODocument eDoc = (ODocument) value;
                    String similarValue = getSimilarDocumentValues(eDoc);
                    if (similarValue == null) continue;
                    String valueName = eDoc.field("name") != null ? (String) eDoc.field("name") : botMessage.WITHOUT_NAME;
                    String embeddedLink = BotState.GO_TO_CLASS.getCommand() + oDocument.getClassName()
                            + "_" + oDocument.getIdentity().getClusterId()
                            + "_" + oDocument.getIdentity().getClusterPosition()
                            + "_" + embeddedId++
                            + botMessage.EMBEDDED
                            + " : " + valueName;
                    searchValue = String.format(botMessage.HTML_STRONG_TEXT, "• " + name + " : ") + similarValue + " " + embeddedLink + "\n";
                } else if (type.isLink()) {
                    final ORecordId linkID = (ORecordId) value;
                    ODocument linkDocument = (ODocument) new DBClosure() {
                        @Override
                        protected Object execute(ODatabaseDocument db) {
                            return db.getRecord(linkID);
                        }
                    }.execute();
                    String similarValue = getSimilarDocumentValues(linkDocument);
                    if (similarValue == null) continue;

                    String linkName = linkDocument.field("name") != null ? (String) linkDocument.field("name") : botMessage.WITHOUT_NAME;
                    String link = BotState.GO_TO_CLASS.getCommand() + linkDocument.getClassName()
                            + "_" + linkDocument.getIdentity().getClusterId()
                            + "_" + linkDocument.getIdentity().getClusterPosition()
                            + " : " + linkName;
                    searchValue = String.format(botMessage.HTML_STRONG_TEXT, "• " + name + " : ") + similarValue + " " + link + "\n";
                } else if (isWordInLine(searchWord, value.toString())){
                    searchValue = String.format(botMessage.HTML_STRONG_TEXT, "• " + name + " : ") + value + " " + documentLink + "\n";
                }
                if (searchValue != null) resultList.add(searchValue);
                searchValue = null;
            }
        }
        return resultList.size() > 0 ? resultList : null;
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
                ODocument document = (ODocument) new DBClosure() {
                    @Override
                    protected Object execute(ODatabaseDocument db) {
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
