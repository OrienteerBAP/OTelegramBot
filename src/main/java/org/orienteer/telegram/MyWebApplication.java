package org.orienteer.telegram;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import org.orienteer.core.OrienteerWebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class MyWebApplication extends OrienteerWebApplication
{
	private static final Logger LOG = LoggerFactory.getLogger(MyWebApplication.class);

	@Override
	public void init()
	{
		super.init();
		mountPages("org.orienteer.telegram.web");
		registerModule(OTelegramModule.class);
	}
	
}
