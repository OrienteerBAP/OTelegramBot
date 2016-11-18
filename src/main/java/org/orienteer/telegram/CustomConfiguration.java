package org.orienteer.telegram;

import com.orientechnologies.orient.core.metadata.schema.OType;
import org.orienteer.core.CustomAttribute;

/**
 * @author Vitaliy Gonchar
 */
public abstract class CustomConfiguration {
    public static final String CUSTOM_TELEGRAM_SEARCH       = "orienteer.telegramSearch";
    public static final String CUSTOM_TELEGRAM_SEARCH_QUERY = "orienteer.telegramSearchQuery";

    public static final CustomAttribute TELEGRAM_SEARCH = CustomAttribute.create(CustomConfiguration.CUSTOM_TELEGRAM_SEARCH, OType.BOOLEAN, false, false, false);
    public static final CustomAttribute TELEGRAM_SEARCH_QUERY = CustomAttribute.create(CustomConfiguration.CUSTOM_TELEGRAM_SEARCH_QUERY, OType.STRING, null, true, false);
}
