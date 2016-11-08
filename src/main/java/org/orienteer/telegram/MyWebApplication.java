package org.orienteer.telegram;

import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.telegram.module.OTelegramModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyWebApplication extends OrienteerWebApplication
{
	private static final Logger LOG = LoggerFactory.getLogger(MyWebApplication.class);

	@Override
	public void init()
	{
		super.init();
		mountPages("org.orienteer.telegram.web");
		registerWidgets("org.orienteer.telegram.widget");
		registerModule(OTelegramModule.class);
	}
	
}
