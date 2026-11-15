package eu.stephane.service;


import eu.stephane.camunda.JerseyHttpClient;

import java.net.URI;

public record AppConfig(URI backendUrl,

                        String imagesUrl,
                        String imageContentUrl) {


    public AppConfig {

    }

    public static AppConfig fromEnvironment() {
        final String imagesUrl1 = getEnv("IMAGES_URL", "IMAGES_URL not set");
        final String imageContentUrl1 = getEnv("IMAGE_CONTENT_URL", "IMAGE_CONTENT_URL not set");
        final String backendUrl1 = getEnv("BACKEND_URL", "BACKEND_URL not set");
        return new AppConfig(
                URI.create(backendUrl1),
                imagesUrl1,
                imageContentUrl1
        );
    }

    public JerseyHttpClient httpClient() {
        return new JerseyHttpClient();
    }

    private static String getEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            value = System.getProperty(name);
        }
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
