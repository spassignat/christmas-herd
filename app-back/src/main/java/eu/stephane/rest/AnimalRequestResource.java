package eu.stephane.rest;

import eu.stephane.camunda.AnimalType;
import eu.stephane.db.ImageDao;
import eu.stephane.db.ImageRecord;
import eu.stephane.gateway.GatewayClient;
import eu.stephane.service.AppConfig;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Request of images of an animal. Download and store in the database.
 */
@Path("animals/requests")
public class AnimalRequestResource {

    @Inject
    ImageDao dao;
    @Inject
    GatewayClient gateway;
    @Inject
    AppConfig config;

    public record AnimalRequest(AnimalType animalType, Integer count) {
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<ImageResource.ImageDto> request(AnimalRequest request) {
        validate(request);
        List<ImageResource.ImageDto> created = new ArrayList<>();
        for (int i = 0; i < request.count(); i++) {
            GatewayClient.Image image;
            try {
                image = gateway.fetch(request.animalType());
            } catch (GatewayClient.UnknownAnimalException e) {
                throw error(Response.Status.BAD_REQUEST, e.getMessage());
            } catch (GatewayClient.GatewayException e) {
                throw error(Response.Status.BAD_GATEWAY,
                        e.getMessage() + " (" + created.size() + "/" + request.count() + " images stored)");
            }
            ImageRecord record = new ImageRecord(UUID.randomUUID(), Instant.now(), image.backend(),
                    request.animalType(), image.content(), image.content().length);
            dao.insert(record);
            created.add(ImageResource.ImageDto.of(record));
        }
        return created;
    }

    private void validate(AnimalRequest request) {
        if (request == null || request.animalType() == null) {
            throw error(Response.Status.BAD_REQUEST, "animalType is required");
        }
        if (request.count() == null || request.count() < 1 || request.count() > config.maxCount()) {
            throw error(Response.Status.BAD_REQUEST, "count must be between 1 and " + config.maxCount());
        }
    }

    private static WebApplicationException error(Response.Status status, String message) {
        return new WebApplicationException(
                Response.status(status).type(MediaType.TEXT_PLAIN).entity(message).build());
    }
}
