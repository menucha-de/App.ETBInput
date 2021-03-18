package havis.app.etb.input.ui.resourcebundle;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.Constants;

public interface ConstantsResource extends Constants {
	
	public static final ConstantsResource INSTANCE = GWT.create(ConstantsResource.class);

	String etbInput();
	String monitorPanel();
	String historyPanel();
	String inputState();
	String startMonitor();
	String stopMonitor();
}
