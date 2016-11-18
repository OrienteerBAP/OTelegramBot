package org.orienteer.telegram.bot.search;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.orienteer.telegram.bot.BotMessage;
import org.orienteer.telegram.bot.BotState;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.ArrayList;

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
        String searchValue;
        ArrayList<String> resultList = new ArrayList<>();
        String[] fieldNames = oDocument.fieldNames();
        String docName = oDocument.field("name", OType.STRING);
        if (docName == null) docName = "without document name";
        String documentLink = BotState.GO_TO_CLASS.getCommand() + oDocument.getClassName()
                + "_" + oDocument.getIdentity().getClusterId()
                + "_" + oDocument.getIdentity().getClusterPosition()
                + " : " + docName;
        for (String name : fieldNames) {
            String fieldValue = oDocument.field(name, OType.STRING);
            if (name != null && fieldValue != null && isWordInLine(searchWord, fieldValue)) {
                searchValue = "• " + name + " : " + fieldValue + " " + documentLink + "\n";
                resultList.add(searchValue);
            }
        }
        return resultList.size() > 0 ? resultList : null;
    }
}
