package havis.custom.harting.etb.input;

import havis.custom.harting.etb.input.HistoryManager;
import havis.custom.harting.etb.input.Main;
import havis.middleware.ale.service.EPC;
import havis.middleware.ale.service.ec.ECReport;
import havis.middleware.ale.service.ec.ECReportGroup;
import havis.middleware.ale.service.ec.ECReportGroupList;
import havis.middleware.ale.service.ec.ECReportGroupListMember;
import havis.middleware.ale.service.ec.ECReportGroupListMemberExtension;
import havis.middleware.ale.service.ec.ECReportMemberField;
import havis.middleware.ale.service.ec.ECReports;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MainECThreadTest {

	@Tested(availableDuringSetup = true)
	Main main;
	@Injectable
	HistoryManager manager;

	@Mocked
	ECReports ecReports;

	private Queue<ECReports> ecQueue;
	
	private Thread thread;

	private void addToQueue() {
		ecQueue.add(ecReports);
		try {
			while (!ecQueue.isEmpty())
				Thread.sleep(50);
		} catch (InterruptedException e) {
		}
	}
	
	private void invokeEvaluate() {
		Deencapsulation.invoke(thread, "evaluate", ecReports);
	}

	@Before
	public void setUp() {
		ecQueue = main.getECQueue();
		main.start();
		thread = Deencapsulation.getField(main, "ecThread");
	}

	@After
	public void tearDown() {
		main.stop();
	}

	@Test
	public void run_nonEmtpyQueue_evaluateInvoked() {

		final Thread thread = Deencapsulation.getField(main, "ecThread");
		addToQueue();

		new Verifications() {
			{
				Deencapsulation.invoke(thread, "evaluate", ecReports);
				times = 1;
			}
		};
	}

	@Test
	public void run_evaluateThrowsException_exceptionCaught(@Mocked final Logger log) {

		final Thread thread = Deencapsulation.getField(main, "ecThread");
		final Throwable e = new Throwable();

		new Expectations(thread) {
			{
				Deencapsulation.invoke(thread, "evaluate", ecReports);
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
	public void testEvaluate(@Mocked final ECReport ecReport, @Mocked final ECReportGroup group, @Mocked final ECReports.Reports ecReportsReports,
			@Mocked final ECReportGroupListMember member, @Mocked final ECReportGroupList groupList, @Mocked final EPC epc,
			@Mocked final ECReportGroupListMemberExtension extension, @Mocked final ECReportGroupListMemberExtension.FieldList fieldList,
			@Mocked final ECReportMemberField field) {

		final String FIELD_VALUE = "test", EPC_VALUE = "uuid";

		// reports.getReports() is null, manager.setCurrentState
		// should be executed without adding any new values

		new Expectations() {
			{
				/* verifies that no exceptions are thrown at any time evaluate()
				 * is invoked (doesn't check the last time, which has it's own Verifications block) */

				ecReports.getReports();
				result = null;
			}
		};
		
		invokeEvaluate();
		
		new Verifications() {{
			manager.setCurrentState(new HashMap<String, String>()); 
			times = 1;
		}};

		// group.getGroupList() is null, manager.setCurrentState
		// should be executed without adding any new values
		new Expectations() {
			{
				ecReports.getReports();
				result = ecReportsReports;

				ecReportsReports.getReport();
				result = ecReport;

				ecReport.getGroup();
				result = group;

				group.getGroupList();
				result = null;
			}
		};
		
		invokeEvaluate();
		
		new Verifications() {{
			manager.setCurrentState(new HashMap<String, String>()); 
			times = 1;
		}};

		// member.getEpc() is null,...
		new Expectations() {
			{
				group.getGroupList();
				result = groupList;

				groupList.getMember();
				result = member;

				member.getEpc();
				result = null;
			}
		};
		
		invokeEvaluate();
		
		new Verifications() {{
			manager.setCurrentState(new HashMap<String, String>()); 
			times = 1;
			
			member.getEpc().getValue();
			times = 0;
		}};

		// member.getExtension() is null,..
		new Expectations() {
			{
				member.getEpc();
				result = epc;

				epc.getValue();
				result = EPC_VALUE;

				member.getExtension();
				result = null;
			}
		};
		
		invokeEvaluate();
		
		new Verifications() {{
			manager.setCurrentState(new HashMap<String, String>()); 
			times = 1;
		}};

		// member.getExtension().getFieldList() is null,..
		new Expectations() {
			{
				member.getExtension();
				result = extension;

				extension.getFieldList();
				result = null;
			}
		};
		
		invokeEvaluate();
		
		new Verifications() {{
			manager.setCurrentState(new HashMap<String, String>()); 
			times = 1;
		}};

		// case is not "State",...
		new Expectations() {
			{
				extension.getFieldList();
				result = fieldList;

				fieldList.getField();
				result = field;

				field.getName();
				result = "notState";
			}
		};
		
		invokeEvaluate();
		
		new Verifications() {{
			HashMap<String, String> currentState = new HashMap<String, String>();
			manager.setCurrentState(currentState = withCapture()); 
			times = 1;
			
			Assert.assertEquals("currentState should be empty", 0, currentState.size());
		}};

		// field.getValue() is null,...
		new Expectations() {
			{
				field.getName();
				result = "State";

				field.getValue();
				result = null;
			}
		};
		
		invokeEvaluate();
		
		new Verifications() {{
			HashMap<String, String> currentState = new HashMap<String, String>();
			manager.setCurrentState(currentState = withCapture()); 
			times = 1;
			
			Assert.assertEquals("currentState should be empty", 0, currentState.size());
		}};

		// complete report, a new value should be added to manager's
		// currentState
		new Expectations() {
			{
				field.getValue();
				result = FIELD_VALUE;
			}
		};

		invokeEvaluate();

		new Verifications() {
			{
				Map<String, String> currentState;
				manager.setCurrentState(currentState = withCapture());
				times = 1;
				
				Assert.assertEquals("currentState should contain a value", FIELD_VALUE, currentState.get(EPC_VALUE));
			}
		};
	}
}