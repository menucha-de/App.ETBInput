package havis.app.etb.input.rest.provider;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import havis.app.etb.input.HistoryManagerException;
import havis.net.rest.shared.data.SerializableValue;

@Provider
public class HistoryManagerExceptionMapper implements
		ExceptionMapper<HistoryManagerException> {

	@Override
	public Response toResponse(HistoryManagerException ex) {
		return Response.status(Response.Status.BAD_REQUEST)
				.entity(new SerializableValue<String>(ex.getMessage()))
				.type(MediaType.APPLICATION_JSON).build();
	}

}
