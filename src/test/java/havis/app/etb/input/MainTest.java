package havis.app.etb.input;

import mockit.Deencapsulation;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

import havis.app.etb.input.HistoryManager;
import havis.app.etb.input.Main;

public class MainTest {
	
	@Tested Main main;
	@Injectable HistoryManager manager;
	
	@Test
	public void start_threadsAreNull_threadsStarted(){
		main.start();
		Thread ecThread = Deencapsulation.getField(main, "ecThread");
		Thread pcThread = Deencapsulation.getField(main, "pcThread");
		
		Assert.assertTrue(ecThread.isAlive());
		Assert.assertTrue(pcThread.isAlive());
		
		main.stop();
	}
	
	@Test
	public void stop_runningThreads_threadsStopped() {
		main.start();
		Thread ecThread = Deencapsulation.getField(main, "ecThread");
		Thread pcThread = Deencapsulation.getField(main, "pcThread");
		main.stop();
		boolean ecRunning = Deencapsulation.getField(ecThread, "running");
		boolean pcRunning = Deencapsulation.getField(pcThread, "running");
		
		Assert.assertFalse(ecRunning);
		Assert.assertFalse(pcRunning);
		
		Assert.assertFalse(ecThread.isAlive());
		Assert.assertFalse(pcThread.isAlive());
	}
	
	@Test
	public void stop_threadsAreNull_noExceptionThrown() {
		main.stop();
	}
}