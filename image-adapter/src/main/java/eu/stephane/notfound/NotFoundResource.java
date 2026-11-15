package eu.stephane.notfound;

import eu.stephane.AnimalResource;
import eu.stephane.camunda.JerseyHttpClient;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;


@Path("images/notfound")
public class NotFoundResource implements AnimalResource {

    private static final URI SOURCE = URI.create("https://animal.exygen.fr/cat");

    /**
     * Shared (thread-safe) client
     */
    private static final JerseyHttpClient CLIENT = new JerseyHttpClient();

    /**
     * Downloads an image from cataas and returns it as is.
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response image() {
        try {
            return CLIENT.get(SOURCE, "image/*", response -> {
                if (response.getStatus() != 200) {
                    throw error(Response.Status.BAD_GATEWAY,
                            "cataas a répondu " + response.getStatus() + " pour " + SOURCE);
                }
                String contentType = response.getHeaderString("Content-Type");
                try (InputStream body = response.readEntity(InputStream.class)) {
                    return Response.ok(body.readAllBytes(),
                            contentType == null ? MediaType.APPLICATION_OCTET_STREAM : contentType).build();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (Exception e) {
            throw error(Response.Status.BAD_GATEWAY, "Téléchargement impossible : " + e.getMessage());
        }
    }

    private static WebApplicationException error(Response.Status status, String message) {
        return new WebApplicationException(
                Response.status(status).type(MediaType.TEXT_PLAIN).entity(message).build());
    }
}
