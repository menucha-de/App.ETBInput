package havis.custom.harting.etb.input.ui.client;

import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;

import havis.net.ui.shared.client.list.WidgetList;

public interface MonitorSectionView {

	void setPresenter(Presenter presenter);
	HasValue<Boolean> getMonitorRunning();
	HasValue<Boolean> getExpanded();
	HasText getCurrentCount();
	HasText getAbsoluteCount();
	WidgetList getTagsList();
	
	interface Presenter {
		void onMonitor();
		void onToggleList();
		void onRefresh();
		void onExport();
	}
}
