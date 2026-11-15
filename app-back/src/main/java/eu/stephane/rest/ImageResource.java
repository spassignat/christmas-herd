package eu.stephane.rest;

import eu.stephane.db.ImageDao;
import eu.stephane.db.ImageRecord;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.List;
import java.util.UUID;

/**
 * Images stored in the database.
 */
@Path("images")
public class ImageResource {

    @Inject
    ImageDao dao;

    /**
     * row of the image table
     */
    public record ImageDto(String id, String createdAt, String source, String animalType, long size) {
        static ImageDto of(ImageRecord r) {
            return new ImageDto(r.uuid().toString(), r.importedAt().toString(), r.backend(),
                    r.animalType() == null ? null : r.animalType().name(), r.length());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ImageDto> list() {
        return dao.findAll().stream().map(ImageDto::of).toList();
    }

    @GET
    @Path("{id}/content")
    public Response content(@PathParam("id") String id) {
        ImageRecord image = dao.findByUuid(uuid(id))
                .orElseThrow(() -> new NotFoundException("image " + id + " not found"));
        return Response.ok(image.content(), contentType(image.content())).build();
    }

    private static UUID uuid(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("image " + id + " not found");
        }
    }

    /** the content type is not stored: guessed from the first bytes (jpeg, png, gif...) */
    private static String contentType(byte[] content) {
        try {
            String type = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(content));
            return type == null ? MediaType.APPLICATION_OCTET_STREAM : type;
        } catch (IOException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
