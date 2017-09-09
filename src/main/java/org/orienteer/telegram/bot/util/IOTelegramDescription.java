package org.orienteer.telegram.bot.util;

import org.apache.wicket.util.io.IClusterable;

/**
 * Interface which represents description of {@link com.orientechnologies.orient.core.record.impl.ODocument}
 * or {@link com.orientechnologies.orient.core.metadata.schema.OClass} in Telegram message
 * @param <T> type of value
 */
public interface IOTelegramDescription<T> extends IClusterable {

    /**
     * @return {@link T} which contains description of object for Telegram message
     */
    public T getDescription();
}
