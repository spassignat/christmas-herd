package eu.stephane.db;


import eu.stephane.camunda.AnimalType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class JdbcImageDao implements ImageDao {
    private static final String COLUMNS = "uuid, imported_at, backend, animal_type, content, size";

    private static final String INSERT =
            "INSERT INTO image (" + COLUMNS + ")"
                    + " VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_UUID = "SELECT " + COLUMNS + " FROM image WHERE uuid = ?";

    private static final String SELECT_ALL = "SELECT " + COLUMNS + " FROM image ORDER BY imported_at, uuid";

    private static final String SELECT_LATEST = "SELECT " + COLUMNS + " FROM image"
            + " ORDER BY imported_at DESC, uuid DESC LIMIT 1";

    private static final String SELECT_LATEST_BY_ANIMAL = "SELECT " + COLUMNS + " FROM image WHERE animal_type = ?"
            + " ORDER BY imported_at DESC, uuid DESC LIMIT 1";

    private final DataSource dataSource;

    public JdbcImageDao(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    @Override
    public void insert(ImageRecord image) {
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(INSERT)) {
                bind(ps, image);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DaoException("insert failed " + image.uuid(), e);
        }
    }

    @Override
    public Optional<ImageRecord> findByUuid(UUID uuid) {
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(SELECT_BY_UUID)) {
                ps.setObject(1, uuid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    return Optional.of(read(rs));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("findByUuid failed " + uuid, e);
        }
    }

    @Override
    public List<ImageRecord> findAll() {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            List<ImageRecord> images = new ArrayList<>();
            while (rs.next()) {
                images.add(read(rs));
            }
            return images;
        } catch (SQLException e) {
            throw new DaoException("findAll failed ", e);
        }
    }

    @Override
    public Optional<ImageRecord> findLatest(AnimalType animalType) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(animalType == null ? SELECT_LATEST : SELECT_LATEST_BY_ANIMAL)) {
            if (animalType != null) {
                ps.setString(1, animalType.name());
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(read(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("findAll failed " + animalType.name(), e);
        }
    }

    private static ImageRecord read(ResultSet rs) throws SQLException {
        return new ImageRecord(
                rs.getObject("uuid", UUID.class),
                rs.getObject("imported_at", OffsetDateTime.class).toInstant(),
                rs.getString("backend"),
                animalType(rs.getString("animal_type")),
                rs.getBytes("content"),
                rs.getLong("size")
        );
    }

    /**
     * Animal of a row: {@code null} if unknown (old row, or value no longer in the enum).
     */
    private static AnimalType animalType(String value) {
        if (value == null) {
            return null;
        }
        try {
            return AnimalType.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static void bind(PreparedStatement ps, ImageRecord image) throws SQLException {
        ps.setObject(1, image.uuid());
        ps.setObject(2, OffsetDateTime.ofInstant(image.importedAt(), ZoneOffset.UTC));
        ps.setString(3, image.backend());
        ps.setString(4, image.animalType() == null ? null : image.animalType().name());
        ps.setBytes(5, image.content() == null ? null : image.content());
        ps.setLong(6, image.content() == null ? null : image.content().length);
    }
}
