package havis.custom.harting.etb.input.ui.client;

import org.fusesource.restygwt.client.Defaults;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import havis.net.ui.shared.resourcebundle.ResourceBundle;

public class ETBInputUI extends Composite implements EntryPoint {

	@UiField MonitorSection monitorSection;
	@UiField HistorySection historySection;
	
	private static ETBInputSectionUiBinder uiBinder = GWT.create(ETBInputSectionUiBinder.class);
	private ResourceBundle res = ResourceBundle.INSTANCE;

	interface ETBInputSectionUiBinder extends UiBinder<Widget, ETBInputUI> {
	}

	public ETBInputUI() {
		initWidget(uiBinder.createAndBindUi(this));
		Defaults.setDateFormat(null);
		res.css().ensureInjected();

		new MonitorSectionPresenter(monitorSection);
		new HistorySectionPresenter(historySection);
	}

	@Override
	public void onModuleLoad() {
		RootLayoutPanel.get().add(this);
	}
}
