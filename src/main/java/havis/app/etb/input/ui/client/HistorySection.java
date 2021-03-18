package havis.app.etb.input.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

import havis.net.ui.shared.client.ConfigurationSection;
import havis.net.ui.shared.client.list.WidgetList;

public class HistorySection extends ConfigurationSection implements HistorySectionView {
	private HistorySectionView.Presenter presenter;

	@UiField ToggleButton observeButton;
	@UiField HTMLPanel logControls;
	@UiField WidgetList logList;
	@UiField Button refreshButton;
	@UiField Button exportButton;

	private static HistorySectionUiBinder uiBinder = GWT.create(HistorySectionUiBinder.class);

	interface HistorySectionUiBinder extends UiBinder<Widget, HistorySection> {
	}

	@UiConstructor
	public HistorySection(String name) {
		super(name);
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiHandler("observeButton")
	void onObserveClick(ClickEvent e) {
		presenter.onObserve();
	}
	
	@UiHandler("refreshButton")
	void onRefreshClick(ClickEvent e) {
		presenter.onRefreshLog();
	}

	@UiHandler("exportButton")
	void onExportClick(ClickEvent e) {
		presenter.onExportLog();
	}
	
	@UiHandler("logList")
	void onScroll(ScrollEvent e) {
		presenter.onScroll();
	}
	
	@Override
	public void setPresenter(HistorySectionView.Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public HasValue<Boolean> isObserving() {
		return observeButton;
	}

	@Override
	public HasVisibility getLogControls() {
		return logControls;
	}

	@Override
	public WidgetList getLogList() {
		return logList;
	}

	@Override
	public HasVisibility getRefreshButton() {
		return refreshButton;
	}
	
	@Override
	public HasVisibility getExportButton() {
		return exportButton;
	}

}
