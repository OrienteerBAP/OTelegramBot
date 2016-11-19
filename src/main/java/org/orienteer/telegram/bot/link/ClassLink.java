package org.orienteer.telegram.bot.link;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.telegram.bot.BotMessage;
import org.orienteer.telegram.bot.response.BotState;
import org.orienteer.telegram.bot.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.*;

/**
 * @author Vitaliy Gonchar
 */
public class ClassLink implements Link {
    private final String className;
    private final BotMessage botMessage;

    private static final Logger LOG = LoggerFactory.getLogger(ClassLink.class);

    public ClassLink(String classLink, BotMessage botMessage) {
        this.botMessage = botMessage;
        className = classLink.substring(BotState.GO_TO_CLASS.getCommand().length());
    }

    @Override
    public String goTo() {
        return (String) new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument oDatabaseDocument) {
                StringBuilder builder = new StringBuilder(
                        String.format(botMessage.HTML_STRONG_TEXT, botMessage.CLASS_DESCRIPTION_MSG) + "\n\n");
                Map<String, OClass> classCache = Cache.getClassCache();
                if (!classCache.containsKey(className)) {
                    return botMessage.SEARCH_FAILED_CLASS_BY_NAME;
                }
                OClass oClass = classCache.get(className);
                builder.append(String.format(botMessage.HTML_STRONG_TEXT, botMessage.NAME + " "));
                builder.append(oClass.getName());
                builder.append("\n");
                builder.append(String.format(botMessage.HTML_STRONG_TEXT, botMessage.SUPER_CLASSES + " "));
                List<String> superClassNames = new ArrayList<>();
                for (OClass superClass : oClass.getSuperClasses()) {
                    if (classCache.containsKey(superClass.getName())) {
                        superClassNames.add("/" + superClass.getName() + " ");
                    }
                }
                if (superClassNames.size() > 0) {
                    for (String str : superClassNames) {
                        builder.append(str);
                    }
                } else builder.append(botMessage.WITHOUT_SUPER_CLASSES);
                builder.append("\n");
                Collection<OProperty> properties = oClass.properties();
                List<String> resultList = new ArrayList<>();
                for (OProperty property : properties) {
                    resultList.add(String.format(botMessage.HTML_STRONG_TEXT, property.getName())
                            + ": " + property.getDefaultValue() + " ("+ botMessage.DEFAULT_VALUE + ")");
                }
                Collections.sort(resultList);
                for (String string : resultList) {
                    builder.append(string);
                    builder.append("\n");
                }
                builder.append("\n");
                builder.append(String.format(botMessage.HTML_STRONG_TEXT, botMessage.CLASS_DOCUMENTS));
                builder.append("\n");
                ORecordIteratorClass<ODocument> oDocuments = oDatabaseDocument.browseClass(oClass.getName());
                resultList = new ArrayList<>();
                for (ODocument oDocument : oDocuments) {
                    String docId = BotState.GO_TO_CLASS.getCommand() + oDocument.getClassName()
                            + "_" + oDocument.getIdentity().getClusterId()
                            + "_" + oDocument.getIdentity().getClusterPosition();
                    resultList.add(oDocument.field("name") + " " + docId);
                }
                Collections.sort(resultList);
                for (String string : resultList) {
                    builder.append(string);
                    builder.append("\n");
                }
                return builder.toString();
            }
        }.execute();
    }

}
