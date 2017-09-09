package org.orienteer.telegram.component.widget;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.security.ORule;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.core.CustomAttribute;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.command.EditSchemaCommand;
import org.orienteer.core.component.command.SaveSchemaCommand;
import org.orienteer.core.component.meta.OClassMetaPanel;
import org.orienteer.core.component.property.BooleanEditPanel;
import org.orienteer.core.component.property.BooleanViewPanel;
import org.orienteer.core.component.property.DisplayMode;
import org.orienteer.core.component.structuretable.OrienteerStructureTable;
import org.orienteer.core.widget.AbstractModeAwareWidget;
import org.orienteer.core.widget.Widget;
import org.orienteer.telegram.module.OTelegramModule;
import ru.ydn.wicket.wicketorientdb.components.TransactionlessForm;
import ru.ydn.wicket.wicketorientdb.security.OSecurityHelper;
import ru.ydn.wicket.wicketorientdb.security.OrientPermission;

import java.util.ArrayList;
import java.util.List;

/**
 * Orienteer widget for manage {@link OTelegramModule}
 */
@Widget(domain="class", tab="telegram", id="telegram-list", autoEnable=true)
public class OTelegramOClassBotWidget extends AbstractModeAwareWidget<OClass> {
	private static final long serialVersionUID = 1L;

	public OTelegramOClassBotWidget(String id, IModel<OClass> model, IModel<ODocument> widgetDocumentModel) {
		super(id, model, widgetDocumentModel);
		Form<OClass> form = new TransactionlessForm<>("form");
		List<String> propertiesList = new ArrayList<>(4);
		propertiesList.add(OTelegramModule.TELEGRAM_BOT_SEARCH.getName());
		propertiesList.add(OTelegramModule.TELEGRAM_BOT_DOCUMENTS_LIST.getName());
		propertiesList.add(OTelegramModule.TELEGRAM_BOT_CLASS_DESCRIPTION.getName());
		propertiesList.add(OTelegramModule.TELEGRAM_BOT_SEARCH_QUERY.getName());
		OrienteerStructureTable<OClass, String> structureTable =
				new OrienteerStructureTable<OClass, String>("attributes", model, propertiesList) {

			@Override
			protected Component getValueComponent(String id, IModel<String> rowModel) {
				return new OClassMetaPanel<Object>(id, getModeModel(), getModel(), rowModel) {
					@Override
					protected Object getValue(OClass entity, String critery) {
						CustomAttribute customAttribute = CustomAttribute.get(critery);
						return customAttribute.getValue(entity);
					}

					@Override
					protected Component resolveComponent(String id, DisplayMode mode, String critery) {
						if (DisplayMode.EDIT.equals(mode) && !OSecurityHelper.isAllowed(ORule.ResourceGeneric.SCHEMA, null, OrientPermission.UPDATE)) {
							mode = DisplayMode.VIEW;
						}
						if (DisplayMode.VIEW.equals(mode)) {
							if (critery.equals(OTelegramModule.TELEGRAM_BOT_SEARCH_QUERY.getName())) {
								return new Label(id, getModel());
							} else {
								return new BooleanViewPanel(id, Model.<Boolean>of(getModel()));
							}
						} else if (DisplayMode.EDIT.equals(mode)) {
							if (critery.equals(OTelegramModule.TELEGRAM_BOT_SEARCH_QUERY.getName())) {
								return new TextField<>(id, getModel());
							} else return new BooleanEditPanel(id, Model.<Boolean>of(getModel()));
						}
						return null;
					}

					@Override
					protected void setValue(OClass entity, String critery, Object value) {
						CustomAttribute customAttribute = CustomAttribute.get(critery);
						customAttribute.setValue(entity, value);
					}
				};
			}
		};

		structureTable.addCommand(new EditSchemaCommand<>(structureTable, getModeModel()));
		structureTable.addCommand(new SaveSchemaCommand<>(structureTable, getModeModel()));
		form.add(structureTable);
		add(form);
	}

    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.list);
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return new ResourceModel("telegram.widget.search");
    }
    
    @Override
	protected String getWidgetStyleClass() {
		return "strict";
	}
}
