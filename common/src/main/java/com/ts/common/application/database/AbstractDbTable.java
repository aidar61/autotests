package com.ts.common.application.database;

import com.ts.common.entitites.BaseEntity;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * @author Dennis Tikhomirov
 */
@Getter
@Slf4j
public abstract class AbstractDbTable {
    public static final String SELECT_QUERY = "SELECT * FROM %s";
    public static final String SELECT_WHERE_QUERY = SELECT_QUERY + " WHERE %s=%s";
    public static final String SELECT_WHERE_ID = SELECT_QUERY + " WHERE id=%s";

    protected JdbcTemplate template;
    protected String name;

    public AbstractDbTable(String name) {
        this.name = name;
    }

    public AbstractDbTable(String name, JdbcTemplate template) {
        this.name = name;
        this.template = template;
    }

    protected AbstractDbTable query(String sql) {
        try {
            log.info("SQL query: " + sql);
            this.template.query(sql, rs -> this);
        } catch (DataAccessException e) {
            log.error("Can not find an object in DB");
        }
        return this;
    }

    protected <T> List<T> query(String sql, BeanPropertyRowMapper<T> mapper) {
        try {
            log.info("SQL query: " + sql);
            return this.template.query(sql, mapper);
        } catch (DataAccessException e) {
            log.error("Can not find an object in DB, {}", e.toString());
        }
        return null;
    }

    protected <T> T queryForObject(String sql, BeanPropertyRowMapper<T> mapper) {
        try {
            log.info("SQL query: " + sql);
            return this.template.queryForObject(sql, mapper);
        } catch (DataAccessException e) {
            log.error("Can not find an object in DB");
        }
        return null;
    }

    public List<BaseEntity> receiveEntities(Class clazz) {
        return query(String.format(SELECT_QUERY, this.name), new BeanPropertyRowMapper<>(clazz));
    }

    public <T extends BaseEntity> T getEntityWhere(Class clazz, String... parameters) {
        return (T) queryForObject(String.format(SELECT_WHERE_QUERY, this.name, parameters[0], parameters[1]), new BeanPropertyRowMapper<>(clazz));
    }

}
