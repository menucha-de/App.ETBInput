package havis.custom.harting.etb.input;

import havis.middleware.ale.service.ec.ECReport;
import havis.middleware.ale.service.ec.ECReportGroup;
import havis.middleware.ale.service.ec.ECReportGroupListMember;
import havis.middleware.ale.service.ec.ECReportMemberField;
import havis.middleware.ale.service.ec.ECReports;
import havis.middleware.ale.service.pc.PCEventReport;
import havis.middleware.ale.service.pc.PCOpReport;
import havis.middleware.ale.service.pc.PCReport;
import havis.middleware.ale.service.pc.PCReports;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

	private final static Logger log = Logger.getLogger(Main.class.getName());

	private ECThread ecThread;
	private PCThread pcThread;

	private HistoryManager manager;

	private final BlockingQueue<PCReports> pcQueue = new LinkedBlockingQueue<>();
	private final BlockingQueue<ECReports> ecQueue = new LinkedBlockingQueue<>();

	// key = "<epc>:<pinId>"
	private Map<String, HistoryEntry> lastState = new HashMap<>();

	public Main(HistoryManager manager) {
		this.manager = manager;
	}

	public Queue<PCReports> getPCQueue() {
		return pcQueue;
	}

	public Queue<ECReports> getECQueue() {
		return ecQueue;
	}

	private class ECThread extends Thread {

		private boolean running = true;

		ECThread() {
			super("EC-Thread");
		}

		private void evaluate(ECReports reports) {
			Map<String, String> currentState = new HashMap<>();

			if (reports.getReports() != null) {
				for (ECReport ecReport : reports.getReports().getReport()) {
					for (ECReportGroup group : ecReport.getGroup()) {
						if (group.getGroupList() != null) {
							member: for (ECReportGroupListMember member : group.getGroupList().getMember()) {
								if (member.getEpc() != null) {
									String epc = member.getEpc().getValue();
									if (member.getExtension() != null && member.getExtension().getFieldList() != null) {
										for (ECReportMemberField field : member.getExtension().getFieldList().getField()) {
											switch (field.getName()) {
											case "State":
												if (field.getValue() != null)
													currentState.put(epc, field.getValue());
												continue member;
											}
										}
									}
								}
							}
						}
					}
				}
			}

			manager.setCurrentState(currentState);
			log.log(Level.FINEST, "Set current state: {0}", currentState);
		}

		@Override
		public void run() {
			try {
				while (running) {
					ECReports reports = ecQueue.poll(100, TimeUnit.MILLISECONDS);

					if (reports != null) {
						try {
							evaluate(reports);
						} catch (Throwable e) {
							log.log(Level.FINE, "Failed to evaluate EC reports", e);
						}
					}
				}
			} catch (InterruptedException e) {
			}
		}
	}

	private class PCThread extends Thread {

		private boolean running = true;

		PCThread() {
			super("PC-Thread");
		}

		private void evaluate(PCReports reports) throws HistoryManagerException {
			if (reports.getReports() != null && reports.getReports().getReport().size() == 4) {
				for (PCReport pcReport : reports.getReports().getReport()) {
					if (pcReport.getEventReports() != null && pcReport.getEventReports().getEventReport().size() == 1) {
						for (PCEventReport pcEventReport : pcReport.getEventReports().getEventReport()) {
							String epc = pcEventReport.getId();
							if (pcEventReport.getOpReports() != null && pcEventReport.getOpReports().getOpReport().size() == 1) {
								for (PCOpReport pcOpReport : pcEventReport.getOpReports().getOpReport()) {
									// 0101 =>
									// pin_id(hex)|pin_state(hex)
									String name = pcOpReport.getOpName();

									Integer pinId = Integer.parseInt(name.substring(0, 2), 16);
									boolean pinState = Integer.parseInt(name.substring(2), 16) != 0;

									String opStatus = pcOpReport.getOpStatus();

									HistoryEntry lastEntry = lastState.get(epc + ":" + pinId);

									if (opStatus != null) {
										if (opStatus.equals("SUCCESS")) {
											if (lastEntry == null || lastEntry.getPinState() == null || lastEntry.getPinState().booleanValue() != pinState) {
												HistoryEntry entry = new HistoryEntry(new Date().getTime(), epc, pinId, pinState, opStatus);
												manager.add(entry);
												lastState.put(epc + ":" + pinId, entry);
												log.log(Level.FINEST, "Written history entry: {0}", entry);
											}
										} else {
											if (lastEntry == null || lastEntry.getPinState() != null || !opStatus.equals(lastEntry.getOpStatus())) {
												HistoryEntry entry = new HistoryEntry(new Date().getTime(), epc, pinId, null, opStatus);
												manager.add(entry);
												lastState.put(epc + ":" + pinId, entry);
												log.log(Level.FINEST, "Written history entry: {0}", entry);
											}
										}
									} else
										log.log(Level.FINE, "Received unexpected PC event operation status");
								}
							} else
								log.log(Level.FINE, "Received unexpected size of PC event operation reports");
						}
					} else
						log.log(Level.FINE, "Received no or unexpected size of PC event reports");
				}
			} else
				log.log(Level.FINE, "Received no or unexpected size of PC reports");
		}

		@Override
		public void run() {
			try {
				while (running) {
					PCReports reports = pcQueue.poll(100, TimeUnit.MILLISECONDS);

					if (reports != null) {
						try {
							evaluate(reports);
						} catch (Throwable e) {
							log.log(Level.FINE, "Failed to evaluate PC reports", e);
						}
					}

				}
			} catch (InterruptedException e) {
			}
		}
	}

	public synchronized void start() {

		if (ecThread == null) {
			ecThread = new ECThread();
			ecThread.start();
		}

		if (pcThread == null) {
			pcThread = new PCThread();
			pcThread.start();
		}
	}

	public synchronized void stop() {

		if (ecThread != null) {
			ecThread.running = false;
			try {
				ecThread.join();
			} catch (InterruptedException e) {
			} finally {
				ecThread = null;
			}
		}

		if (pcThread != null) {
			pcThread.running = false;
			try {
				pcThread.join();
			} catch (InterruptedException e) {
			} finally {
				pcThread = null;
			}
		}

		if (manager != null)
			try {
				manager.close();
			} catch (Throwable e) {
				log.log(Level.FINE, "Failed to close manager", e);
			}
	}
}