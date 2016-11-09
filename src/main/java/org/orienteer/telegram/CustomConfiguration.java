package org.orienteer.telegram;

import com.orientechnologies.orient.core.metadata.schema.OType;
import org.orienteer.core.CustomAttribute;

/**
 * @author Vitaliy Gonchar
 */
public abstract class CustomConfiguration {
    public static final String CUSTOM_TELEGRAM_SEARCH = "orienteer.telegramSearch";
    public static final String CUSTOM_TELEGRAM_SEARCH_QUERY = "telegramSearchQuery";

    public static void initCustom() {
        CustomAttribute.create(CustomConfiguration.CUSTOM_TELEGRAM_SEARCH, OType.BOOLEAN, false, false);
        CustomAttribute.create(CustomConfiguration.CUSTOM_TELEGRAM_SEARCH_QUERY, OType.STRING, "", true);
    }
}
