package org.orienteer.telegram.widget;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.orienteer.core.CustomAttributes;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.command.EditSchemaCommand;
import org.orienteer.core.component.command.SaveSchemaCommand;
import org.orienteer.core.component.meta.OClassMetaPanel;
import org.orienteer.core.component.property.DisplayMode;
import org.orienteer.core.component.structuretable.OrienteerStructureTable;
import org.orienteer.core.component.widget.oclass.OClassConfigurationWidget;
import org.orienteer.core.widget.AbstractModeAwareWidget;
import org.orienteer.core.widget.AbstractWidget;
import org.orienteer.core.widget.Widget;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;

import ru.ydn.wicket.wicketorientdb.components.TransactionlessForm;

/**
 * @author Vitaliy Gonchar
 */
@Widget(domain="class", tab="telegram", id="telegram-list", autoEnable=true)
public class OTelegramBotWidget extends AbstractModeAwareWidget<OClass> {
	private static final long serialVersionUID = 1L;

	private OrienteerStructureTable<OClass, String> structureTable;

	private List<String> list = new ArrayList<>();
	
	public OTelegramBotWidget(String id, IModel<OClass> model, IModel<ODocument> widgetDocumentModel) {
		super(id, model, widgetDocumentModel);
		Form<OClass> form = new TransactionlessForm<OClass>("form");
		list.add(CustomAttributes.SEARCH_QUERY.getName());
		list.add(CustomAttributes.DISPLAYABLE.getName());
		structureTable = new OrienteerStructureTable<OClass, String>("attributes", getModel(), list) {
			
			@Override
			protected Component getValueComponent(String id, IModel<String> rowModel) {
				// TODO Auto-generated method stub
				return new OClassMetaPanel<Object>(id, getModeModel(), OTelegramBotWidget.this.getModel(), rowModel);
			}
		};
		structureTable.addCommand(new EditSchemaCommand<OClass>(structureTable, getModeModel()));
		structureTable.addCommand(new SaveSchemaCommand<OClass>(structureTable, getModeModel(), getModel()));
		form.add(structureTable);
		add(form);
	}

	
	
    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.list);
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return Model.of("Telegram shared documents");
    }
    
    @Override
	protected String getWidgetStyleClass() {
		return "strict";
	}
}
