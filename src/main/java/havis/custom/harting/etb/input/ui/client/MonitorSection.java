package havis.custom.harting.etb.input.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

import havis.net.ui.shared.client.ConfigurationSection;
import havis.net.ui.shared.client.list.WidgetList;

public class MonitorSection extends ConfigurationSection implements MonitorSectionView {

	@UiField FlowPanel monitorPanel;
	@UiField WidgetList tagsList;
	@UiField InlineLabel countCurrent;
	@UiField InlineLabel countAbsolute;
	@UiField ToggleButton monitorButton;
	@UiField ToggleButton expandList;

	private Presenter presenter;
	private static MonitorSectionUiBinder uiBinder = GWT.create(MonitorSectionUiBinder.class);

	interface MonitorSectionUiBinder extends UiBinder<Widget, MonitorSection> {
	}

	@UiConstructor
	public MonitorSection(String name) {
		super(name);
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiHandler("monitorButton")
	void onMonitorClick(ValueChangeEvent<Boolean> event) {
		presenter.onMonitor();
	}
	
	@UiHandler("expandList")
	void onToggleList(ValueChangeEvent<Boolean> event) {
		presenter.onToggleList();
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public HasValue<Boolean> getMonitorRunning() {
		return monitorButton;
	}

	@Override
	public HasValue<Boolean> getExpanded() {
		return expandList;
	}

	@Override
	public HasText getCurrentCount() {
		return countCurrent;
	}

	@Override
	public HasText getAbsoluteCount() {
		return countAbsolute;
	}

	@Override
	public WidgetList getTagsList() {
		return tagsList;
	}

}
