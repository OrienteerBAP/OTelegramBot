package org.orienteer.telegram.bot.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.orienteer.telegram.bot.OTelegramBot;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Search document by given word in class
 */
public class OClassDocumentSearch extends AbstractSearch {

    private final String searchWord;
    private final String className;
    private int embeddedId = 0;

    /**
     * Constructor
     * @param bot {@link OTelegramBot} bot which need search
     * @param searchWord {@link String} word which will be search
     * @param className {@link String} class name where will be search
     */
    public OClassDocumentSearch(OTelegramBot bot, String searchWord, String className) {
        super(bot);
        this.searchWord = searchWord;
        this.className = className;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<Integer, String> search() {
        return new DBClosure<Map<Integer, String>>() {
            @Override
            protected Map<Integer, String> execute(ODatabaseDocument db) {
                List<ODocument> docs = getDocuments(db);
                List<String> result = Lists.newArrayList();
                String head = null;
                for (ODocument document : docs) {
                    List<String> resultOfSearch = searchInFieldValues(document);
                    if (resultOfSearch != null && !resultOfSearch.isEmpty()) {
                        result.addAll(resultOfSearch);
                    }
                }

                if (!result.isEmpty()) head = "\n" + Markdown.BOLD.toString(MessageKey.SEARCH_RESULT.toLocaleString(bot))
                        + "\n" + Markdown.BOLD.toString(1 + ".  ");

                return newSearchResult(result, head);
            }

            private List<ODocument> getDocuments(ODatabaseDocument db) {
                return db.query(new OSQLSynchQuery<>(queryCache.get(className)));
            }
        }.execute();
    }


    /**
     * Search similar words in field values
     * @param document document where is newSearchResult
     * @return list of strings with result of newSearchResult
     */
    private List<String> searchInFieldValues(ODocument document) {
        List<String> resultList = Lists.newArrayList();
        String documentLink = BotState.GO_TO_CLASS.getCommand() + document.getClassName()
                + "\\_" + document.getIdentity().getClusterId()
                + "\\_" + document.getIdentity().getClusterPosition();
        String documentName = OTelegramUtil.getDocumentName(document, bot);

        for (String fieldName : document.fieldNames()) {
            String fieldValueAsString = searchInField(fieldName, document, false);
            if (!Strings.isNullOrEmpty(fieldValueAsString) || isWordInLine(searchWord, fieldName)) {
                if (Strings.isNullOrEmpty(fieldValueAsString))
                    fieldValueAsString = searchInField(fieldName, document, true);
                resultList.add(createSearchResultString(searchWord, fieldName) + " : "
                        + fieldValueAsString + " (" + documentName + ": " + documentLink + ")\n");
            }
        }
        return resultList.size() > 0 ? resultList : null;
    }

    @SuppressWarnings("unchecked")
    private String searchInField(String fieldName, ODocument document, boolean withoutSearch) {
        String result = null;
        Object value = document.field(fieldName);
        OType type = OType.getTypeByValue(value);
        if (type != null) {
            if (type.isLink()) {
                result = searchInFieldFromLinkTypes(type, document, value, withoutSearch);
            } else if (type.isEmbedded()) {
                result = searchInFieldFromEmbeddedTypes(type, document, value, withoutSearch);
            } else if (isWordInLine(searchWord, value.toString()) || withoutSearch) {
                result = createSearchResultString(searchWord, value.toString());
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private String searchInFieldFromLinkTypes(OType type, ODocument owner, Object value, boolean withoutSearch) {
        StringBuilder sb = new StringBuilder();
        switch (type) {
            case LINK:
                ODocument link = value instanceof ORecordId ?
                        OTelegramUtil.getDocumentByRecord((ORecordId) value) : (ODocument) value;
                if (link != null) {
                    String linkName = OTelegramUtil.getDocumentName(link, bot);
                    if (!Strings.isNullOrEmpty(linkName) && (isWordInLine(searchWord, linkName) || withoutSearch)) {
                        OTelegramUtil.appendDocumentLink(sb, createSearchResultString(searchWord, linkName), link);
                    }
                }
                break;
            case LINKSET:
            case LINKLIST:
                if (value instanceof Collection) {
                    Collection<Object> documents = (Collection<Object>) value;
                    searchInCollection(sb, owner, documents, withoutSearch);
                }
                break;
            case LINKMAP:
                if (value instanceof Map) {
                    Map<String, Object> docsMap = (Map<String, Object>) value;
                    searchInMap(sb, owner, docsMap, withoutSearch);
                }
                break;
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String searchInFieldFromEmbeddedTypes(OType type, ODocument owner, Object value, boolean withoutSearch) {
        StringBuilder sb = new StringBuilder();
        switch (type) {
            case EMBEDDED:
                ODocument doc = (ODocument) value;
                String valueName = OTelegramUtil.getDocumentName(doc, bot);
                if (!Strings.isNullOrEmpty(valueName) && (isWordInLine(searchWord, valueName) || withoutSearch)) {
                    OTelegramUtil.appendEmbeddedLink(sb, createSearchResultString(searchWord, valueName), embeddedId++, owner, doc);
                }
                break;
            case EMBEDDEDLIST:
            case EMBEDDEDSET:
                if (value instanceof Collection) {
                    Collection<Object> documents = (Collection<Object>) value;
                    searchInCollection(sb, owner, documents, withoutSearch);
                }
                break;
            case EMBEDDEDMAP:
                if (value instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) value;
                    searchInMap(sb, owner, map, withoutSearch);
                }
                break;
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private void searchInCollection(StringBuilder sb, ODocument owner, Collection<Object> collection, boolean withoutSearch) {
        int counter = 0;
        for (Object obj : collection) {
            OType type = OType.getTypeByValue(obj);
            if (type != null && (type.isLink() || type.isEmbedded())) {
                ODocument document = (ODocument) obj;
                String name = OTelegramUtil.getDocumentName(document, bot);
                if (!Strings.isNullOrEmpty(name) && (isWordInLine(searchWord, name) || withoutSearch)) {
                    sb.append("\n").append(counter + 1).append(") ");
                    if (type.isEmbedded()) {
                        OTelegramUtil.appendEmbeddedLink(sb, createSearchResultString(searchWord, name),
                                embeddedId++, owner, document);
                    } else OTelegramUtil.appendDocumentLink(sb, createSearchResultString(searchWord, name), document);
                    counter++;
                }
            } else if (obj != null && (isWordInLine(searchWord, obj.toString()) || withoutSearch)) {
                sb.append("\n").append(counter + 1).append(") ")
                        .append(createSearchResultString(searchWord, obj.toString())).append("\n");
                counter++;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void searchInMap(StringBuilder sb, ODocument owner, Map<String, Object> map, boolean withoutSearch) {
        int counter = 0;
        for (String key : map.keySet()) {
            Object obj = map.get(key);
            OType type = OType.getTypeByValue(obj);
            if (type != null && (type.isEmbedded() || type.isLink())) {
                ODocument doc = (ODocument) obj;
                String name = OTelegramUtil.getDocumentName(doc, bot);
                if (!Strings.isNullOrEmpty(name) && (isWordInLine(searchWord, name) || isWordInLine(searchWord, key) || withoutSearch)) {
                    sb.append("\n").append(counter + 1).append(") ")
                            .append(createSearchResultString(searchWord, key)).append(": ");
                    if (type.isEmbedded()) {
                        OTelegramUtil.appendEmbeddedLink(sb, createSearchResultString(searchWord, name),
                                embeddedId++, owner, doc);
                    } else OTelegramUtil.appendDocumentLink(sb, createSearchResultString(searchWord, name), doc);
                    counter++;
                }
            } else if (obj != null && (isWordInLine(searchWord, obj.toString()) || isWordInLine(searchWord, key) || withoutSearch)) {
                sb.append("\n").append(counter + 1).append(") ")
                        .append(createSearchResultString(searchWord, key)).append(": ")
                        .append(createSearchResultString(searchWord, obj.toString()));
                counter++;
            }
        }
    }
}
