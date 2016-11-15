package org.orienteer.telegram;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import org.orienteer.core.CustomAttribute;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

/**
 * @author Vitaliy Gonchar
 */
public abstract class CustomConfiguration {
    public static final String CUSTOM_TELEGRAM_SEARCH       = "orienteer.telegramSearch";
    public static final String CUSTOM_TELEGRAM_SEARCH_QUERY = "orienteer.telegramSearchQuery";

    public static void initCustom() {
        CustomAttribute.create(CustomConfiguration.CUSTOM_TELEGRAM_SEARCH, OType.BOOLEAN, false, false);
        CustomAttribute.create(CustomConfiguration.CUSTOM_TELEGRAM_SEARCH_QUERY, OType.STRING, null, true);
        new DBClosure() {
            @Override
            protected Object execute(ODatabaseDocument db) {
                CustomAttribute customAttribute = CustomAttribute.get(CUSTOM_TELEGRAM_SEARCH);
                CustomAttribute customAttribute1 = CustomAttribute.get(CUSTOM_TELEGRAM_SEARCH_QUERY);
                for (OClass oClass : db.getMetadata().getSchema().getClasses()) {
                    if (oClass.getCustom(CUSTOM_TELEGRAM_SEARCH) == null) {
                        customAttribute.setValue(oClass, false);
                    }
                    if (oClass.getCustom(CUSTOM_TELEGRAM_SEARCH_QUERY) == null) {
                        customAttribute1.setValue(oClass, "SELECT FROM " + oClass.getName());
                    }
                }
                return null;
            }
        }.execute();
    }
}
