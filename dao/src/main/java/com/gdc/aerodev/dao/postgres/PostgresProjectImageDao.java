package com.gdc.aerodev.dao.postgres;

import com.gdc.aerodev.dao.ProjectImageDao;
import com.gdc.aerodev.model.ProjectImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.gdc.aerodev.dao.postgres.DaoMaintenance.getTableName;
import static com.gdc.aerodev.dao.postgres.DaoMaintenance.toByteArray;

/**
 * PostgreSQL realization of DAO for {@code ProjectImage}
 *
 * @author Yusupov Danil
 * @see ProjectImage
 * @see com.gdc.aerodev.model.Project
 */
@Repository
public class PostgresProjectImageDao implements ProjectImageDao {
    /**
     * Autowired on application run, but initialized evidently in test cases
     */
    private JdbcTemplate jdbcTemplate;

    /**
     * Gets from classpath:/db.properties as 'project.images.table' property. <br>
     * For test cases initializes evidently according to migration files
     */
    private String tableName;

    private final String SELECT_QUERY = "SELECT img_id, prj_id, prj_image, img_type FROM ";

    @Autowired
    public PostgresProjectImageDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = getTableName("project.images.table");
    }

    public PostgresProjectImageDao(JdbcTemplate jdbcTemplate, String tableName) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = tableName;
    }

    @Override
    public List<Long> getAll(Long projectId) {
        return jdbcTemplate.query(
                "SELECT img_id FROM " + tableName + " WHERE prj_id = ?;",
                (RowMapper<Long>) (rs, rowNum) -> rs.getLong("img_id")
                , projectId);
    }

    @Override
    public ProjectImage getById(Long id) {
        try {
            return jdbcTemplate.queryForObject(
                    SELECT_QUERY + tableName + " WHERE img_id=?;",
                    new ProjectImageRowMapper(),
                    id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Long save(ProjectImage entity) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO " + tableName + " (prj_id, prj_image, img_type) VALUES (?, ?, ?);", new String[]{"img_id"});
                    ps.setLong(1, entity.getProjectId());
                    log.info("Received new avatar with size: " + entity.getProjectImage().length + " bytes.");
                    ps.setBinaryStream(2, new ByteArrayInputStream(entity.getProjectImage()));
                    ps.setString(3, entity.getContentType());
                    return ps;
                },
                keyHolder
        );
        long id = keyHolder.getKey().longValue();
        log.info("Inserted project image with id " + id + ". Project id: " + entity.getProjectId() + ".");
        return id;
    }

    @Override
    public boolean delete(Long imageId) {
        int rows = jdbcTemplate.update("DELETE FROM " + tableName + " WHERE img_id = ?;", imageId);
        return rows > 0;
    }

    private static class ProjectImageRowMapper implements RowMapper<ProjectImage> {
        /**
         * Utility method, which builds {@code ProjectImage} entity from inserted {@code ResultSet}
         *
         * @param rs incoming {@code ResultSet}
         * @return built {@code ProjectImage} entity
         * @throws SQLException if build was performed incorrectly (see stacktrace)
         */
        @Override
        public ProjectImage mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ProjectImage(
                    rs.getLong("img_id"),
                    rs.getLong("prj_id"),
                    toByteArray(rs.getBinaryStream("prj_image")),
                    rs.getString("img_type")
            );
        }
    }
}
