package havis.app.etb.input;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Environment {

	private final static Logger log = Logger.getLogger(Environment.class.getName());
	private final static Properties properties = new Properties();

	static {
		try (InputStream stream = Environment.class.getClassLoader().getResourceAsStream("Environment.properties")) {
			if (stream != null)
				properties.load(stream);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Failed to load environment properties", e);
		}
	}

	public static final String LOCK = properties.getProperty("havis.app.etb.lock", "conf/havis/app/etb/input/lock");
	public static final String SPEC = properties.getProperty("havis.app.etb.spec", "conf/havis/app/etb/input/spec");

	public static final String JDBC_URL = properties.getProperty("havis.app.etb.jdbcUrl",
			"jdbc:h2:./etb-input;INIT=RUNSCRIPT FROM 'conf/havis/app/etb/input/history.sql'");
	public static final String JDBC_DRIVER = properties.getProperty("havis.app.etb.jdbcDriver", "org.h2.Driver");
	public static final String JDBC_USERNAME = properties.getProperty("havis.app.etb.jdbcUsername", "sa");
	public static final String JDBC_PASSWORD = properties.getProperty("havis.app.etb.jdbcPassword", "");
	public static final int MAX_RECORD_COUNT = Integer.valueOf(properties.getProperty("havis.app.etb.maxRecordCount", "1000"));
}