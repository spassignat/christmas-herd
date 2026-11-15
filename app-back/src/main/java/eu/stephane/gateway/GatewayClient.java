package eu.stephane.gateway;

import eu.stephane.camunda.AnimalType;
import eu.stephane.camunda.JerseyHttpClient;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Client of nginx-gateway: one image of an animal per call on {@code <gatewayUrl>/proxy/<animal>}
 * (round robin + failover between the backends of this animal), or directly on an image-adapter resource
 * (local dev, see {@link eu.stephane.service.AppConfig#imageUrl()}).
 *
 * AI generated
 */
public class GatewayClient {

    /** header set by the gateway: address(es) of the upstream(s) tried, the last one answered */
    static final String UPSTREAM_HEADER = "X-Upstream";
    /** upstreams are unix sockets named after the resource: unix:/run/nginx/cat-cataas.sock */
    private static final Pattern SOCKET_NAME = Pattern.compile("([\\w-]+)\\.sock\\s*$");

    /** URL of an image, {@code {animal}} is replaced by the animal */
    private final String imageUrl;
    private final JerseyHttpClient client = new JerseyHttpClient();

    public GatewayClient(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /** image downloaded from the gateway; {@code backend} is the resource which served it */
    public record Image(String backend, byte[] content) {
    }

    /** the gateway has no backend for this animal (404) */
    public static class UnknownAnimalException extends RuntimeException {
        public UnknownAnimalException(AnimalType animalType) {
            super("no image backend for " + animalType);
        }
    }

    /** the gateway did not return an image (all backends offline, gateway unreachable...) */
    public static class GatewayException extends RuntimeException {
        public GatewayException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public Image fetch(AnimalType animalType) {
        String animal = animalType.name().toLowerCase(Locale.ROOT);
        URI uri = URI.create(imageUrl.replace("{animal}", animal));
        try {
            return client.get(uri, "image/*", response -> {
                if (response.getStatus() == 404) {
                    throw new UnknownAnimalException(animalType);
                }
                if (response.getStatus() != 200) {
                    throw new GatewayException("gateway answered " + response.getStatus() + " on " + uri, null);
                }
                return new Image(backend(response, uri), readAll(response));
            });
        } catch (UnknownAnimalException | GatewayException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new GatewayException("gateway call failed on " + uri + ": " + e.getMessage(), e);
        }
    }

    /** name of the backend: from the gateway header, else the last segment of the URL */
    private static String backend(Response response, URI uri) {
        String upstream = response.getHeaderString(UPSTREAM_HEADER);
        if (upstream != null) {
            Matcher m = SOCKET_NAME.matcher(upstream);
            if (m.find()) {
                return m.group(1);
            }
        }
        String path = uri.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private static byte[] readAll(Response response) {
        try (InputStream in = response.readEntity(InputStream.class)) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
