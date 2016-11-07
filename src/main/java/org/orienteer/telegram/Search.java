package org.orienteer.telegram;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.*;

/**
 * @author Vitaliy Gonchar
 */
public class Search {
    private static final Logger LOG = LoggerFactory.getLogger(Search.class);
    private boolean globalFieldNamesSearch;
    private boolean globalFieldValuesSearch;
    private boolean globalDocumentNamesSearch;

    private boolean classFieldNamesSearch;
    private boolean classFieldValuesSearch;
    private boolean classDocumentNamesSearch;

    private boolean globalSearch;
    private boolean globalClassSearch;

    private final int N = 10;   // max result in one message
    private final String searchWord;
    private final String className;

    private final Set<OClass> CLASS_CACHE = OTelegramBot.getClassCache();

    public Search(String searchWord) {
        this.searchWord = searchWord;
        this.className = null;
    }

    public Search(String searchWord, String className) {
        this.searchWord = searchWord;
        String value = null;
        for (OClass oClass : CLASS_CACHE) {
            if (className.equals(oClass.getName())) {
                value = className;
                break;
            }
        }
        this.className = value;
    }

    public List<String> getResultOfSearch() {
        List<String> result = null;
        if (globalSearch) {
            result = getResultOfGlobalSearch();
        } else if (globalFieldNamesSearch) {
            result = getResultOfFieldNamesSearch();
        } else if (globalFieldValuesSearch) {
            result = getResultOfFieldValuesSearch();
        } else if (globalDocumentNamesSearch) {
            result = getResultOfSearchDocumentGlobal();
        } else if (globalClassSearch) {
            result = className != null ? getResultOfSearchInClassAllOptions() : Arrays.asList(BotMessage.ERROR_MSG);
        } else if (classFieldNamesSearch) {
            result = className != null ? getResultOfSearchFieldNamesInClass() : Arrays.asList(BotMessage.ERROR_MSG);
        } else if (classFieldValuesSearch) {
            result = className != null ? getResultOfSearchFieldValuesInClass() : Arrays.asList(BotMessage.ERROR_MSG);
        } else if (classDocumentNamesSearch) {
            result = className != null ? getResultOfSearchDocumentsInClass() : Arrays.asList(BotMessage.ERROR_MSG);
        }
        return result;
    }

    private List<String> getResultListOfSearch(List<String> fields, List<String> values,
                                               List<String> docs, List<String> classes) {
        List<String> resultList = new LinkedList<>();
        if (fields == null) fields = new ArrayList<>();
        if (values == null) values = new ArrayList<>();
        if (docs == null) docs = new ArrayList<>();
        if (classes == null) classes = new ArrayList<>();

        if (fields.size() > 0 || values.size() > 0 || docs.size() > 0 || classes.size() > 0) {
            int counter = 0;
            if (classes.size() > 0) {
                String info = "\n" + BotMessage.SEARCH_CLASS_NAMES_RESULT + "\n";
                resultList.addAll(splitBigResult(classes, info, counter));
            }
            if (docs.size() > 0) {
                String info = "\n" + BotMessage.SEARCH_DOCUMENT_NAMES_RESULT + "\n";
                resultList.addAll(splitBigResult(docs, info, counter));
            }
            if (fields.size() > 0) {
                String info = "\n" + BotMessage.SEARCH_FIELD_NAMES_RESULT + "\n";
                resultList.addAll(splitBigResult(fields, info, counter));
            }
            if (values.size() > 0) {
                String info = "\n" + BotMessage.SEARCH_FIELD_VALUES_RESULT + "\n";
                resultList.addAll(splitBigResult(values, info, counter));
            }
        } else resultList.add(BotMessage.SEARCH_RESULT_FAILED_MSG);
        return resultList;
    }


    private List<String> getResultOfGlobalSearch() {
        return (List<String>) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                List<String> fieldNamesList = new LinkedList<>();
                List<String> fieldValuesList = new LinkedList<>();
                List<String> documentNamesList = new LinkedList<>();
                List<String> classesNamesList = new LinkedList<>();
                classesNamesList.addAll(searchInClassNames());
                for (OClass oClass : CLASS_CACHE) {
                    ORecordIteratorClass<ODocument> oDocuments = oDatabaseDocument.browseClass(oClass.getName());
                    for (ODocument oDocument : oDocuments) {
                        fieldNamesList.addAll(searchInFieldNames(oDocument));
                        fieldValuesList.addAll(searchInFieldValues(oDocument));
                        String doc = searchDocument(oDocument);
                        if (doc != null) documentNamesList.add(doc);
                    }
                }
                return getResultListOfSearch(fieldNamesList, fieldValuesList, documentNamesList, classesNamesList);
            }
        }.execute();
    }


    private List<String> splitBigResult(List<String> bigResult, String info, int counter) {
        List<String> resultList = new LinkedList<>();
        String head = BotMessage.SEARCH_RESULT_SUCCESS_MSG;
        StringBuilder builder = new StringBuilder();
        builder.append(head);
        builder.append(info);
        for (String string : bigResult) {
            builder.append(string);
            counter++;
            if (counter % N == 0) {
                resultList.add(builder.toString());
                builder = new StringBuilder();
                builder.append(head);
                builder.append(info);
            }
        }
        if (counter % N != 0) resultList.add(builder.toString());
        return resultList;
    }

    /**
     * getResultOfSearch similar class names with word
     * @return result list of getResultOfSearch
     */
    private List<String> searchInClassNames() {
        List<String> resultList = new LinkedList<>();
        String searchClass;
        for (OClass oClass : CLASS_CACHE) {
            if (isWordInLine(searchWord, oClass.getName())) {
                searchClass = "-  class name: " + oClass.getName() + " "
                        + BotState.GO_TO_CLASS.command + oClass.getName() + "\n";
                resultList.add(searchClass);
            }
        }
        return resultList;
    }


    /**
     * Build string with result of searchAll
     * @param oDocument getResultOfSearch document
     * @return string with result of searchAll
     */
    private String searchDocument(ODocument oDocument) {
        StringBuilder builder = new StringBuilder();
        String docName = oDocument.field("name", OType.STRING);
        if (docName == null) docName = "without document name";
        String documentLink = BotState.GO_TO_CLASS.command + oDocument.getClassName()
                + "_" + oDocument.getIdentity().getClusterId()
                + "_" + oDocument.getIdentity().getClusterPosition()
                + " : " + docName;
        if (isWordInLine(searchWord, docName)) {
            builder.append("- ");
            builder.append(docName);
            builder.append(" ");
            builder.append(documentLink);
            builder.append("\n");
        }
        if (builder.length() > 0) return builder.toString();
        return null;
    }


    /**
     * getResultOfSearch similar words in field names
     * @param oDocument document where is getResultOfSearch
     * @return list of strings with result of getResultOfSearch
     */
    private List<String> searchInFieldNames(ODocument oDocument) {
        List<String> resultOfSearch = new LinkedList<>();
        String searchName;
        String [] fieldNames = oDocument.fieldNames();
        String docName = oDocument.field("name", OType.STRING);
        if (docName == null) docName = "without document name";
        String documentLink = BotState.GO_TO_CLASS.command + oDocument.getClassName()
                + "_" + oDocument.getIdentity().getClusterId()
                + "_" + oDocument.getIdentity().getClusterPosition()
                + " : " + docName;
        for (String name : fieldNames) {
            if (name != null && isWordInLine(searchWord, name)) {
                searchName = "- " + name + " : "
                        + oDocument.field(name, OType.STRING) + " " + documentLink + "\n";
                resultOfSearch.add(searchName);
            }
        }
        return resultOfSearch;
    }

    /**
     * getResultOfSearch similar words in field values
     * @param oDocument document where is getResultOfSearch
     * @return list of strings with result of getResultOfSearch
     */
    private List<String> searchInFieldValues(ODocument oDocument) {
        String searchValue;
        List<String> resultList = new LinkedList<>();
        String[] fieldNames = oDocument.fieldNames();
        String docName = oDocument.field("name", OType.STRING);
        if (docName == null) docName = "without document name";
        String documentLink = BotState.GO_TO_CLASS.command + oDocument.getClassName()
                + "_" + oDocument.getIdentity().getClusterId()
                + "_" + oDocument.getIdentity().getClusterPosition()
                + " : " + docName;
        for (String name : fieldNames) {
            String fieldValue = oDocument.field(name, OType.STRING);
            if (name != null && fieldValue != null && isWordInLine(searchWord, fieldValue)) {
                searchValue = "- " + name + " : " + fieldValue + " " + documentLink + "\n";
                resultList.add(searchValue);
            }
        }
        return resultList;
    }

    /**
     * getResultOfSearch similar words in field names in all database
     * @return string with result of getResultOfSearch
     */
    private List<String> getResultOfFieldNamesSearch() {
        return (List<String>) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                List<String> fieldsList = new LinkedList<>();
                for (OClass oClass : CLASS_CACHE) {
                    ORecordIteratorClass<ODocument> oDocuments = oDatabaseDocument.browseClass(oClass.getName());
                    for (ODocument oDocument : oDocuments) {
                       fieldsList.addAll(searchInFieldNames(oDocument));
                    }
                }

                return getResultListOfSearch(fieldsList, null, null, null);
            }
        }.execute();
    }

    /**
     * Search similar words with word in field values in all database
     * @return string with result of getResultOfSearch
     */
    private List<String> getResultOfFieldValuesSearch() {
        return (List<String>) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                List<String> valuesList = new LinkedList<>();
                for (OClass oClass : CLASS_CACHE) {
                    ORecordIteratorClass<ODocument> oDocuments = oDatabaseDocument.browseClass(oClass.getName());
                    for (ODocument oDocument : oDocuments) {
                        valuesList.addAll(searchInFieldValues(oDocument));
                    }
                }
                return getResultListOfSearch(null, valuesList, null, null);
            }
        }.execute();
    }

    /**
     * Search documents with similar names from all database
     * @return result of getResultOfSearch
     */
    private List<String> getResultOfSearchDocumentGlobal() {
        return (List<String>) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                List<String> docsList = new LinkedList<>();
                for (OClass oClass : CLASS_CACHE) {
                    ORecordIteratorClass<ODocument> oDocuments = oDatabaseDocument.browseClass(oClass.getName());
                    for (ODocument oDocument: oDocuments) {
                        String doc = searchDocument(oDocument);
                        if (doc != null) docsList.add(doc);
                    }
                }

                return getResultListOfSearch(null, null, docsList, null);
            }
        }.execute();
    }

    /**
     * getResultOfSearch similar words with word in class
     * @return result of getResultOfSearch
     */
    private List<String> getResultOfSearchInClassAllOptions() {
        return (List<String>) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                List<String> fieldNamesList = new LinkedList<>();
                List<String> fieldValuesList = new LinkedList<>();
                List<String> docNamesList = new LinkedList<>();
                ORecordIteratorClass<ODocument> oDocuments = oDatabaseDocument.browseClass(className);
                for (ODocument oDocument : oDocuments) {
                    fieldNamesList.addAll(searchInFieldNames(oDocument));
                    fieldValuesList.addAll(searchInFieldValues(oDocument));
                    String doc = searchDocument(oDocument);
                    if (doc != null) docNamesList.add(doc);
                }
                return  getResultListOfSearch(fieldNamesList, fieldValuesList, docNamesList, null);
            }
        }.execute();
    }

    /**
     * getResultOfSearch similar field names with fieldName
     * @return result of getResultOfSearch
     */
    private List<String> getResultOfSearchFieldNamesInClass() {
        return (List<String>) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                ORecordIteratorClass<ODocument> oDocuments = oDatabaseDocument.browseClass(className);
                List<String> fieldsList = new LinkedList<>();
                for (ODocument oDocument : oDocuments) {
                    fieldsList.addAll(searchInFieldNames(oDocument));
                }
                return getResultListOfSearch(fieldsList, null, null, null);
            }
        }.execute();
    }

    /**
     * getResultOfSearch similar field values with valueName
     * @return result of getResultOfSearch
     */
    private List<String> getResultOfSearchFieldValuesInClass() {
        return (List<String>) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                ORecordIteratorClass<ODocument> oDocuments = oDatabaseDocument.browseClass(className);
                List<String> fieldValuesList = new LinkedList<>();
                for (ODocument oDocument : oDocuments) {
                    fieldValuesList.addAll(searchInFieldValues(oDocument));
                }
                return getResultListOfSearch(null, fieldValuesList, null, null);
            }
        }.execute();
    }

    /**
     * Search documents with similar names from target class
     * @return result of getResultOfSearch
     */
    private List<String> getResultOfSearchDocumentsInClass() {
        return(List<String>) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                List<String> docList = new LinkedList<>();
                ORecordIteratorClass<ODocument> oDocuments = oDatabaseDocument.browseClass(className);
                for (ODocument oDocument : oDocuments) {
                    String doc = searchDocument(oDocument);
                    if (doc != null) docList.add(doc);
                }

                return getResultListOfSearch(null, null, docList, null);
            }
        }.execute();

    }

    /**
     * getResultOfSearch word in line
     * @param word getResultOfSearch word
     * @param line string where word can be
     * @return true if word is in line
     */
    private boolean isWordInLine(final String word, String line) {
        boolean isIn = false;
        if (line.toLowerCase().contains(word.toLowerCase())) isIn = true;
        return isIn;
    }

//    private List<String> searchFieldNames()

    public boolean isGlobalFieldNamesSearch() {
        return globalFieldNamesSearch;
    }

    public void setGlobalFieldNamesSearch(boolean globalFieldNamesSearch) {
        this.globalFieldNamesSearch = globalFieldNamesSearch;
    }

    public boolean isGlobalFieldValuesSearch() {
        return globalFieldValuesSearch;
    }

    public void setGlobalFieldValuesSearch(boolean globalFieldValuesSearch) {
        this.globalFieldValuesSearch = globalFieldValuesSearch;
    }

    public boolean isGlobalDocumentNamesSearch() {
        return globalDocumentNamesSearch;
    }

    public void setGlobalDocumentNamesSearch(boolean globalDocumentNamesSearch) {
        this.globalDocumentNamesSearch = globalDocumentNamesSearch;
    }

    public boolean isGlobalClassSearch() {
        return globalClassSearch;
    }

    public void setGlobalClassSearch(boolean globalClassSearch) {
        this.globalClassSearch = globalClassSearch;
    }

    public String getSearchWord() {
        return searchWord;
    }

    public boolean isClassFieldNamesSearch() {
        return classFieldNamesSearch;
    }

    public void setClassFieldNamesSearch(boolean classFieldNamesSearch) {
        this.classFieldNamesSearch = classFieldNamesSearch;
    }

    public boolean isClassFieldValuesSearch() {
        return classFieldValuesSearch;
    }

    public void setClassFieldValuesSearch(boolean classFieldValuesSearch) {
        this.classFieldValuesSearch = classFieldValuesSearch;
    }

    public boolean isClassDocumentNamesSearch() {
        return classDocumentNamesSearch;
    }

    public void setClassDocumentNamesSearch(boolean classDocumentNamesSearch) {
        this.classDocumentNamesSearch = classDocumentNamesSearch;
    }

    public int getN() {
        return N;
    }

    public boolean isGlobalSearch() {
        return globalSearch;
    }

    public void setGlobalSearch(boolean globalSearch) {
        this.globalSearch = globalSearch;
    }


}
