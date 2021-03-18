package havis.app.etb.input.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.junit.Assert;
import org.junit.Test;

import havis.app.etb.input.HistoryEntry;
import havis.app.etb.input.HistoryManager;
import havis.app.etb.input.rest.ETBInputService;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Verifications;

public class ETBInputServiceTest {

	@Test
	public void testExportHistory(@Mocked final ResponseBuilder builder, @Mocked final HistoryManager manager, @Mocked final HistoryEntry entry) throws Exception {
		ETBInputService inputService = new ETBInputService(manager);

		final String EPC = "epc", OP_STATUS = "SUCCESS";
		final Integer PIN_ID = 42;
		final Boolean PIN_STATE = true;
		final long TIME = new Date().getTime();
		
		new MockUp<Response>() {

			@Mock
			ResponseBuilder ok(Object entity, String type) throws IOException {
				InputStream stream = (InputStream) entity;

				try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
					StringBuilder out = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						out.append(line);
					}
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss:S");
					StringBuilder sb = new StringBuilder();
					sb.append(simpleDateFormat.format(TIME)+ ", ");
					sb.append(EPC + ", ");
					sb.append(PIN_ID + ", ");
					sb.append(PIN_STATE + ", ");
					sb.append(OP_STATUS);
					
					Assert.assertEquals(sb.toString(), out.toString());
				}
				return builder;
			}
		};
	
		
		new Expectations() {
			{
				builder.header(anyString, any);
				result = builder;

				List<HistoryEntry> entries = new ArrayList<>();
				entries.add(entry);
				manager.getEntries(-1, 0);
				result = entries;

				entry.getTime();
				result = TIME;

				entry.getEpc();
				result = EPC;

				entry.getPinId();
				result = PIN_ID;

				entry.getPinState();
				result = PIN_STATE;

				entry.getOpStatus();
				result = OP_STATUS;
			}
		};

		inputService.exportHistory();

		new Verifications() {
			{
				String filename;

//				Response.ok(any, anyString);
//				times = 1;

				builder.header("Content-Disposition", filename = withCapture());
				times = 1;
				Assert.assertTrue(Pattern.matches("attachment; filename=\"History_\\d{4}\\d{2}\\d{2}\\.txt\"", filename));

			}
		};
	}
}
