package eu.stephane;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnimalResourceTest {

    private static Map<Boolean, List<String>> resourcesByOnline;

    @BeforeAll
    static void callAllImplementations() {
        List<AnimalResource> resources = ServiceLoader.load(AnimalResource.class, AnimalResource.class.getClassLoader())
                .stream()
                .map(ServiceLoader.Provider::get)
                .toList();
        assertFalse(resources.isEmpty(), "no AnimalResource implementation found by the ServiceLoader");
        resourcesByOnline = resources.stream()
                .collect(Collectors.partitioningBy(AnimalResourceTest::isOnline,
                        Collectors.mapping(r -> r.getClass().getSimpleName(), Collectors.toList())));
        System.out.println("online: " + resourcesByOnline.get(true) + ", offline: " + resourcesByOnline.get(false));
    }

    @Test
    void atLeastOneIsOnline() {
        assertFalse(resourcesByOnline.get(true).isEmpty(), "offline: " + resourcesByOnline.get(false));
    }

    @Test
    void atLeastOneIsOffline() {
        assertFalse(resourcesByOnline.get(false).isEmpty(), "online: " + resourcesByOnline.get(true));
    }

    private static boolean isOnline(AnimalResource resource) {
        try (Response response = resource.image()) {
            return response.getStatus() == 200 && response.hasEntity();
        } catch (WebApplicationException e) {
            return false;
        }
    }
}
