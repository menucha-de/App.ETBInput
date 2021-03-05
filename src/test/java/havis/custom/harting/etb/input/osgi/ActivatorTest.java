package havis.custom.harting.etb.input.osgi;

import havis.custom.harting.etb.input.HistoryManager;
import havis.custom.harting.etb.input.Main;
import havis.custom.harting.etb.input.osgi.Activator;
import havis.custom.harting.etb.input.rest.RESTApplication;
import havis.middleware.ale.service.ec.ECReports;
import havis.middleware.ale.service.pc.PCReports;

import java.util.Hashtable;
import java.util.Queue;

import javax.ws.rs.core.Application;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;

import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class ActivatorTest {

	@Tested
	Activator activator;
	@Mocked
	BundleContext context;
	@Mocked
	HistoryManager manager;

	@Mocked
	RESTApplication restApplication;
	@Mocked
	PCReports pcReports;
	@Mocked
	ECReports ecReports;

	@Test
	public void testStart(@Mocked final Queue<?> ecQueue, @Mocked final Queue<?> pcQueue) throws Exception {

		new MockUp<Main>() {

			@Mock
			void start(){				
			}
			@Mock
			Queue<?> getECQueue() {
				return ecQueue;
			}

			@Mock
			Queue<?> getPCQueue() {
				return pcQueue;
			}
		};
		activator.start(context);

		final Main main = Deencapsulation.getField(activator, "main");
		final HistoryManager manager = Deencapsulation.getField(main, "manager");

		new Verifications() {
			{

				final String QUEUE_NAME = "name";
				// subscriber URI: queue://etb-ec
				final String EC_QUEUE_VALUE = "etb-ec";
				// subscriber URI: queue://etb-pc
				final String PC_QUEUE_VALUE = "etb-pc";

				Hashtable<String, String> pcTable = new Hashtable<String, String>();
				Hashtable<String, String> ecTable = new Hashtable<String, String>();
				pcTable.put(QUEUE_NAME, PC_QUEUE_VALUE);
				ecTable.put(QUEUE_NAME, EC_QUEUE_VALUE);

				context.registerService(Application.class, new RESTApplication(withSameInstance(manager)), null);
				times = 1;

				context.registerService(Queue.class, ecQueue, ecTable);
				times = 1;

				context.registerService(Queue.class, pcQueue, pcTable);
				times = 1;
			}
		};
	}

	@Test
	public void testStop_variablesAreNull() {

		try {
			activator.stop(context);
		} catch (Exception e) {
			throw new NullPointerException("If a reference variable is already null, it must not be unregistered or stopped.");
		}
	}

	@Test
	public void testStop(@Mocked final Main main, @Mocked final ServiceRegistration<?> serviceRegistration) throws Exception {

		new Expectations() {
			{
				new Main(withAny(manager));
				result = main;
			}
		};

		activator.start(context);
		activator.stop(context);

		new Verifications() {
			{
				serviceRegistration.unregister();
				times = 3;

				main.stop();
				times = 1;
			}
		};
	}
}
