package eu.stephane.camunda;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jnh.connector.JavaNetHttpClientProperties;
import org.glassfish.jersey.jnh.connector.JavaNetHttpConnectorProvider;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.function.Function;

/**
 * a simple jersey client to call backend service.
 */
public class JerseyHttpClient {

    public static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration READ_TIMEOUT = Duration.ofSeconds(30);

    private final Client client;

    public JerseyHttpClient() {

        this.client = ClientBuilder.newClient(new ClientConfig()
                .connectorProvider(new JavaNetHttpConnectorProvider())
                .property(JavaNetHttpClientProperties.HTTP_VERSION, HttpClient.Version.HTTP_2)
                .property(ClientProperties.CONNECT_TIMEOUT, (int) CONNECT_TIMEOUT.toMillis())
                .property(ClientProperties.READ_TIMEOUT, (int) READ_TIMEOUT.toMillis()));
    }


    public void close() {
        client.close();
    }

    /**
     * GET on {@code uri}; the response is handed to {@code handler}, whatever its status, then closed:
     * the body must be read inside {@code handler}.
     *
     * @param accept Accept header, or {@code null}
     * @return the result of {@code handler}
     * @throws ProcessingException if the server cannot be reached or the exchange fails
     */
    public <T> T get(URI uri, String accept, Function<Response, T> handler) {
        Invocation.Builder builder = client.target(uri).request();
        if (accept != null) {
            builder.accept(accept);
        }
        try (Response response = builder.get()) {
            return handler.apply(response);
        }
    }

}
