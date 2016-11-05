package org.orienteer.telegram.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.metadata.schema.OGlobalProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.ODocumentPageLink;
import org.orienteer.core.component.command.EditSchemaCommand;
import org.orienteer.core.component.command.SaveSchemaCommand;
import org.orienteer.core.component.meta.AbstractModeMetaPanel;
import org.orienteer.core.component.meta.OClassMetaPanel;
import org.orienteer.core.component.property.DisplayMode;
import org.orienteer.core.component.structuretable.OrienteerStructureTable;
import org.orienteer.core.util.ODocumentChoiceRenderer;
import org.orienteer.core.widget.AbstractModeAwareWidget;
import org.orienteer.core.widget.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.record.impl.ODocument;

import ru.ydn.wicket.wicketorientdb.OrientDbWebSession;
import ru.ydn.wicket.wicketorientdb.components.TransactionlessForm;
import ru.ydn.wicket.wicketorientdb.model.SimpleNamingModel;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

/**
 * @author Vitaliy Gonchar
 */
@Widget(domain="class", tab="telegram", id="telegram-list", autoEnable=true)
public class OTelegramBotWidget extends AbstractModeAwareWidget<OClass> {
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(OTelegramBotWidget.class);
	
	private OrienteerStructureTable<OClass, String> structureTable;

	private List<String> propertiesList = new ArrayList<>();
	
	public OTelegramBotWidget(String id, IModel<OClass> model, IModel<ODocument> widgetDocumentModel) {
		super(id, model, widgetDocumentModel);
		Form<OClass> form = new TransactionlessForm<OClass>("form");
		test();
		propertiesList.add("isEnableToSearch");
		propertiesList.add("searchQuery");
		structureTable = new OrienteerStructureTable<OClass, String>("attributes", model, propertiesList) {
			
			@Override
			protected Component getValueComponent(String id, IModel<String> rowModel) {
				return new AbstractModeMetaPanel<OClass, DisplayMode, String, ODocument>(id, getModeModel(), OTelegramBotWidget.this.getModel(), rowModel) {
					@Override
					protected IModel<ODocument> resolveValueModel() {
						return new LoadableDetachableModel<ODocument>() {
							@Override
							protected ODocument load() {
								return null;
							}

							@Override
							public void setObject(ODocument value) {

							}
						};
					}

					@Override
					protected IModel<String> newLabelModel() {
						return new SimpleNamingModel<String>("class.bot."+ getPropertyObject());
					}

					@Override
					protected Component resolveComponent(String id, DisplayMode mode, String critery) {
						if(DisplayMode.EDIT.equals(mode) && critery.equals("isEnableToSearch")) {
							return new CheckBox(id);
						} else if (DisplayMode.EDIT.equals(mode) && critery.equals("searchQuery")) {
							return new TextArea<ODocument>(id);
						} else{
							return new ODocumentPageLink(id, getValueModel()).setDocumentNameAsBody(true);
						}
					}
				};
			}
		};
		structureTable.addCommand(new EditSchemaCommand<OClass>(structureTable, getModeModel()));
		structureTable.addCommand(new SaveSchemaCommand<OClass>(structureTable, getModeModel()));
		form.add(structureTable);
		add(form);
	}

	private void test() {
		new DBClosure() {
			@Override
			protected Object execute(ODatabaseDocument db) {
				OSchema schema = db.getMetadata().getSchema();

				List<OGlobalProperty> globalProperties = schema.getGlobalProperties();
				for (OGlobalProperty pr : globalProperties) {
					LOG.info("global property - name: " + pr.getName() + " type: " + pr.getType() + " id: " + pr.getId());
				}

				return null;
			}
		}.execute();
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
