package eu.stephane.proxy;

import eu.stephane.camunda.JerseyHttpClient;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests of the "cat" images.
 * <ul>
 *     <li>cat backends, called directly on tomcat-int (image-adapter):
 *     {@code BACKEND_URL}/&lt;backend&gt; (default {@code http://localhost:9080/image-adapter/api/images})</li>
 *     <li>cat proxy (nginx-gateway, round robin + failover):
 *     {@code PROXY_URL}/proxy/cat (default {@code http://localhost:9081})</li>
 * </ul>
 * URLs: env or system property. A test is skipped if its server is not reachable.
 */
class CatProxyIT {

    /** cat resources of image-adapter, behind the "cat" proxy */
    private static final List<String> CAT_BACKENDS = List.of("cataas", "placekitten");

    private static final JerseyHttpClient CLIENT = new JerseyHttpClient();
    private static String backendUrl;
    private static String proxyUrl;

    /** status + body size + content type of a call */
    private record Result(int status, String contentType, int size) {
        boolean isImage() {
            return status == 200 && size > 0 && contentType != null && contentType.startsWith("image/");
        }
    }

    @BeforeAll
    static void readUrls() {
        backendUrl = trimSlash(getEnv("BACKEND_URL", "http://localhost:9080/image-adapter/api/images"));
        proxyUrl = trimSlash(getEnv("PROXY_URL", "http://localhost:9081"));
    }

    @AfterAll
    static void closeClient() {
        CLIENT.close();
    }

    static List<String> catBackends() {
        return CAT_BACKENDS;
    }

    /**
     * Direct call of each cat backend on image-adapter: 200 + image if the final server is online,
     * 502 if it is offline. Any other status means the URL or the deployment is wrong.
     */
    @ParameterizedTest
    @MethodSource("catBackends")
    void directCallOfEachCatBackend(String backend) {
        String url = backendUrl + "/" + backend;
        Result result = callIfReachable(url);
        System.out.println(url + " -> " + result);
        switch (result.status()) {
            case 200 -> assertTrue(result.isImage(), "200 without image on " + url + ": " + result);
            case 502 -> { /* final server offline: expected without failover */ }
            default -> fail("unexpected status on " + url + ": " + result);
        }
    }

    /**
     * Round robin + failover: as long as one cat backend is online, every call on /proxy/cat
     * returns an image (the offline backends are hidden by the failover).
     */
    @Test
    void proxyCatReturnsAnImageWhenOneBackendIsOnline() {
        String url = proxyUrl + "/proxy/cat";
        callIfReachable(url);

        boolean oneOnline = CAT_BACKENDS.stream().anyMatch(b -> call(backendUrl + "/" + b).isImage());
        assumeTrue(oneOnline, "all cat backends are offline: failover cannot be tested");

        // more calls than backends: every backend is chosen at least once by the round robin
        for (int i = 0; i < CAT_BACKENDS.size() * 2; i++) {
            Result result = call(url);
            System.out.println(url + " #" + i + " -> " + result);
            assertTrue(result.isImage(), "call #" + i + " on " + url + ": " + result);
        }
    }

    /** calls {@code url}, skips the test if the server cannot be reached */
    private static Result callIfReachable(String url) {
        try {
            return call(url);
        } catch (Exception e) {
            assumeTrue(false, url + " not reachable: " + e.getMessage());
            throw e;
        }
    }

    private static Result call(String url) {
        return CLIENT.get(URI.create(url), "image/*", response -> {
            byte[] body = response.hasEntity() ? readAll(response) : new byte[0];
            return new Result(response.getStatus(), response.getHeaderString("Content-Type"), body.length);
        });
    }

    private static byte[] readAll(Response response) {
        try (InputStream in = response.readEntity(InputStream.class)) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String trimSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String getEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            value = System.getProperty(name);
        }
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
