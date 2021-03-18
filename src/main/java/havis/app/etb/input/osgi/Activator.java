package havis.app.etb.input.osgi;

import havis.app.etb.input.Environment;
import havis.app.etb.input.HistoryManager;
import havis.app.etb.input.Main;
import havis.app.etb.input.rest.RESTApplication;
import havis.middleware.ale.base.exception.ALEException;
import havis.middleware.ale.config.service.mc.Path;
import havis.middleware.ale.service.mc.MC;
import havis.middleware.ale.service.mc.MCEventCycleSpec;
import havis.middleware.ale.service.mc.MCPortCycleSpec;
import havis.middleware.ale.service.mc.MCSubscriberSpec;
import havis.middleware.ale.service.mc.MCTagMemorySpec;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Application;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Activator implements BundleActivator {

	private final static Logger log = Logger.getLogger(Activator.class.getName());

	private final static String QUEUE_NAME = "name";
	// subscriber URI: queue://etb-ec
	private final static String EC_QUEUE_VALUE = "etb-ec";
	// subscriber URI: queue://etb-pc
	private final static String PC_QUEUE_VALUE = "etb-pc";

	private ServiceTracker<MC, MC> tracker;
	private ServiceRegistration<?> pcQueue;
	private ServiceRegistration<?> ecQueue;
	private ServiceRegistration<Application> restApp;
	Main main;

	@SuppressWarnings("serial")
	@Override
	public void start(BundleContext context) throws Exception {
		HistoryManager manager;

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(Activator.class.getClassLoader());
			manager = new HistoryManager();
		} finally {
			Thread.currentThread().setContextClassLoader(loader);
		}

		main = new Main(manager);
		pcQueue = context.registerService(Queue.class, main.getPCQueue(), new Hashtable<String, String>() {
			{
				put(QUEUE_NAME, PC_QUEUE_VALUE);
			}
		});
		ecQueue = context.registerService(Queue.class, main.getECQueue(), new Hashtable<String, String>() {
			{
				put(QUEUE_NAME, EC_QUEUE_VALUE);
			}
		});

		if (new File(Environment.LOCK).createNewFile())
			create(context);

		main.start();
		restApp = context.registerService(Application.class, new RESTApplication(manager), null);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (pcQueue != null) {
			pcQueue.unregister();
			pcQueue = null;
		}

		if (ecQueue != null) {
			ecQueue.unregister();
			ecQueue = null;
		}

		if (restApp != null) {
			restApp.unregister();
			restApp = null;
		}

		if (main != null) {
			main.stop();
			main = null;
		}
	}

	private void create(BundleContext context) throws IOException {

		tracker = new ServiceTracker<MC, MC>(context, MC.class, null) {
			@SuppressWarnings("unchecked")
			@Override
			public MC addingService(ServiceReference<MC> reference) {
				try {
					MC mc = super.addingService(reference);
					ObjectMapper mapper = new ObjectMapper();
					try {
						Thread.currentThread().setContextClassLoader(Activator.class.getClassLoader());
						for (java.nio.file.Path path : Files.newDirectoryStream(Paths.get(Environment.SPEC, "tm")))
							mc.add(Path.Service.TM.TagMemory, mapper.readValue(path.toFile(), MCTagMemorySpec.class));
						for (java.nio.file.Path path : Files.newDirectoryStream(Paths.get(Environment.SPEC, "ec"))) {
							Map<String, Object> map = mapper.readValue(path.toFile(), Map.class);
							String id = mc.add(Path.Service.EC.EventCycle, mapper.convertValue(map.get("eventCycle"), MCEventCycleSpec.class));
							for (MCSubscriberSpec spec : mapper.convertValue(map.get("subscribers"), MCSubscriberSpec[].class))
								mc.add(Path.Service.EC.Subscriber, spec, id);
						}
						for (java.nio.file.Path path : Files.newDirectoryStream(Paths.get(Environment.SPEC, "pc"))) {
							Map<String, Object> map = mapper.readValue(path.toFile(), Map.class);
							String id = mc.add(Path.Service.PC.PortCycle, mapper.convertValue(map.get("portCycle"), MCPortCycleSpec.class));
							for (MCSubscriberSpec spec : mapper.convertValue(map.get("subscribers"), MCSubscriberSpec[].class))
								mc.add(Path.Service.PC.Subscriber, spec, id);
						}
					} catch (IOException | ALEException e) {
						log.log(Level.SEVERE, "Failed to import spec", e);
					}
					return mc;
				} finally {
					tracker.close();
				}
			}
		};
		tracker.open();
	}
}