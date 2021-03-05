package havis.custom.harting.etb.input.rest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Application;

import havis.custom.harting.etb.input.HistoryManager;
import havis.custom.harting.etb.input.rest.provider.HistoryManagerExceptionMapper;

public class RESTApplication extends Application {

	private final static String PROVIDERS = "javax.ws.rs.ext.Providers";

	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> empty = new HashSet<Class<?>>();
	private Map<String, Object> properties = new HashMap<>();

	public RESTApplication(HistoryManager historyManager) {
		singletons.add(new ETBInputService(historyManager));
		properties.put(PROVIDERS, new Class<?>[] { HistoryManagerExceptionMapper.class });
	}

	@Override
	public Set<Class<?>> getClasses() {
		return empty;
	}

	public Set<Object> getSingletons() {
		return singletons;
	}

	@Override
	public Map<String, Object> getProperties() {
		return properties;
	}
}