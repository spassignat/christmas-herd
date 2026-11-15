package eu.stephane.db;


import eu.stephane.camunda.AnimalType;

import java.time.Instant;
import java.util.UUID;

/**
 * Row of the image table.
 */
public record ImageRecord(UUID uuid,
                          Instant importedAt,
                          String backend,
                          AnimalType animalType,
                          byte[] content,long length) {

    public ImageRecord {

    }
}
