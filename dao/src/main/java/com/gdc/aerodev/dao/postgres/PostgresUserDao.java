package com.gdc.aerodev.dao.postgres;

import com.gdc.aerodev.dao.UserDao;
import com.gdc.aerodev.model.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Realization of data access object for working with {@code User} instance
 *
 * @author Yusupov Danil
 * @see User
 */
@Repository
public class PostgresUserDao extends AbstractDao<User, Long> implements UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final String TABLE_NAME;
    //TODO: make table_name static.
    private final String SELECT_QUERY = "SELECT usr_id, usr_name, usr_password, usr_email, usr_level FROM ";

    public PostgresUserDao(JdbcTemplate jdbcTemplate, String TABLE_NAME) {
        this.jdbcTemplate = jdbcTemplate;
        this.TABLE_NAME = TABLE_NAME;
    }

    @Override
    public User getById(Long id) {
        try {
            return jdbcTemplate.queryForObject(SELECT_QUERY + TABLE_NAME + " WHERE usr_id = ?;",
                    new UserRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }


    @Override
    public User getByName(String name) {
        try {
            return jdbcTemplate.queryForObject(SELECT_QUERY + TABLE_NAME + " WHERE usr_name = ?;",
                    new UserRowMapper(), name);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<User> getAll() {
        return jdbcTemplate.query(SELECT_QUERY + TABLE_NAME + ";", new UserRowMapper());
    }

    protected Long insert(User entity) {
        final String INSERT_SQL = "INSERT INTO " + TABLE_NAME + " (usr_name, usr_password, usr_email) VALUES (?, ?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement(INSERT_SQL, new String[]{"usr_id"});
                    ps.setString(1, entity.getUserName());
                    ps.setString(2, entity.getUserPassword());
                    ps.setString(3, entity.getUserEmail());
                    return ps;
                },
                keyHolder
        );
        return keyHolder.getKey().longValue();
    }

    protected Long update(User entity) {
        int rows = jdbcTemplate.update("UPDATE " + TABLE_NAME +
                        " SET usr_name=?, usr_password=?, usr_email=?, usr_level=? WHERE usr_id = "
                        + entity.getUserId() + ";",
                entity.getUserName(),
                entity.getUserPassword(),
                entity.getUserEmail(),
                entity.getUserLevel());
        return (rows > 0) ? entity.getUserId() : null;
    }


    @Override
    public boolean delete(Long id) {
        int rows = jdbcTemplate.update("DELETE FROM " + TABLE_NAME + " WHERE usr_id = ?;", id);
        return rows > 0;
    }

    @Override
    protected boolean isNew(User entity) {
        return entity.getUserId() == null;
    }

    @Override
    public int count() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + TABLE_NAME + ";", Integer.class);
    }

    public String existentEmail(String userEmail) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT usr_name FROM " + TABLE_NAME + " WHERE usr_email = ?;",
                    new Object[]{userEmail},
                    String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static class UserRowMapper implements RowMapper<User> {
        /**
         * Utility method, which builds {@code User} entity from inserted {@code ResultSet}
         *
         * @param resultSet incoming {@code ResultSet}
         * @return built {@code User} entity
         * @throws SQLException if build was performed incorrectly (see stacktrace)
         */
        @Override
        public User mapRow(ResultSet resultSet, int i) throws SQLException {
            User user = new User();
            user.setUserId(resultSet.getLong("usr_id"));
            user.setUserName(resultSet.getString("usr_name"));
            user.setUserPassword(resultSet.getString("usr_password"));
            user.setUserEmail(resultSet.getString("usr_email"));
            user.setUserLevel(resultSet.getShort("usr_level"));
            return user;
        }
    }
}
