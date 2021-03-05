package havis.custom.harting.etb.input;

import java.util.Date;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import havis.custom.harting.etb.input.HistoryEntry;

public class HistoryEntryTest {

	private final Integer id = 1;
	private final long time = new Date().getTime();
	private final String epc = UUID.randomUUID().toString();
	private final Integer pinId = 1;
	private final Boolean pinState = true;
	private final String opStatus = "SUCCESS";
		
	@Test
	public void test(){
		

		//test constructors, getters and setters
		HistoryEntry entry = new HistoryEntry(time, epc, pinId, pinState, opStatus);
		assertEqualsEntry(null, entry);

		entry = new HistoryEntry();
		entry.setId(id);
		entry.setTime(time);
		entry.setEpc(epc);
		entry.setPinId(pinId);
		entry.setPinState(pinState);
		entry.setOpStatus(opStatus);
		assertEqualsEntry(id, entry);

		entry = new HistoryEntry(id, time, epc, pinId, pinState, opStatus);
		assertEqualsEntry(id, entry);
	}
	
	public void assertEqualsEntry(Integer id, HistoryEntry entry){
		Assert.assertEquals(id, entry.getId());
		Assert.assertEquals(time, entry.getTime());
		Assert.assertEquals(epc, entry.getEpc());
		Assert.assertEquals(pinId, entry.getPinId());
		Assert.assertEquals(pinState, entry.getPinState());
		Assert.assertEquals(opStatus, entry.getOpStatus());	
	}

	@Test
	public void testEquals(){
		HistoryEntry entry1 = new HistoryEntry(id, time, epc, pinId, pinState, opStatus);
		HistoryEntry entry2 = new HistoryEntry(id, time, epc, pinId, pinState, opStatus);
		
		Assert.assertTrue(entry1.equals(entry2));
		
		entry1.setId(null);
		Assert.assertFalse(entry1.equals(entry2));

		entry1.setId(new Integer(1));
		Assert.assertTrue(entry1.equals(entry2));
		
		entry1.setEpc(null);
		Assert.assertFalse(entry1.equals(entry2));
		
		entry1.setEpc(new String(epc));
		Assert.assertTrue(entry1.equals(entry2));
		
		entry1.setPinId(null);
		Assert.assertFalse(entry1.equals(entry2));
		
		entry1.setPinId(new Integer(1));
		Assert.assertTrue(entry1.equals(entry2));
		
		entry1.setPinState(null);
		Assert.assertFalse(entry1.equals(entry2));
		
		entry1.setPinState(new Boolean(true));
		Assert.assertTrue(entry1.equals(entry2));
		
		entry1.setOpStatus(null);
		Assert.assertFalse(entry1.equals(entry2));
		
		entry1.setOpStatus(new String(opStatus));
		Assert.assertTrue(entry1.equals(entry2));
		
		Assert.assertFalse(entry1.equals(new Integer(1)));
	}
	
	@Test
	public void testToString() {
		long timestamp = 1451639410000L;
		HistoryEntry entry1 = new HistoryEntry(1, timestamp, "epcString", 1, true, "SUCCESS");
		Assert.assertEquals("{ 'id': 1, 'time': 'Fri Jan 01 10:10:10 CET 2016', 'epc': 'epcString', 'pinId': 1, 'pinState': 'true', 'opStatus': 'SUCCESS' }", entry1.toString());
	}
}