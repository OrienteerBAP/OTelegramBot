package org.orienteer.telegram.bot;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import org.orienteer.core.CustomAttribute;
import org.orienteer.telegram.CustomConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Vitaliy Gonchar
 */
public abstract class Cache {
    private static Map<String, OClass> classCache;
    private static Map<String, String> queryCache;

    private static int schemaVersion;

    private static final Logger LOG = LoggerFactory.getLogger(Cache.class);

    public static void initCache() {
        new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument db) {
                int newVersion = db.getMetadata().getSchema().getVersion();
                if (schemaVersion != newVersion) {
                    createClassCache();
                    createQueryCache();
                    schemaVersion = newVersion;
                }
                return null;
            }
        }.execute();
    }

    private static void createClassCache() {
        new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument db) {
                classCache = new HashMap<>();
                CustomAttribute customAttribute = CustomAttribute.get(CustomConfiguration.CUSTOM_TELEGRAM_SEARCH);
                for (OClass oClass : db.getMetadata().getSchema().getClasses()) {
                    if (customAttribute.getValue(oClass)) {
                        classCache.put(oClass.getName(), oClass);
                    }
                }
                return null;
            }
        }.execute();
        LOG.debug("Class cache size: " + classCache.size());
    }

    private static void createQueryCache() {
        queryCache = new HashMap<>();
        if (classCache == null) createClassCache();
        CustomAttribute customAttribute = CustomAttribute.get(CustomConfiguration.CUSTOM_TELEGRAM_SEARCH_QUERY);
        for (OClass oClass : classCache.values()) {
            queryCache.put(oClass.getName(), (String) customAttribute.getValue(oClass));
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
