package org.orienteer.telegram;

import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.module.AbstractOrienteerModule;
import org.orienteer.core.util.OSchemaHelper;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.metadata.schema.OType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.logging.BotLogger;
import org.telegram.telegrambots.logging.BotsFileHandler;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

/**
 * @author Vitaliy Gonchar
 */
public class OTelegramModule extends AbstractOrienteerModule{

	private static final Logger LOG = LoggerFactory.getLogger(OTelegramModule.class);

	public static final String OCLASS_NAME = "OTelegramBot";
	public static final String OPROPERTY_USERNAME = "username";
	public static final String OPROPERTY_TOKEN = "token";

	protected OTelegramModule() {
		super("OTelegramModule", 1);
	}
	
	@Override
	public ODocument onInstall(OrienteerWebApplication app, ODatabaseDocument db) {
		OSchemaHelper helper = OSchemaHelper.bind(db);
		helper.oClass(OCLASS_NAME, "OModule")
				.oProperty(OPROPERTY_USERNAME, OType.STRING)
				.oProperty(OPROPERTY_TOKEN, OType.STRING)
				.switchDisplayable(true, OPROPERTY_USERNAME, OPROPERTY_TOKEN);
		LOG.debug("Install");
		//Install data model
		//Return null of default OModule is enough
		return null;
	}

	@Override
	public void onInitialize(OrienteerWebApplication app, ODatabaseDocument db) {
		createClass(db);
		BotConfig botConfig = readBotConfig(db);
		TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
		BotLogger.setLevel(Level.WARNING);
		BotLogger.registerLogger(new ConsoleHandler());
		try {
			LOG.debug("Database is closed: " + db.isClosed());
			telegramBotsApi.registerBot(new OTelegramBot(botConfig, db));
		} catch (TelegramApiRequestException e) {
			LOG.error("Cannot register bot");
			if (LOG.isDebugEnabled()) e.printStackTrace();
		}
	}

	protected void createClass(ODatabaseDocument db) {
		if (!db.getMetadata().getSchema().existsClass(OCLASS_NAME)) {
			OClass oTelegramClass = db.getMetadata().getSchema().createClass(OCLASS_NAME);
			OClass oModule = db.getMetadata().getSchema().getClass("OModule");
			oTelegramClass.addSuperClass(oModule);
			oTelegramClass.createProperty(OPROPERTY_USERNAME, OType.STRING);
			oTelegramClass.createProperty(OPROPERTY_TOKEN, OType.STRING);
		}
	}

	protected BotConfig readBotConfig(ODatabaseDocument db) {
		ORecordIteratorClass<ODocument> oTelegramBots = db.browseClass(OCLASS_NAME);
		String username = null;
		String token = null;
		if (oTelegramBots.hasNext()) {
			ODocument bot = oTelegramBots.next();
			if (bot.field(OMODULE_ACTIVATE)) {
				username = bot.field("username");
				token = bot.field("token");
			}
		}
		LOG.info("Bot username: " + username);
		LOG.info("Bot token: " + token);
		return new BotConfig(username, token);
    }

    public class BotConfig {
        final String USERNAME;
        final String TOKEN;

        BotConfig(String username, String token) {
            USERNAME = username;
            TOKEN = token;
        }
    }
}
