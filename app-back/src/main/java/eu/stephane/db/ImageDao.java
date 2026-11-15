package eu.stephane.db;


import eu.stephane.camunda.AnimalType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImageDao {

     void insert(ImageRecord image);


    Optional<ImageRecord> findByUuid(UUID uuid);

    List<ImageRecord> findAll();

    Optional<ImageRecord> findLatest(AnimalType animalType);
}
