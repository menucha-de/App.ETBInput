package havis.custom.harting.etb.input.ui.client.tagrow;

import com.google.gwt.user.client.ui.Label;

import havis.net.ui.shared.client.list.ComparableWidget;

public class TagIDLabel extends Label implements ComparableWidget<TagIDLabel> {

	byte[] epc;

	public TagIDLabel() {
	}

	public TagIDLabel(byte[] epc) {
		this.epc = epc;
		String s = "";
		for (byte b : epc) {
			int i = b & 255;
			if (i <= 16)
				s += "0";
			s += Integer.toHexString(i).toUpperCase();
		}
		setText(s);
		setTitle(s);
	}

	public TagIDLabel(String epc) {
		setText(epc);
	}

	public String getEpcString() {
		return getText();
	}

	public byte[] getEpc() {
		return epc;
	}

	@Override
	public int compareTo(TagIDLabel tagID) {
		return getEpcString().compareTo(tagID.getEpcString());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TagIDLabel) {
			return getEpcString().equals(((TagIDLabel) obj).getEpcString());
		}
		return super.equals(obj);
	}
}
