package havis.app.etb.input.rest;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import havis.app.etb.input.HistoryEntry;
import havis.app.etb.input.HistoryManager;
import havis.app.etb.input.HistoryManagerException;
import havis.net.rest.shared.data.SerializableValue;

@Path("webui/app/etb")
public class ETBInputService {

	private HistoryManager historyManager;

	public ETBInputService(HistoryManager historyManager) {
		this.historyManager = historyManager;
	}

	@PermitAll
	@GET
	@Path("state")
	@Produces({ MediaType.APPLICATION_JSON })
	public Map<String, String> getCurrentState() {
		return historyManager.getCurrentState();
	}

	@RolesAllowed("admin")
	@DELETE
	@Path("history")
	public void deleteHistory() throws HistoryManagerException {
		historyManager.clear();
	}

	@PermitAll
	@GET
	@Path("history")
	@Produces({ MediaType.APPLICATION_JSON })
	public SerializableValue<Integer> getHistoryEntryCount() throws HistoryManagerException {
		return new SerializableValue<Integer>(historyManager.size());
	}

	@PermitAll
	@GET
	@Path("history/{limit}/{offset}")
	@Produces({ MediaType.APPLICATION_JSON })
	public List<HistoryEntry> getHistoryEntries(@PathParam("limit") int limit, @PathParam("offset") int offset)
			throws HistoryManagerException {
		return historyManager.getEntries(limit, offset);
	}

	@PermitAll
	@GET
	@Path("export")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportHistory() throws HistoryManagerException {
		StringWriter writer = new StringWriter();
		try {
			historyManager.marshal(writer);
			String filename = String.format("History_%s.txt", new SimpleDateFormat("yyyyMMdd").format(new Date()));
			byte[] data = writer.toString().getBytes();
			return Response.ok(writer.toString(), MediaType.APPLICATION_OCTET_STREAM)
					.header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
					.header("Content-Type", "text/plain; charset=utf-8").header("Content-Length", data.length).build();
		} catch (SQLException | IOException e) {
			return Response.serverError().build();
		}
	}
}
