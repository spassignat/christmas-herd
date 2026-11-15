package eu.stephane.service;

import eu.stephane.db.ImageDao;
import eu.stephane.db.JdbcImageDao;
import eu.stephane.gateway.GatewayClient;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Initialisation of the JAX-RS (Jersey) services, declared in WEB-INF/web.xml
 * (servlet "jersey", mapped on /api/*).
 * <p>
 * Every class annotated with {@code @Path} or {@code @Provider} under {@code eu.stephane}
 * is registered automatically. The DAO and the gateway client are injected ({@code @Inject}).
 */
public class RestApplication extends ResourceConfig {

    /** root package scanned (recursively) for REST resources and providers */
    static final String RESOURCES_PACKAGE = "eu.stephane";

    public RestApplication() {
        packages(true, RESOURCES_PACKAGE);

        AppConfig config = AppConfig.fromEnvironment();
        ImageDao dao = new JdbcImageDao(lookup(config.dataSourceName()));
        GatewayClient gateway = new GatewayClient(config.gatewayUrl());
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(config).to(AppConfig.class);
                bind(dao).to(ImageDao.class);
                bind(gateway).to(GatewayClient.class);
            }
        });
    }

    private static DataSource lookup(String name) {
        try {
            return (DataSource) new InitialContext().lookup(name);
        } catch (NamingException e) {
            throw new IllegalStateException("datasource " + name + " not found (tomcat conf/context.xml)", e);
        }
    }
}
