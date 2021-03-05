package havis.custom.harting.etb.input;

import java.util.Date;

public class HistoryEntry {

	private Integer id;
	private long time;
	private String epc;
	private Integer pinId;
	private Boolean pinState;
	private String opStatus;

	HistoryEntry(Integer id, long time, String epc, Integer pinId, Boolean pinState, String opStatus) {
		this.id = id;
		this.time = time;
		this.epc = epc;
		this.pinId = pinId;
		this.pinState = pinState;
		this.opStatus = opStatus;
	}

	public HistoryEntry(long time, String epc, Integer pinId, Boolean pinState, String opStatus) {
		this(null, time, epc, pinId, pinState, opStatus);
	}

	public HistoryEntry() {

	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getEpc() {
		return epc;
	}

	public void setEpc(String epc) {
		this.epc = epc;
	}

	public Integer getPinId() {
		return pinId;
	}

	public void setPinId(Integer pinId) {
		this.pinId = pinId;
	}

	public Boolean getPinState() {
		return pinState;
	}

	public void setPinState(Boolean pinState) {
		this.pinState = pinState;
	}

	public String getOpStatus() {
		return opStatus;
	}

	public void setOpStatus(String opStatus) {
		this.opStatus = opStatus;
	}

	private boolean equals(HistoryEntry obj) {
		if (id == obj.id || id != null && id.equals(obj.id))
			if (time == obj.time)
				if (epc == obj.epc || epc != null && epc.equals(obj.epc))
					if (pinId == obj.pinId || pinId != null && pinId.equals(obj.pinId))
						if (pinState == obj.pinState || pinState != null && pinState.equals(obj.pinState))
							if (opStatus == obj.opStatus || opStatus != null && opStatus.equals(obj.opStatus))
								return true;
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof HistoryEntry ? equals((HistoryEntry) obj) : false;
	}

	@Override
	public String toString() {
		return "{ " + "'id': " + id + ", " + "'time': '" + new Date(time) + "', " + "'epc': '" + epc + "', " + "'pinId': " + pinId + ", " + "'pinState': '"
				+ pinState + "', " + "'opStatus': '" + opStatus + "' }";
	}
}