package havis.app.etb.input.ui.client;

import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVisibility;

import havis.net.ui.shared.client.list.WidgetList;

public interface HistorySectionView {
	void setPresenter(Presenter presenter);
	HasValue<Boolean> isObserving();
	HasVisibility getLogControls();
	HasVisibility getRefreshButton();
	HasVisibility getExportButton();
	WidgetList getLogList();

	interface Presenter {
		void onClearLog();
		void onExportLog();
		void onObserve();
		void onRefreshLog();
		void onScroll();
	}
}
