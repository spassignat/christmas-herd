package eu.stephane.service;

/**
 * @param imageUrl URL of an image of an animal, {@code {animal}} is replaced by the animal (cat, dog...).
 *                 {@code IMAGE_URL}, by default {@code <GATEWAY_URL>/proxy/{animal}} (nginx-gateway).
 *                 Without nginx (local dev), an image-adapter resource can be called directly:
 *                 {@code http://localhost:8080/image-adapter/api/images/cataas} (every animal is then a cat).
 */
public record AppConfig(String imageUrl, String dataSourceName, int maxCount) {

    public static AppConfig fromEnvironment() {
        final String gatewayUrl = trimSlash(getEnv("GATEWAY_URL", "http://nginx-gateway"));
        final String imageUrl = getEnv("IMAGE_URL", gatewayUrl + "/proxy/{animal}");
        final String datasourceJndi = getEnv("DATASOURCE_JNDI", "java:comp/env/jdbc/christmas");
        final int maxImagesPerRequest = Integer.parseInt(getEnv("MAX_IMAGES_PER_REQUEST", "20"));
        return new AppConfig(
                imageUrl,
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
