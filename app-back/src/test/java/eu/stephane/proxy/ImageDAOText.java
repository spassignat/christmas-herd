package eu.stephane.proxy;

import eu.stephane.camunda.AnimalType;
import eu.stephane.db.ImageDao;
import eu.stephane.db.ImageRecord;
import eu.stephane.db.JdbcImageDao;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImageDAOText {
    @Test
    void testInsert() {
        ImageDao dao = getDao();
        final ImageRecord record = new ImageRecord(UUID.randomUUID(),
                Instant.now(), "default",
                AnimalType.CAT, new byte[0], 0);
        dao.insert(record);
    }

    @Test
    void testInsertSelect() {
        ImageDao dao = getDao();
        final UUID uuid = UUID.randomUUID();
        final ImageRecord record = new ImageRecord(uuid,
                Instant.now(), "default",
                AnimalType.CAT, new byte[0], 0);
        dao.insert(record);
        final Optional<ImageRecord> byUuid = dao.findByUuid(uuid);
        assertTrue(byUuid.isPresent(),"Not Found");
    }

    private static @NonNull ImageDao getDao() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setServerNames(new String[]{"localhost"});
        ds.setPortNumbers(new int[]{4444});
        ds.setDatabaseName("camunda");
        ds.setUser("camunda");
        ds.setPassword("camunda");
        ds.setApplicationName("Christmas-herd");

        ImageDao dao = new JdbcImageDao(ds);
        return dao;
    }
}
