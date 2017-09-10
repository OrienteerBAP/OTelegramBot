package org.orienteer.telegram.module;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.orienteer.core.CustomAttribute;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.module.AbstractOrienteerModule;
import org.orienteer.core.util.OSchemaHelper;
import org.orienteer.telegram.bot.util.OTelegramUtil;
import org.orienteer.telegram.bot.util.telegram.IOTelegramBotManager;
import org.orienteer.telegram.component.visualizer.OTelegramVisualizer;
import org.telegram.telegrambots.ApiContextInitializer;
import ru.ydn.wicket.wicketorientdb.model.ODocumentModel;

import java.util.List;

/**
 * Orienteer module for access Orienteer data from Telegram
 */
public class OTelegramModule extends AbstractOrienteerModule {

	public static final String OTELEGRAM_OCLASS     = "OTelegramBot";
	public static final String NAME                 = "telegram";
	public static final String USERNAME             = "username";
	public static final String TOKEN                = "token";
	public static final String USER_SESSION         = "user_session";
	public static final String EXTERNAL_URL         = "external_url";
	public static final String INTERNAL_PATH        = "internal_path";
	public static final String INTERNAL_PORT        = "internal_port";
	public static final String PUBLIC_KEY           = "public_key";
	public static final String CERTIFICATE_STORE    = "certificate_store";
	public static final String CERTIFICATE_PASSWORD = "certificate_password";
	public static final String ACTIVE               = "active";
	public static final String WEB_HOOK_ENABLE      = "web_hook_enable";

	public static final CustomAttribute TELEGRAM_BOT_SEARCH            = CustomAttribute.create("orienteer.telegram.bot.search", OType.BOOLEAN, false, false, false);
	public static final CustomAttribute TELEGRAM_BOT_DOCUMENTS_LIST    = CustomAttribute.create("orienteer.telegram.bot.documents.list", OType.BOOLEAN, false, false, false);
	public static final CustomAttribute TELEGRAM_BOT_SEARCH_QUERY      = CustomAttribute.create("orienteer.telegram.bot.search.query", OType.STRING, null, true, false);
	public static final CustomAttribute TELEGRAM_BOT_CLASS_DESCRIPTION = CustomAttribute.create("orienteer.telegram.bot.class.description", OType.BOOLEAN, false, false, false);

	@Inject
	private IOTelegramBotManager manager;

	@Inject
	private UIVisualizersRegistry registry;

	protected OTelegramModule() {
		super(NAME, 1);
	}
	
	@Override
	public ODocument onInstall(OrienteerWebApplication app, ODatabaseDocument db) {
		OSchemaHelper helper = OSchemaHelper.bind(db);
		helper.oClass(OTELEGRAM_OCLASS)
				.oProperty(USERNAME, OType.STRING)
					.markAsDocumentName()
					.markDisplayable()
					.notNull()
				.oProperty(TOKEN, OType.STRING)
					.switchDisplayable(false)
					.notNull()
				.oProperty(ACTIVE, OType.BOOLEAN)
					.defaultValue("false")
					.updateCustomAttribute(CustomAttribute.HIDDEN, true)
					.assignVisualization("telegram")
					.markDisplayable()
				.oProperty(WEB_HOOK_ENABLE, OType.BOOLEAN)
					.defaultValue("false")
					.updateCustomAttribute(CustomAttribute.HIDDEN, true)
					.assignVisualization("telegram")
					.markDisplayable()
				.oProperty(EXTERNAL_URL, OType.STRING)
					.defaultValue("/")
					.notNull()
					.switchDisplayable(false)
				.oProperty(INTERNAL_PATH, OType.STRING)
					.defaultValue("/")
					.notNull()
					.switchDisplayable(false)
				.oProperty(INTERNAL_PORT, OType.INTEGER)
					.defaultValue("9000")
					.notNull()
					.switchDisplayable(false)
				.oProperty(PUBLIC_KEY, OType.STRING)
					.assignVisualization("telegram")
					.switchDisplayable(false)
				.oProperty(CERTIFICATE_STORE, OType.STRING)
					.assignVisualization("telegram")
					.switchDisplayable(false)
				.oProperty(CERTIFICATE_PASSWORD, OType.STRING)
					.assignVisualization("telegram")
					.switchDisplayable(false)
				.oProperty(USER_SESSION, OType.LONG)
					.defaultValue("30")
					.notNull()
					.switchDisplayable(false);

		return null;
	}

	@Override
	public void onInitialize(OrienteerWebApplication app, ODatabaseDocument db) {
		app.registerWidgets("org.orienteer.telegram.component.widget");
		ApiContextInitializer.init();
		registry.registerUIComponentFactory(new OTelegramVisualizer());
		for (ODocument botDoc : getActiveBotDocuments(db)) {
			ODocumentModel model = new ODocumentModel(botDoc);
			try {
				OTelegramUtil.switchBotStateByDocument(model, manager);
			} catch (Exception ex) {
				botDoc.field(ACTIVE, false);
				botDoc.save();
				OTelegramUtil.stopBotByDocument(model, manager);
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void onDestroy(OrienteerWebApplication app, ODatabaseDocument db) {
		app.unregisterWidgets("org.orienteer.telegram.component.widget");
		for (ODocument botDoc : getActiveBotDocuments(db)) {
			OTelegramUtil.stopBotByDocument(new ODocumentModel(botDoc), manager);
			botDoc.field(ACTIVE, false);
			botDoc.save();
		}
	}

	private List<ODocument> getActiveBotDocuments(ODatabaseDocument db) {
		OSQLSynchQuery<ODocument> sql =
				new OSQLSynchQuery<>(String.format("select from %s where %s = true", OTELEGRAM_OCLASS, ACTIVE));
		return db.query(sql);
	}

}
