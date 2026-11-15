package eu.stephane.service;

import java.net.URI;

public record AppConfig(URI gatewayUrl, String dataSourceName, int maxCount) {

    public static AppConfig fromEnvironment() {
        final URI gatewayUrl1 = URI.create(trimSlash(getEnv("GATEWAY_URL", "http://nginx-gateway")));
        final String datasourceJndi = getEnv("DATASOURCE_JNDI", "java:comp/env/jdbc/christmas");
        final int maxImagesPerRequest = Integer.parseInt(getEnv("MAX_IMAGES_PER_REQUEST", "20"));
        return new AppConfig(
                gatewayUrl1,
                datasourceJndi,
                maxImagesPerRequest
        );
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
