package havis.app.etb.input.ui.client.tagrow;

import com.google.gwt.user.client.ui.SimplePanel;

import havis.net.ui.shared.client.list.ComparableWidget;
import havis.net.ui.shared.resourcebundle.ResourceBundle;

public class LedPanel extends SimplePanel implements ComparableWidget<LedPanel> {

	private boolean on;
	ResourceBundle res = ResourceBundle.INSTANCE;
	
	public LedPanel() {
		
	}

	public LedPanel(boolean on) {
		set(on);
	}

	public void set(boolean on) {
		this.on = on;
		setStylePrimaryName(res.css().ledPanel());
		if (on) {
			addStyleName(res.css().ledOn());
		} else {
			removeStyleName(res.css().ledOn());
		}
//		setStyleName(res.css().ledOn(), on);
	}

	@Override
	public int compareTo(LedPanel ledPanel) {
		if (on == ledPanel.on) {
			return 0;
		} else {
			if (on) {
				return 1;
			} else {
				return -1;
			}
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LedPanel) {
			return on == ((LedPanel) obj).on;
		}
		return super.equals(obj);
	}
}
