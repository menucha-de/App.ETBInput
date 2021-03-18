package havis.app.etb.input.ui.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;

import havis.app.etb.input.rest.async.ETBInputServiceAsync;
import havis.app.etb.input.ui.client.MonitorSectionView.Presenter;
import havis.app.etb.input.ui.client.tagrow.InputStateLabel;
import havis.app.etb.input.ui.client.tagrow.LedPanel;
import havis.app.etb.input.ui.client.tagrow.TagFoundPanel;
import havis.app.etb.input.ui.client.tagrow.TagIDLabel;

public class MonitorSectionPresenter implements Presenter {

	private ETBInputServiceAsync service = GWT.create(ETBInputServiceAsync.class);
	private MonitorSectionView view;
	private static final Widget[] WIDGET_TYPE = new Widget[] {};
	private HashMap<String, TagRow> tagsMap = new HashMap<String, TagRow>();
	private Timer timer;

	public MonitorSectionPresenter(MonitorSectionView view) {
		this.view = view;
		this.view.setPresenter(this);
		setListHeader();
	}

	private static class TagRow {
		private TagFoundPanel tagFoundPanel;
		private TagIDLabel tagIDLabel;
		private InputStateLabel inputState;
		private LedPanel pin1;
		private LedPanel pin2;

		public TagRow(Map.Entry<String, String> tagData) {
			if (tagData != null) {
				this.tagFoundPanel = new TagFoundPanel(true);
				this.tagIDLabel = new TagIDLabel(tagData.getKey());
				this.inputState = new InputStateLabel(tagData.getValue());
				boolean[] states = getStates(tagData.getValue());
				this.pin1 = new LedPanel(states[0]);
				this.pin2 = new LedPanel(states[1]);
			} else {
				this.tagFoundPanel = new TagFoundPanel(false);
				this.tagIDLabel = new TagIDLabel("ERROR");
				this.inputState = new InputStateLabel("ERROR");
			}
		}

		private boolean[] getStates(String state) {
			String st = state.substring(state.length() - 1);
			int dezState = Integer.parseInt(st);
			return new boolean[] { (dezState & 1) == 1, (dezState & 2) == 2 };
		}

		public void setFound(boolean found) {
			tagFoundPanel.set(found);
		}
		
		public void setInputState(String state) {
			inputState.setText(state);
			boolean[] states = getStates(state);
			this.pin1.set(states[0]);
			this.pin2.set(states[1]);
		}

		public void reset() {
			tagFoundPanel.set(false);
		}

		public Widget[] getWidgets() {
			ArrayList<Widget> widgets = new ArrayList<Widget>();
			widgets.add(tagFoundPanel);
			widgets.add(tagIDLabel);
			widgets.add(inputState);
			widgets.add(pin1);
			widgets.add(pin2);
			return widgets.toArray(WIDGET_TYPE);
		}
	}

	private void clearMonitor() {
		view.getCurrentCount().setText("0");
		view.getAbsoluteCount().setText("0");
		view.getTagsList().clear();
		tagsMap.clear();
	}

	private void setListHeader() {
		view.getTagsList().removeHeader();
		view.getTagsList().addHeaderCell("Found");
		view.getTagsList().addHeaderCell("EPC");
		view.getTagsList().addHeaderCell("Input State");
		view.getTagsList().addHeaderCell("Pin1");
		view.getTagsList().addHeaderCell("Pin2");
	}

	private void resetFound() {
		for (Entry<String, TagRow> entry : tagsMap.entrySet()) {
			entry.getValue().reset();
		}
	}

	private void addTags(Map<String, String> tags) {
		HashSet<String> epcs = new HashSet<String>();
		resetFound();
		if (tags != null) {
			for (Map.Entry<String, String> tagdata : tags.entrySet()) {
				TagRow row = tagsMap.get(tagdata.getKey());
				epcs.add(tagdata.getKey());

				if (row == null) {
					row = new TagRow(tagdata);
					view.getTagsList().addItem(row.getWidgets());
					tagsMap.put(tagdata.getKey(), row);
				} else {
					row.setFound(true);
					if (tagdata.getValue() != null && !tagdata.getValue().isEmpty()) {
						row.setInputState(tagdata.getValue());
					}
				}
			}
		}
		view.getCurrentCount().setText(String.valueOf(epcs.size()));
		view.getAbsoluteCount().setText(String.valueOf(tagsMap.size()));
	}

	@Override
	public void onMonitor() {
		if (view.getMonitorRunning().getValue()) {
			if (!view.getTagsList().isVisible()) {
				view.getExpanded().setValue(true, true);
			}
			clearMonitor();
			startTimer();
		} else {
			stopTimer();
		}
	}
	
	private void startTimer() {
		timer = new Timer() {
			public void run() {
				monitor();
			}
		};
		timer.scheduleRepeating(500);
	}

	private void monitor() {
		service.getCurrentState(new MethodCallback<Map<String, String>>() {

			@Override
			public void onSuccess(Method method, Map<String, String> response) {
				addTags(response);
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				// TODO Auto-generated method stub

			}
		});
	}

	private void stopTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onExport() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onToggleList() {
		view.getTagsList().setVisible(view.getExpanded().getValue());
	}

}
