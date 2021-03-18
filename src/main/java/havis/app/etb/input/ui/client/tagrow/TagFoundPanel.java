package havis.app.etb.input.ui.client.tagrow;

import com.google.gwt.user.client.ui.SimplePanel;

import havis.net.ui.shared.client.list.ComparableWidget;
import havis.net.ui.shared.resourcebundle.ResourceBundle;

public class TagFoundPanel extends SimplePanel implements ComparableWidget<TagFoundPanel> {

	private boolean found;
	ResourceBundle res = ResourceBundle.INSTANCE;
	
	public TagFoundPanel() {
		
	}

	public TagFoundPanel(boolean found) {
		set(found);
	}

	public void set(boolean found) {
		this.found = found;
		setStylePrimaryName(res.css().tagFoundPanel());
		if (found) {
			removeStyleName(res.css().notFound());
			addStyleName(res.css().found());
		} else {
			removeStyleName(res.css().found());
			addStyleName(res.css().notFound());
		}
//		setStyleName(res.css().found(), found);
	}

	@Override
	public int compareTo(TagFoundPanel tagFound) {
		if (found == tagFound.found) {
			return 0;
		} else {
			if (found) {
				return 1;
			} else {
				return -1;
			}
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TagFoundPanel) {
			return found == ((TagFoundPanel) obj).found;
		}
		return super.equals(obj);
	}
}
