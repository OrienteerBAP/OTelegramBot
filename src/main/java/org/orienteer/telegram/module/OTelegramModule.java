package org.orienteer.telegram.module;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.Localizer;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.module.AbstractOrienteerModule;
import org.orienteer.core.util.OSchemaHelper;
import org.orienteer.telegram.bot.OTelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.logging.BotLogger;

import java.util.logging.Level;

/**
 * @author Vitaliy Gonchar
 */
public class OTelegramModule extends AbstractOrienteerModule{

	private static final Logger LOG = LoggerFactory.getLogger(OTelegramModule.class);
	public static final String NAME = "telegram";
	public static final String OCLASS_NAME = "OTelegramBot";
	public static final String OPROPERTY_USERNAME = "username";
	public static final String OPROPERTY_TOKEN = "token";
	public static final String OPROPERTY_USER_SESSION = "user_session";

	protected OTelegramModule() {
		super(NAME, 1);
	}
	
	@Override
	public ODocument onInstall(OrienteerWebApplication app, ODatabaseDocument db) {
		OSchemaHelper helper = OSchemaHelper.bind(db);
		helper.oClass(OCLASS_NAME, OMODULE_CLASS)
				.oProperty(OPROPERTY_USERNAME, OType.STRING).notNull()
				.oProperty(OPROPERTY_TOKEN, OType.STRING).notNull()
				.oProperty(OPROPERTY_USER_SESSION, OType.LONG).defaultValue("30").notNull();

		return new ODocument(helper.getOClass());
	}

	@Override
	public void onInitialize(OrienteerWebApplication app, ODatabaseDocument db) {
		String username = null;
		String token = null;
		long userSession = 0;
		ODocument bot = db.browseClass(OCLASS_NAME).next();
		if (bot.field(OMODULE_ACTIVATE)) {
			username = bot.field(OPROPERTY_USERNAME, OType.STRING);
			token = bot.field(OPROPERTY_TOKEN, OType.STRING);
			userSession = bot.field(OPROPERTY_USER_SESSION, OType.LONG);
		}
		BotConfig botConfig = new BotConfig(username, token, userSession);
		LOG.debug("\n" + botConfig.toString());
		TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
		BotLogger.setLevel(Level.OFF);
		try {
			if (username !=  null) {
				telegramBotsApi.registerBot(OTelegramBot.getOrienteerTelegramBot(botConfig));
			}
		} catch (TelegramApiRequestException e) {
			LOG.error("Cannot register bot");
			if (LOG.isDebugEnabled()) e.printStackTrace();
		}
	}

    public class BotConfig {
        public final String USERNAME;
        public final String TOKEN;
		public final long USER_SESSION;

        BotConfig(String username, String token, long userSession) {
            USERNAME = username;
            TOKEN = token;
			USER_SESSION = userSession;
        }

		@Override
		public String toString() {
			return "BotConfig:"
					+ "\nUsername: " + USERNAME
					+ "\nBot token: " + TOKEN
					+ "\nUser session: " + USER_SESSION;
		}
	}
}
