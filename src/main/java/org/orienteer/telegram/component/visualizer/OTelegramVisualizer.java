package org.orienteer.telegram.component.visualizer;

import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.orienteer.core.component.property.BooleanViewPanel;
import org.orienteer.core.component.property.DisplayMode;
import org.orienteer.core.component.visualizer.AbstractSimpleVisualizer;
import org.orienteer.telegram.component.OWebHookCertificateUploadPanel;

/**
 * Visualizer for upload WebHook certificates
 */
public class OTelegramVisualizer extends AbstractSimpleVisualizer {

    public OTelegramVisualizer() {
        super("telegram", false, OType.STRING, OType.BOOLEAN);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> Component createComponent(String id, DisplayMode mode, IModel<ODocument> documentModel,
                                         IModel<OProperty> propertyModel, IModel<V> valueModel) {
        Component component = null;
        OType type = propertyModel.getObject().getType();
        if (mode == DisplayMode.EDIT) {
            switch (type) {
                case STRING:
                    component = new OWebHookCertificateUploadPanel(id, (IModel<String>) valueModel);
                    break;
                case BOOLEAN:
                    component = new BooleanViewPanel(id, (IModel<Boolean>) valueModel);
                    break;
            }
        } else {
            switch (type) {
                case STRING:
                    component = new Label(id, valueModel);
                    break;
                case BOOLEAN:
                    component = new BooleanViewPanel(id, (IModel<Boolean>) valueModel);
                    break;
            }
        }
        return component;
    }
}