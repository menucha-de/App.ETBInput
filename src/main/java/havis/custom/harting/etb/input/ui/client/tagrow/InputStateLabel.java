package havis.custom.harting.etb.input.ui.client.tagrow;

import com.google.gwt.user.client.ui.Label;

import havis.net.ui.shared.client.list.ComparableWidget;

public class InputStateLabel extends Label implements ComparableWidget<InputStateLabel> {

	public InputStateLabel(String text) {
		super(text);
	}

	@Override
	public int compareTo(InputStateLabel o) {
		return getText().compareTo(o.getText());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TagIDLabel) {
			return getText().equals(((InputStateLabel) obj).getText());
		}
		return super.equals(obj);
	}

}
