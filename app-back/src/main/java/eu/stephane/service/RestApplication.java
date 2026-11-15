package eu.stephane.service;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * Initialisation of the JAX-RS (Jersey) services, declared in WEB-INF/web.xml
 * (servlet "jersey", mapped on /api/*).
 * <p>
 * Every class annotated with {@code @Path} or {@code @Provider} under {@code eu.stephane}
 * is registered automatically.
 */
public class RestApplication extends ResourceConfig {

    /** root package scanned (recursively) for REST resources and providers */
    static final String RESOURCES_PACKAGE = "eu.stephane";

    public RestApplication() {
        packages(true, RESOURCES_PACKAGE);
    }
}
