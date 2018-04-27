package com.gdc.aerodev.dao.postgres;

import com.gdc.aerodev.dao.GenericDao;
import com.gdc.aerodev.dao.exception.DaoException;
import com.gdc.aerodev.dao.logging.LoggingDao;
import org.springframework.dao.DuplicateKeyException;

/**
 * This interface details {@code GenericDao} for PostgreSQL implementation, especially method save(). <br>
 * It also uses logger from {@code LoggingDao}
 *
 * @param <T> is the type of entity object, for example: {@code User}, {@code Cr}, {@code Project} and so on.
 * @param <V> is the type of ID {@code Long}, {@code Integer} or {@code Short}.
 * @see com.gdc.aerodev.dao.GenericDao
 * @see com.gdc.aerodev.dao.logging.LoggingDao
 * @author Yusupov Danil
 */
interface Postgresqlable<T, V> extends GenericDao<T, V>, LoggingDao {
    /**
     * Method inherited from {@code GenericDao} & checks which method needs to be invoked. If {@code entityId} is
     * {@code null}, then {@code insert()} method will execute. If {@code entityId} isn't {@code null}, then
     * {@code update()} method will execute.
     *
     * @param entity {@code T} entity to save
     * @return id of saved or updated entity
     */
    @Override
    default V save(T entity) {
        if (isNew(entity)) {
            try {
                return insert(entity);
            } catch (DuplicateKeyException e) {
                throw new DaoException("Error inserting entity: ", e);
            }
        } else {
            return update(entity);
        }
    }

    /**
     * Inserts new entity with {@code entityId == null} into database.
     *
     * @param entity {@code T} entity to save
     * @return {@code V} id of inserted entity
     */
    V insert(T entity);

    /**
     * Updates entity with {@code entityId != null} into database.
     *
     * @param entity {@code T} entity to update
     * @return {@code V} id of updated entity
     */
    V update(T entity);

    /**
     * Checks nullable of entity's ID. This is necessary check for method 'save'.
     *
     * @param entity target to check
     * @return (1) {@code true} if ID of entity is {@code null}, (2) {@code false} if ID of entity is {@code not null}
     */
    boolean isNew(T entity);

}
