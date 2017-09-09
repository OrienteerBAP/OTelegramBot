package org.orienteer.telegram.component.widget;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.command.EditODocumentCommand;
import org.orienteer.core.component.command.SaveODocumentCommand;
import org.orienteer.core.component.meta.ODocumentMetaPanel;
import org.orienteer.core.component.property.DisplayMode;
import org.orienteer.core.component.structuretable.OrienteerStructureTable;
import org.orienteer.core.widget.AbstractWidget;
import org.orienteer.core.widget.Widget;
import org.orienteer.telegram.bot.util.OTelegramUtil;
import org.orienteer.telegram.bot.util.telegram.IOTelegramBotManager;
import org.orienteer.telegram.module.OTelegramModule;

import java.util.List;

/**
 * Widget for manage 'orienteer-telegram'. Enable/Disable bot and enable/disable webhook bot update
 */
@Widget(domain = "document", id = "bot-manager", selector = OTelegramModule.OTELEGRAM_OCLASS, autoEnable = true, order = 10)
public class OTelegramBotManagerWidget extends AbstractWidget<ODocument> {

    @Inject
    private IOTelegramBotManager manager;

    public OTelegramBotManagerWidget(String id, final IModel<ODocument> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
        Form form = new Form("form") {
            @Override
            protected void onSubmit() {
                OTelegramUtil.switchBotStateByDocument(model, manager);
            }
        };
        final IModel<DisplayMode> mode = DisplayMode.VIEW.asModel();
        IModel<List<OProperty>> list = new LoadableDetachableModel<List<OProperty>>() {
            @Override
            protected List<OProperty> load() {
                List<OProperty> result = Lists.newArrayList();
                OClass oClass = model.getObject().getSchemaClass();
                result.add(oClass.getProperty(OTelegramModule.ACTIVE));
                result.add(oClass.getProperty(OTelegramModule.WEB_HOOK_ENABLE));
                return result;
            }
        };
        OrienteerStructureTable<ODocument, OProperty> table =
                new OrienteerStructureTable<ODocument, OProperty>("state", model, list) {
            @Override
            protected Component getValueComponent(String id, IModel<OProperty> rowModel) {
                return new ODocumentMetaPanel<Boolean>(id, mode, model, rowModel);
            }
        };

        table.addCommand(new EditODocumentCommand(table, mode));
        table.addCommand(new SaveODocumentCommand(table, mode));
        form.add(table);
        add(form);
    }

    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.wrench);
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return new ResourceModel("telegram.widget.manager");
    }
}
