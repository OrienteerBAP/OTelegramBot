package org.orienteer.telegram.bot;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import org.orienteer.telegram.module.OTelegramModule;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class which contains oClass and query cache
 */
public abstract class OTelegramCache {
    private static Map<String, OClass> classCache;
    private static Map<String, String> queryCache;

    private static int schemaVersion;

    public static synchronized void initCache() {
        new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument db) {
                int newVersion = db.getMetadata().getSchema().getVersion();
                if (schemaVersion != newVersion) {
                    createClassCache(db);
                    createQueryCache();
                    schemaVersion = newVersion;
                }
                return null;
            }
        }.execute();
    }

    private static void createClassCache(ODatabaseDocument db) {
        classCache = new HashMap<>();
        for (OClass oClass : db.getMetadata().getSchema().getClasses()) {
            if (OTelegramModule.TELEGRAM_BOT_SEARCH.getValue(oClass)) {
                classCache.put(oClass.getName(), oClass);
            }
        }
    }

    private static void createQueryCache() {
        queryCache = new HashMap<>();
        for (OClass oClass : classCache.values()) {
            String query = OTelegramModule.TELEGRAM_BOT_SEARCH_QUERY.getValue(oClass);
            if (query == null){
                query = "SELECT FROM " + oClass.getName();
                OTelegramModule.TELEGRAM_BOT_SEARCH_QUERY.setValue(oClass, query);
            }
            queryCache.put(oClass.getName(), query);
        }
    }

    public static Map<String, String> getQueryCache() {
        initCache();
        return queryCache;
    }

    public static Map<String, OClass> getClassCache() {
        initCache();
        return classCache;
    }
}
