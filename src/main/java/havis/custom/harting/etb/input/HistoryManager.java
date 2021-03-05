package havis.custom.harting.etb.input;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvResultSetWriter;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CsvContext;

public class HistoryManager {

	private final static int ID = 1, TIMESTAMP = 2, EPC = 3, PIN_ID = 4, PIN_STATE = 5, OP_STATUS = 6;
	private final static String CLEAR = "DELETE FROM history";
	private final static String SIZE = "SELECT COUNT(id) FROM history";
	private final static String SELECT = "SELECT id, timestamp, epc, pin_id, pin_state, op_status FROM history ORDER BY id";
	private final static String INSERT = "INSERT INTO history (timestamp, epc, pin_id, pin_state, op_status) VALUES (?, ?, ?, ?, ?)";
	private final static String STRIP = "DELETE FROM history WHERE id <= ";

	// filled by EC, key=epc, value=state (e.g x8003);
	private Map<String, String> currentState;
	private Connection connection;

	private final static CellProcessor processor = new CellProcessor() {

		@SuppressWarnings("unchecked")
		@Override
		public String execute(Object value, CsvContext context) {
			if (value instanceof Clob) {
				Clob clob = (Clob) value;
				try {
					InputStream stream = clob.getAsciiStream();
					byte[] bytes = new byte[stream.available()];
					stream.read(bytes);
					return new String(bytes);
				} catch (Exception e) {
					// log.log(Level.FINE, "Failed to read column data", e);
				}
			}
			return null;
		}
	};

	public synchronized Map<String, String> getCurrentState() {
		return currentState == null ? new HashMap<String, String>() : currentState;
	}

	public synchronized void setCurrentState(Map<String, String> currentState) {
		this.currentState = currentState;
	}

	public HistoryManager() throws HistoryManagerException {
		try {
			connection = DriverManager.getConnection(Environment.JDBC_URL, Environment.JDBC_USERNAME,
					Environment.JDBC_PASSWORD);
		} catch (SQLException e) {
			throw new HistoryManagerException("Failed to get connection", e);
		}
	}

	public synchronized int clear() throws HistoryManagerException {
		try (Statement stmt = connection.createStatement()) {
			return stmt.executeUpdate(CLEAR);
		} catch (SQLException e) {
			throw new HistoryManagerException(e);
		}
	}

	public synchronized int size() throws HistoryManagerException {
		try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(SIZE)) {
			if (rs.next())
				return rs.getInt(1);
			return 0;
		} catch (SQLException e) {
			throw new HistoryManagerException(e);
		}
	}

	public synchronized void marshal(Writer writer) throws SQLException, IOException, HistoryManagerException {
		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(SELECT + " limit " + size() + " offset " + 0)) {
			ResultSetMetaData data = rs.getMetaData();
			CellProcessor[] processors = new CellProcessor[data.getColumnCount()];
			for (int i = 0; i < data.getColumnCount(); i++)
				if (data.getColumnType(i + 1) == Types.CLOB)
					processors[i] = processor;
			try (CsvResultSetWriter csv = new CsvResultSetWriter(writer, CsvPreference.EXCEL_PREFERENCE)) {
				csv.write(rs, processors);
				csv.flush();
			}
		}
	}

	public synchronized List<HistoryEntry> getEntries(int limit, int offset) throws HistoryManagerException {
		List<HistoryEntry> result = new ArrayList<>();

		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(SELECT + " limit " + limit + " offset " + offset)) {
			while (rs.next()) {
				HistoryEntry historyEntry = new HistoryEntry(rs.getInt(ID), rs.getTimestamp(TIMESTAMP).getTime(),
						rs.getString(EPC), rs.getInt(PIN_ID), (Boolean) rs.getObject(PIN_STATE),
						rs.getString(OP_STATUS));
				result.add(historyEntry);
			}
		} catch (SQLException e) {
			throw new HistoryManagerException(e);
		}
		return result;
	}

	private void strip(int min) throws SQLException {
		if (min > 0)
			try (Statement stmt = connection.createStatement()) {
				stmt.executeUpdate(STRIP + min);
			}
	}

	public synchronized void add(HistoryEntry entry) throws HistoryManagerException {
		try (PreparedStatement stmt = connection.prepareStatement(INSERT)) {
			stmt.setTimestamp(1, new Timestamp(entry.getTime()));
			stmt.setString(2, entry.getEpc());
			stmt.setInt(3, entry.getPinId());
			stmt.setObject(4, (Boolean) entry.getPinState());
			stmt.setString(5, entry.getOpStatus());
			stmt.execute();
			try (ResultSet rs = stmt.getGeneratedKeys()) {
				while (rs.next())
					strip(rs.getInt(1) - Environment.MAX_RECORD_COUNT);
			}
		} catch (SQLException e) {
			throw new HistoryManagerException(e);
		}
	}

	public synchronized void close() throws HistoryManagerException {
		try {
			connection.close();
		} catch (SQLException e) {
			throw new HistoryManagerException(e);
		}
	}
}