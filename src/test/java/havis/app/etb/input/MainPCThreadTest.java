package havis.app.etb.input;

import havis.app.etb.input.HistoryEntry;
import havis.app.etb.input.HistoryManager;
import havis.app.etb.input.Main;
import havis.middleware.ale.service.pc.PCEventReport;

import havis.middleware.ale.service.pc.PCOpReport;
import havis.middleware.ale.service.pc.PCOpReports;
import havis.middleware.ale.service.pc.PCReport;
import havis.middleware.ale.service.pc.PCReports;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.Verifications;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MainPCThreadTest {

	@Tested(availableDuringSetup = true)
	Main main;
	
	@Injectable
	HistoryManager manager;

	@Mocked
	PCReports pcReports;
	
	private Queue<PCReports> pcQueue;
	
	private Thread thread;

	final String EPC = "testID", OP_NAME = "0101";

	private void addToQueue() {
		pcQueue.add(pcReports);
		try {
			while (!pcQueue.isEmpty())
				Thread.sleep(50);
		} catch (InterruptedException e) {
		}
	}
	
	private void invokeEvaluate() {
		Deencapsulation.invoke(thread, "evaluate", pcReports);
	}

	@Before
	public void setUp() {
		pcQueue = main.getPCQueue();
		main.start();
		thread = Deencapsulation.getField(main, "pcThread");
	}

	@After
	public void tearDown() {
		main.stop();
	}

	@Test
	public void run_evaluateThrowsException_exceptionCaught(@Mocked final Logger log) {

		final Throwable e = new Throwable();

		new Expectations(thread) {
			{
				Deencapsulation.invoke(thread, "evaluate", pcReports);
				result = e;
			}
		};

		addToQueue();

		new Verifications() {
			{
				log.log((Level) any, anyString, e);
				times = 1;
			}
		};
	}

	@Test
	public void run_nonEmtpyQueue_evaluateInvoked() {

		new Expectations(thread) {
		};

		addToQueue();

		new Verifications() {
			{
				Deencapsulation.invoke(thread, "evaluate", pcReports);
				times = 1;
			}
		};

	}

	@Test
	public void testEvaluate(@Mocked final PCReports.Reports pcReportsReports, @Mocked final PCReport pcReport,
			@Mocked final PCReport.EventReports pcReportEventReports, @Mocked final PCEventReport pcEventReport, @Mocked final PCOpReports opReports,
			@Mocked final PCOpReport pcOpReport, @Mocked final HistoryEntry lastEntry, @Mocked final Logger log) {
		
		standardExpectations(pcReportsReports, pcReport, pcReportEventReports, pcEventReport, opReports, pcOpReport);
		
		// reports.getReports() is null
		new Expectations(){
			{
				pcReports.getReports();
				result = null;
			}
		};
		
		invokeEvaluate();

		new Verifications() {{
			//verifies that no exceptions are thrown at any time evaluate() is invoked
			String logMessage;
			log.log((Level) any, logMessage = withCapture());
			times = 1;
			
			assertTrue(logMessage.matches("Received no or unexpected size of PC reports"));
		}};
		
		// reports.getReports().getReport().size() is not 4
		new Expectations() {
			{
				pcReports.getReports();
				result = pcReportsReports;

				pcReportsReports.getReport();
				result = new ArrayList<PCReport>();
			}
		};
		
		invokeEvaluate();
		
		new Verifications() {{
			String logMessage;
			log.log((Level) any, logMessage = withCapture());
			times = 1;
			
			assertTrue(logMessage.matches("Received no or unexpected size of PC reports"));
		}};

		// pcReport.getEventReports() is null
		new NonStrictExpectations() {
			{
				List<PCReport> reportList = new ArrayList<>();
				reportList.add(pcReport);
				reportList.add(pcReport);
				reportList.add(pcReport);
				reportList.add(pcReport);
				
				pcReportsReports.getReport();
				result = reportList;
				
				pcReport.getEventReports();
				result = null;
			}
		};
		
		invokeEvaluate();
		
		new Verifications() {{
			String logMessage;
			log.log((Level) any, logMessage = withCapture());
			times = 4;
			
			assertTrue(logMessage.matches("Received no or unexpected size of PC event reports"));
		}};

		// pcReport.getEventReports().getEventReport().size() is not 1
		new NonStrictExpectations() {
			{
				pcReport.getEventReports();
				result = pcReportEventReports;

				pcReportEventReports.getEventReport();
				result = new ArrayList<PCEventReport>();
			}
		};
		
		invokeEvaluate();
		
		new Verifications() {{
			String logMessage;
			log.log((Level) any, logMessage = withCapture());
			times = 4;
			
			assertTrue(logMessage.matches("Received no or unexpected size of PC event reports"));
		}};

		// pcEventReport.getOpReports() is null
		new NonStrictExpectations() {
			{
				pcReportEventReports.getEventReport();
				result = pcEventReport;

				pcEventReport.getId();
				result = EPC;

				pcEventReport.getOpReports();
				result = null;
			}
		};
		
		invokeEvaluate();
		
		new Verifications() {{
			String logMessage;
			log.log((Level) any, logMessage = withCapture());
			times = 4;
			
			assertTrue(logMessage.matches("Received unexpected size of PC event operation reports"));
		}};
		

		// pcEventReport.getOpReports().getOpReport().size() is not 1
		new Expectations() {
			{
				pcEventReport.getOpReports();
				result = opReports;

				opReports.getOpReport();
				result = new ArrayList<PCOpReport>();
			}
		};
		
		invokeEvaluate();
		
		new Verifications() {{
			String logMessage;
			log.log((Level) any, logMessage = withCapture());
			times = 4;
			
			assertTrue(logMessage.matches("Received unexpected size of PC event operation reports"));
		}};

		// opStatus is null
		new Expectations() {
			{
				opReports.getOpReport();
				result = pcOpReport;

				pcOpReport.getOpName();
				result = OP_NAME;

				pcOpReport.getOpStatus();
				result = null;
			}
		};
		
		invokeEvaluate();
		
		new Verifications() {{
			String logMessage;
			log.log((Level) any, logMessage = withCapture());
			times = 4;
			
			assertTrue(logMessage.matches("Received unexpected PC event operation status"));
		}};
	}
	
	@Test
	public void evaluate_opStatusSuccess(@Mocked final PCReports.Reports pcReportsReports, @Mocked final PCReport pcReport,
			@Mocked final PCReport.EventReports pcReportEventReports, @Mocked final PCEventReport pcEventReport, @Mocked final PCOpReports opReports,
			@Mocked final PCOpReport pcOpReport, @Mocked final HistoryEntry lastEntry) throws Exception {

		final boolean PIN_STATE = Integer.parseInt(OP_NAME.substring(2), 16) != 0;
		final Integer PIN_ID = Integer.parseInt(OP_NAME.substring(0, 2), 16);
		final String OP_STATUS_SUCCESS = "SUCCESS";

		standardExpectations(pcReportsReports, pcReport, pcReportEventReports, pcEventReport, opReports, pcOpReport);

		// during 1st iteration lastEntry == null is true, so one HistoryEntry should be created
		//other iterations do not match conditions 
		new Expectations() {
			{
				pcOpReport.getOpStatus();
				result = OP_STATUS_SUCCESS;

				lastEntry.getPinState();
				result = true;

				new HistoryEntry(anyLong, anyString, anyInt, anyBoolean, anyString);
				result = lastEntry;				
			}
		};
		
		invokeEvaluate();
		
		new Verifications() {{
			new HistoryEntry(anyLong, anyString, anyInt, anyBoolean, anyString);
			times = 1;			
		}};
		
		// only lastEntry.getPinState() == null is true in all iterations
		// Four HistoryEntry instances should be created
		new Expectations() {
			{
				lastEntry.getPinState();
				result = null;
			}
		};
		
		invokeEvaluate();
		
		new Verifications() {{
			new HistoryEntry(anyLong, anyString, anyInt, anyBoolean, anyString);
			times = 4;			
		}};
		

		// only lastEntry.getPinState().booleanValue() != pinState is true
		// Four HistoryEntry instances should be created
		new Expectations() {
			{
				lastEntry.getPinState();
				result = false;
			}
		};
		
		invokeEvaluate();
		
		new Verifications() {{
			new HistoryEntry(anyLong, anyString, anyInt, anyBoolean, anyString);
			times = 4;			
		}};

		// test if HistoryEntry instances are created with the right values,
		// manager.add() is invoked with the right HistoryEntry,
		// and lastState.put() is invoked correctly
		new Verifications() {
			{
				boolean pinState;
				String epc, opStatus;
				Integer pinId;

				new HistoryEntry(anyLong, epc = withCapture(), pinId = withCapture(), pinState = withCapture(),
						opStatus = withCapture());

				Assert.assertEquals("new HistoryEntry should be invoked with epc as 2nd argument", EPC, epc);
				Assert.assertEquals("new HistoryEntry should be invoked with pinId as 3rd argument", PIN_ID, pinId);
				Assert.assertEquals("new HistoryEntry should be invoked with pinState as 4th argument", PIN_STATE, pinState);
				Assert.assertEquals("new HistoryEntry should be invoked with opStatus as 5th argument", OP_STATUS_SUCCESS, opStatus);
				
				manager.add(this.<HistoryEntry>withNotNull());
				times = 4;	

				final Map<String, HistoryEntry> lastState = Deencapsulation.getField(main, "lastState");
				Assert.assertNotNull(lastState.get(epc + ":" + pinId));
			}
		};
	}

	@Test
	public void evaluate_opStatusNotSuccess(@Mocked final PCReports pcReports, @Mocked final PCReports.Reports pcReportsReports,
			@Mocked final PCReport pcReport, @Mocked final PCReport.EventReports pcReportEventReports, @Mocked final PCEventReport pcEventReport,
			@Mocked final PCOpReports opReports, @Mocked final PCOpReport pcOpReport, @Mocked final HistoryEntry lastEntry) throws Exception {

		final String OP_STATUS_NOT_SUCCESS = "abc", OP_STATUS_NOT_LAST = "xyz";
		final Integer PIN_ID = Integer.parseInt(OP_NAME.substring(0, 2), 16);

		standardExpectations(pcReportsReports, pcReport, pcReportEventReports, pcEventReport, opReports, pcOpReport);

		// during 1st iteration lastEntry == null is true, so one HistoryEntry should be created
		//other iterations do not match conditions 
		new Expectations() {
			{
				pcOpReport.getOpStatus();
				result = OP_STATUS_NOT_SUCCESS;

				lastEntry.getPinState();
				result = null;

				lastEntry.getOpStatus();
				result = OP_STATUS_NOT_SUCCESS;

				new HistoryEntry(anyLong, anyString, anyInt, null, anyString);
				result = lastEntry;
			}
		};
		
		invokeEvaluate();

		new Verifications() {{
			new HistoryEntry(anyLong, anyString, anyInt, anyBoolean, anyString);
			times = 1;			
		}};
		
		// only lastEntry.getPinState() != null is true
		// Four HistoryEntry instances should be created
		new Expectations() {
			{
				lastEntry.getPinState();
				result = true;
			}
		};
		
		invokeEvaluate();

		new Verifications() {{
			new HistoryEntry(anyLong, anyString, anyInt, anyBoolean, anyString);
			times = 4;			
		}};
		
		// only !opStatus.equals(lastEntry.getOpStatus()) is true
		// Four HistoryEntry instances should be created
		final Map<String, HistoryEntry> lastState = Deencapsulation.getField(main, "lastState");
		
		new Expectations(lastState) {
			{
				lastEntry.getPinState();
				result = null;

				lastEntry.getOpStatus();
				result = OP_STATUS_NOT_LAST;
			}
		};
		
		invokeEvaluate();

		// test if HistoryEntry instances are created with the right values,
		// manager.add() is invoked with the right HistoryEntry,
		// and lastState.put() is invoked correctly
		new Verifications() {
			{
				String epc, opStatus;
				Integer pinId;

				new HistoryEntry(anyLong, epc = withCapture(), pinId = withCapture(), (Boolean) withNull(), opStatus = withCapture());

				Assert.assertEquals("new HistoryEntry should be invoked with epc as 2nd argument", EPC, epc);
				Assert.assertEquals("new HistoryEntry should be invoked with pinId as 3rd argument", PIN_ID, pinId);
				Assert.assertEquals("new HistoryEntry should be invoked with opStatus as 5th argument", OP_STATUS_NOT_SUCCESS, opStatus);
				
				manager.add(this.<HistoryEntry>withNotNull());
				times = 4;	

				final Map<String, HistoryEntry> lastState = Deencapsulation.getField(main, "lastState");
				Assert.assertNotNull(lastState.get(epc + ":" + pinId));
			}
		};
	}

	// all results needed for evaluate() to be executed until if (opStatus != null)
	public void standardExpectations(@Mocked final PCReports.Reports pcReportsReports, @Mocked final PCReport pcReport,
			@Mocked final PCReport.EventReports pcReportEventReports, @Mocked final PCEventReport pcEventReport, @Mocked final PCOpReports opReports,
			@Mocked final PCOpReport pcOpReport) {

		new NonStrictExpectations() {
			{
				pcReports.getReports();
				result = pcReportsReports;

				List<PCReport> reportList = new ArrayList<>();
				reportList.add(pcReport);
				reportList.add(pcReport);
				reportList.add(pcReport);
				reportList.add(pcReport);
				
				pcReportsReports.getReport();
				result = reportList;

				pcReport.getEventReports();
				result = pcReportEventReports;

				pcReportEventReports.getEventReport();
				result = pcEventReport;

				pcEventReport.getId();
				result = EPC;

				pcEventReport.getOpReports();
				result = opReports;

				opReports.getOpReport();
				result = pcOpReport;

				pcOpReport.getOpName();
				result = OP_NAME;
			}
		};

	}
}