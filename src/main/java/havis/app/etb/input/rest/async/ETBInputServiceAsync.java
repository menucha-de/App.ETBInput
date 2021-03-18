package havis.app.etb.input.rest.async;

import java.util.List;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import havis.app.etb.input.HistoryEntry;
import havis.net.rest.shared.data.SerializableValue;

@Path("../rest/webui/app/etb")
public interface ETBInputServiceAsync extends RestService {
	
	@GET
	@Path("state")
	void getCurrentState(MethodCallback<Map<String, String>> callback);
	
	@DELETE
	@Path("history")
	void deleteHistory(MethodCallback<Void> callback);
	
	@GET
	@Path("history")
	void getHistoryCount(MethodCallback<SerializableValue<Integer>> callback);
	
	@GET
	@Path("history/{limit}/{offset}")
	void getHistoryEntries(@PathParam("limit") int limit, @PathParam("offset") int offset, MethodCallback<List<HistoryEntry>> callback);
}
