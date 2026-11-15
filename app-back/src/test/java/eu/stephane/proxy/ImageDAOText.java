package eu.stephane.proxy;

import eu.stephane.camunda.AnimalType;
import eu.stephane.db.ImageDao;
import eu.stephane.db.ImageRecord;
import eu.stephane.db.JdbcImageDao;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

import java.time.Instant;
import java.util.UUID;

public class ImageDAOText {
    @Test
    void testInsert() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setServerNames(new String[]{"localhost"});
        ds.setPortNumbers(new int[]{4444});
        ds.setDatabaseName("camunda");
        ds.setUser("camunda");
        ds.setPassword("camunda");
        ds.setApplicationName("Christmas-herd");

        ImageDao dao = new JdbcImageDao(ds);
        final ImageRecord record = new ImageRecord(UUID.randomUUID(),
                Instant.now(), "default",
                AnimalType.CAT, new byte[0], 0);
        dao.insert(record);
    }
}
