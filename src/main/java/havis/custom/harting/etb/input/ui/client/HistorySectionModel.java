package havis.custom.harting.etb.input.ui.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import com.google.gwt.i18n.client.DateTimeFormat;

import havis.custom.harting.etb.input.HistoryEntry;

public class HistorySectionModel {
	private static final int DEFAULT_LIMIT = 30;
	private static final int DEFAULT_PART = 9;

	private static final DateTimeFormat DATE = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT);
	private static final DateTimeFormat TIME = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.TIME_MEDIUM);

	private static final String[] FIELD_LABELS = new String[] { "Date", "Time", "EPC", "Pin ID", "Pin State", "Operation Status" };

	private int limit = DEFAULT_LIMIT;
	private boolean endReached = false;

	private int offset;
	private int cursor;

	private ArrayList<String[]> historyEntryList = new ArrayList<String[]>();

	public HistorySectionModel() {
		reset();
	}

	public void setLogEntries(List<HistoryEntry> logEntries) {
		for (ListIterator<HistoryEntry> iterator = logEntries.listIterator(logEntries.size()); iterator.hasPrevious();) {
			HistoryEntry historyEntry = iterator.previous();
			Date date = new Date(historyEntry.getTime());
			historyEntryList.add(new String[] { DATE.format(date), TIME.format(date), historyEntry.getEpc(), historyEntry.getPinId().toString(),
					historyEntry.getPinState() == null ? "" : historyEntry.getPinState().toString(), historyEntry.getOpStatus() });
		}

		if (offset == 0) {
			endReached = true;
			historyEntryList.add(new String[] { "", "", "", "", "", "" });
		}

	}

	public List<String[]> getLogEntries() {
		return historyEntryList;
	}

	public String[] getPreviousEntry() {
		if (cursor < historyEntryList.size()) {
			return historyEntryList.get(cursor++);
		}
		return null;
	}

	public static int getDefaultPart() {
		return DEFAULT_PART;
	}

	public static String[] getFieldLabels() {
		return FIELD_LABELS;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int currentOffset) {
		if (currentOffset < 0) {
			this.limit = currentOffset + DEFAULT_LIMIT;
			this.offset = 0;
		} else {
			this.limit = DEFAULT_LIMIT;
			this.offset = currentOffset;
		}
	}

	public int getCursor() {
		return cursor;
	}

	public void setCursor(int cursor) {
		this.cursor = cursor;
	}

	public int getLimit() {
		return limit;
	}

	public boolean isEndReached() {
		return endReached;
	}

	public int getInitialCount() {
		return historyEntryList.size() < DEFAULT_PART ? historyEntryList.size() : DEFAULT_PART;
	}

	public void reset() {
		this.limit = DEFAULT_LIMIT;
		this.historyEntryList.clear();
		this.cursor = 0;
		// this.offset = -1;
		this.endReached = false;
	}
}
