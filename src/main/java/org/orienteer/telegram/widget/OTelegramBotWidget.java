package org.orienteer.telegram.widget;

import java.io.Serializable;
import java.util.*;

import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.metadata.schema.*;
import com.orientechnologies.orient.core.metadata.security.ORule;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;
import org.orienteer.core.CustomAttributes;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.ODocumentPageLink;
import org.orienteer.core.component.command.EditSchemaCommand;
import org.orienteer.core.component.command.SaveSchemaCommand;
import org.orienteer.core.component.meta.AbstractComplexModeMetaPanel;
import org.orienteer.core.component.meta.AbstractModeMetaPanel;
import org.orienteer.core.component.meta.OClassMetaPanel;
import org.orienteer.core.component.property.BooleanEditPanel;
import org.orienteer.core.component.property.BooleanViewPanel;
import org.orienteer.core.component.property.DisplayMode;
import org.orienteer.core.component.structuretable.OrienteerStructureTable;
import org.orienteer.core.component.widget.oclass.OClassConfigurationWidget;
import org.orienteer.core.component.widget.oclass.OClassHooksWidget;
import org.orienteer.core.util.ODocumentChoiceRenderer;
import org.orienteer.core.widget.AbstractModeAwareWidget;
import org.orienteer.core.widget.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.record.impl.ODocument;

import ru.ydn.wicket.wicketorientdb.OrientDbWebSession;
import ru.ydn.wicket.wicketorientdb.components.TransactionlessForm;
import ru.ydn.wicket.wicketorientdb.model.SimpleNamingModel;
import ru.ydn.wicket.wicketorientdb.proto.OClassPrototyper;
import ru.ydn.wicket.wicketorientdb.security.OSecurityHelper;
import ru.ydn.wicket.wicketorientdb.security.OrientPermission;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

/**
 * @author Vitaliy Gonchar
 */
@Widget(domain="class", tab="telegram", id="telegram-list", autoEnable=true)
public class OTelegramBotWidget extends AbstractModeAwareWidget<OClass> {
	private static final long serialVersionUID = 1L;

	private static final String SYSTEM_CUSTOM = "orienteer.bot.";

	private static final Logger LOG = LoggerFactory.getLogger(OTelegramBotWidget.class);

	private OrienteerStructureTable<OClass, String> structureTable;

	private List<String> propertiesList = new ArrayList<>();
	
	public OTelegramBotWidget(String id, IModel<OClass> model, IModel<ODocument> widgetDocumentModel) {
		super(id, model, widgetDocumentModel);
		Form<OClass> form = new TransactionlessForm<OClass>("form");
		propertiesList.add(SYSTEM_CUSTOM + "telegramSearch");
		propertiesList.add(SYSTEM_CUSTOM + "telegramSearchQuery");

		structureTable = new OrienteerStructureTable<OClass, String>("attributes", model, propertiesList) {

			@Override
			protected Component getValueComponent(String id, IModel<String> rowModel) {
				return new OClassMetaPanel<Object>(id, getModeModel(), getModel(), rowModel) {
					@Override
					protected Object getValue(OClass entity, String critery) {
						String custom = getEntityObject().getCustom(getPropertyObject());
						LOG.debug("custom: " + custom);

						if (getPropertyObject().equals(propertiesList.get(0))) {
							return custom != null ? new Boolean(custom) : false;
						} else return custom != null ? custom : "SELECT * FROM " + getEntityObject().getName() + " WHERE name=?";
					}

					@Override
					protected Component resolveComponent(String id, DisplayMode mode, String critery) {
						if (DisplayMode.EDIT.equals(mode) && !OSecurityHelper.isAllowed(ORule.ResourceGeneric.SCHEMA, null, OrientPermission.UPDATE)) {
							mode = DisplayMode.VIEW;
						}
						if (DisplayMode.VIEW.equals(mode)) {
							if (critery.equals(propertiesList.get(0))) {
								return new BooleanViewPanel(id, Model.<Boolean>of(getModel())).setHideIfFalse(true);
							} else return new Label(id, getModel());
						} else if (DisplayMode.EDIT.equals(mode)){
							if (critery.equals(propertiesList.get(0))) {
								return new BooleanEditPanel(id, Model.<Boolean>of(getModel()));
							} else return new TextField(id, getModel());
						}
						return null;
					}

					@Override
					protected void setValue(OClass entity, String critery, Object value) {
						ODatabaseDocument db = OrientDbWebSession.get().getDatabase();
						db.commit();
						if (value != null) {
							entity.setCustom(critery, value.toString());
						}
						db.commit();
					}
				};
			}
		};

		structureTable.addCommand(new EditSchemaCommand<OClass>(structureTable, getModeModel()));
		structureTable.addCommand(new SaveSchemaCommand<OClass>(structureTable, getModeModel()));
		form.add(structureTable);
		add(form);
	}

    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.list);
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return Model.of("Telegram enable");
    }
    
    @Override
	protected String getWidgetStyleClass() {
		return "strict";
	}
}
