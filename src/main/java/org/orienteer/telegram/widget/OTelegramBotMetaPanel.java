package org.orienteer.telegram.widget;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.orienteer.core.component.meta.AbstractComplexModeMetaPanel;
import org.orienteer.core.component.property.DisplayMode;

import com.orientechnologies.orient.core.metadata.schema.OClass;

public class OTelegramBotMetaPanel<V> extends AbstractComplexModeMetaPanel<OClass, DisplayMode, String, V>{

	public OTelegramBotMetaPanel(String id, IModel<DisplayMode> modeModel, IModel<OClass> entityModel,
			IModel<String> criteryModel) {
		super(id, modeModel, entityModel, criteryModel);
	}
	
	public static final List<String> OBOT_ATTRS = new ArrayList<>(2);
	static {
		OBOT_ATTRS.add("Enable to search this class in OTelegramBot");
		OBOT_ATTRS.add("Search query");
	}

	@Override
	protected V getValue(OClass entity, String critery) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void setValue(OClass entity, String critery, V value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Component resolveComponent(String id, DisplayMode mode, String critery) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected IModel<String> newLabelModel() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
