package org.orienteer.telegram;

import org.apache.wicket.Application;
import org.apache.wicket.IInitializer;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.telegram.module.OTelegramModule;

/**
 * @author Vitaliy Gonchar
 */
public class Initializer implements IInitializer {
    @Override
    public void init(Application application) {
        OrienteerWebApplication app = (OrienteerWebApplication) application;
        app.registerModule(OTelegramModule.class);
    }

    @Override
    public void destroy(Application application) {
        OrienteerWebApplication app = (OrienteerWebApplication) application;
    }
}
