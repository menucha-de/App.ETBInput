package havis.app.etb.input;

import static mockit.Deencapsulation.getField;
import static mockit.Deencapsulation.setField;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.junit.Assert;
import org.junit.Test;

import havis.app.etb.input.Environment;
import havis.app.etb.input.HistoryEntry;
import havis.app.etb.input.HistoryManager;
import havis.app.etb.input.HistoryManagerException;

public class HistoryManagerTest {

	@Test
	public void testGetCurrentState() throws HistoryManagerException {
		//tests if getCurrentState returns the correct currentState
		HistoryManager manager = new HistoryManager();
		HashMap<String, String> map = new HashMap<String, String>();
		
		Assert.assertEquals(map, manager.getCurrentState());
		
		map.put("urn:epc:raw:96.xAAA000000000000000000001", "x8000");
		setField(manager, "currentState", map);
		
		Assert.assertEquals(map, manager.getCurrentState());
	}

	@Test
	public void testSetCurrentState() throws HistoryManagerException {
		//tests if setCurrentState correctly changes the currentState
		HistoryManager manager = new HistoryManager();
		HashMap<String, String> map = new HashMap<String, String>();
		
		map.put("urn:epc:raw:96.xAAA000000000000000000001", "x8000");
		manager.setCurrentState(map);
		
		Assert.assertEquals(map, getField(manager, "currentState"));
	}

	@Test
	public void testHistoryManager(@Mocked DriverManager dm) throws SQLException {
		//tests the Constructor in case an Exception is thrown
		new NonStrictExpectations() {{
			DriverManager.getConnection(Environment.JDBC_URL, Environment.JDBC_USERNAME, Environment.JDBC_PASSWORD);
			result = new SQLException();
		}};
		
		try {
			@SuppressWarnings("unused")
			HistoryManager manager = new HistoryManager();
			Assert.fail();
		} catch (HistoryManagerException e) {
		}
	}

	@Test
	public void testGeneralFunction() throws HistoryManagerException {
		HistoryManager manager = new HistoryManager();
		
		Assert.assertEquals(0, manager.size());
		List<HistoryEntry> entries = new ArrayList<HistoryEntry>();
		//add new entries, check if size is returned correctly
		for(int i = 0; i < 5; i++) {
			manager.add(new HistoryEntry(new Date().getTime(), UUID.randomUUID().toString(), (int) (Math.random() * 10), i % 2 == 0, "SUCCESS"));
		}
		
		Assert.assertEquals(5, manager.size());
		
		//clear entries, check if size is 0
		manager.clear();
		Assert.assertEquals(0, manager.size());
		
		//add new entried, get all entries and check if it returns all correct entries
		for(int i = 0; i < 5; i++) {
			HistoryEntry entry = new HistoryEntry(new Date().getTime(), UUID.randomUUID().toString(), (int) (Math.random() * 10), i % 2 == 0, "SUCCESS");
			manager.add(entry);
			entry.setId(i + 6);
			entries.add(entry);
		}
		
		Assert.assertEquals(entries, manager.getEntries(-1, -1));
		
		//clean up entries, cut off everything before entry 7 (including 7), check if
		//size is correct
		Deencapsulation.invoke(manager, "strip", 7);
		
		Assert.assertEquals(3, manager.size());
	}

	@Test
	public void testExceptions(@Mocked final Connection connection, @Mocked DriverManager dm) throws SQLException, HistoryManagerException {
		//all cases in which SQLExceptions can come up are tested
		HistoryManager manager = new HistoryManager();
		new NonStrictExpectations() {{
			DriverManager.getConnection(Environment.JDBC_URL, Environment.JDBC_USERNAME, Environment.JDBC_PASSWORD);
			result = connection;
			
			connection.createStatement();
			result = new SQLException();
			
			connection.prepareStatement(anyString);
			result = new SQLException();
		}};
		
		try {
			manager.clear();
			Assert.fail("Expected HistoryManagerException from clear");
		}
		catch(HistoryManagerException e) {	
		}
		
		try {
			manager.size();
			Assert.fail("Expected HistoryManagerException from size");
		}
		
		catch(HistoryManagerException e) {	
		}
		
		try {
			manager.getEntries(1, 0);
			Assert.fail("Expected HistoryManagerException from getEntries");
		}
		
		catch(HistoryManagerException e) {	
		}
		
		try {
			HistoryEntry entry = new HistoryEntry(new Date().getTime(), UUID.randomUUID().toString(), (int) (Math.random() * 10), true, "SUCCESS");
			manager.add(entry);
			Assert.fail("Expected HistoryManagerException from add");
		}
		
		catch(HistoryManagerException e) {	
		}
	}

}
