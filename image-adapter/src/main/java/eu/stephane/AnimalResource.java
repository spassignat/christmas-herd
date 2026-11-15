package eu.stephane;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public interface AnimalResource {
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    Response image();
}
