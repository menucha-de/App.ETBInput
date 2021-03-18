package havis.app.etb.input;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ MainTest.class,
	MainPCThreadTest.class,
	MainECThreadTest.class,
	HistoryManagerTest.class,
	HistoryEntryTest.class
})
public class TestSuite {
}
