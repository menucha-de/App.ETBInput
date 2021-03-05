package havis.custom.harting.etb.input.ui.client;

import java.util.List;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import havis.custom.harting.etb.input.HistoryEntry;
import havis.custom.harting.etb.input.rest.async.ETBInputServiceAsync;
import havis.net.rest.shared.data.SerializableValue;

public class HistorySectionPresenter implements HistorySectionView.Presenter {
	
	private ETBInputServiceAsync logging = GWT.create(ETBInputServiceAsync.class);
	
	private HistorySectionModel model;
	private HistorySectionView view;
	private Timer timer;
	private boolean locked;
	private static final String DOWNLOAD_LINK = GWT.getHostPageBaseURL() + "rest/webui/harting/etb/export";

	public HistorySectionPresenter(final HistorySectionView historySectionView) {
		this.model = new HistorySectionModel();
		this.view = historySectionView;
		view.setPresenter(this);
		setListHeader();
		refresh();
	}
	
	private void setLocked(boolean locked) {
		this.locked = locked;
	}

	private void setButtonsVisible(boolean visible) {
		view.getRefreshButton().setVisible(visible);
		view.getExportButton().setVisible(visible);
		String overflow = locked ? "hidden" : "auto";
		view.getLogList().getItemsContainter().getElement().getStyle().setProperty("overflow", overflow);
	}
	
	private void setListHeader() {
		for (String item : HistorySectionModel.getFieldLabels()) {
			view.getLogList().addHeaderCell(item);
		}
	}
	
	private Widget[] createWidgetRow(String[] row) {
		Widget[] widgetRow = new Widget[row.length];
		for (int i = 0; i < row.length; i++) {
			InlineHTML ih = new InlineHTML(row[i]);
			ih.setTitle(row[i]);
			widgetRow[i] = ih;
		}
		return widgetRow;
	}

	private void prependLogEntry(String[] row) {
		Widget[] w = createWidgetRow(row);
		view.getLogList().insertRow(w);
	}
	

	private void loadLogEntries(final boolean refresh) {
		logging.getHistoryEntries(model.getLimit(), model.getOffset(), new MethodCallback<List<HistoryEntry>>() {
			
			@Override
			public void onSuccess(Method method, List<HistoryEntry> response) {
				if (refresh) {
					model.reset();
					view.getLogList().clear();
				}
				model.setLogEntries(response);
				if (refresh) {
					int count = model.getInitialCount();
					int rows = 10;
					int i;
					for (i = 0; i < rows - count; ++i) {
						prependLogEntry(new String[] { "", "", "", "", "" });
					}
					for (; i < rows; ++i) {
						prependLogEntry(model.getPreviousEntry());
					}
					view.getLogList().getItemsContainter().setVerticalScrollPosition(34);
				}
				if (!view.isObserving().getValue()) {
					setLocked(false);
				}
			}
			
			@Override
			public void onFailure(Method method, Throwable exception) {
				if (!view.isObserving().getValue()) {
					setLocked(false);
				}
			}
		});
	}

	private void refresh() {
		if (!view.isObserving().getValue()) {
			setLocked(true);
		}
		logging.getHistoryCount(new MethodCallback<SerializableValue<Integer>>() {
			
			@Override
			public void onSuccess(Method method, SerializableValue<Integer> response) {
				if (response.getValue() > 0) {
					model.setOffset(response.getValue() - model.getLimit());
					loadLogEntries(true);
				} else {
					view.getLogList().clear();
					if (!view.isObserving().getValue()) {
						setLocked(false);
					}
				}
			}
			
			@Override
			public void onFailure(Method method, Throwable exception) {
				setLocked(false);
			}
		});
	}

	public void onObserve() {
		refresh();
		if (view.isObserving().getValue()) {
			setLocked(true);
			setButtonsVisible(false);
			timer = new Timer() {
				
				@Override
				public void run() {
					refresh();
				}
			};
			timer.scheduleRepeating(1000);
		} else {
			timer.cancel();
			setLocked(false);
			setButtonsVisible(true);
		}
	}

	public void onClearLog() {
		view.getLogList().clear();
	}

	public void onExportLog() {
		Window.Location.assign(DOWNLOAD_LINK);
	}

	private void scrollUp() {
		if (model.getCursor() == model.getLogEntries().size() - 5) {
			model.setOffset(model.getOffset() - model.getLimit());
			if (!model.isEndReached()) {
				loadLogEntries(false);
			}
		}
		String[] entry = model.getPreviousEntry();
		if (entry != null) {
			prependLogEntry(entry);
		}
	}

	private void scrollDown() {
		if (!locked) {
			refresh();
		}
	}

	public void onRefreshLog() {
		refresh();
	}

	public void onScroll() {
		ScrollPanel itemsContainer = view.getLogList().getItemsContainter();
		int maxPos = itemsContainer.getMaximumVerticalScrollPosition();
		int scrollPos = itemsContainer.getVerticalScrollPosition();
		if (scrollPos == 0) {
			scrollUp();
			maxPos = itemsContainer.getMaximumVerticalScrollPosition();
			itemsContainer.setVerticalScrollPosition(34);
		}

		if (scrollPos == maxPos) {
			scrollDown();
			maxPos = itemsContainer.getMaximumVerticalScrollPosition();
			itemsContainer.setVerticalScrollPosition(34);
		}
	}

}
