package com.ts.common.application.database;

import com.ts.common.entitites.BaseEntity;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;

import static com.ts.common.utils.RandomUtils.generateRandomNumberBetween;


/**
 * @author Aidar Askeev
 */
@Getter
@Slf4j
public abstract class AbstractDbTable {
    public static final String SELECT_QUERY = "SELECT * FROM %s";
    public static final String SELECT_WHERE_QUERY = SELECT_QUERY + " WHERE %s = '%s'";
    public static final String SELECT_WHERE_AND = SELECT_WHERE_QUERY + " AND %s = '%s'";
    public static final String SELECT_WHERE_ID = SELECT_QUERY + " WHERE id = '%s'";
    public static final String SELECT_WHERE_AND_OFFSET = SELECT_WHERE_AND + " OFFSET %s ROWS FETCH NEXT %s ROWS ONLY";
    public static final String SELECT_WHERE_OFFSET = SELECT_WHERE_QUERY + " OFFSET %s ROWS FETCH NEXT %s ROWS ONLY";
    public static final String SELECT_OFFSET_NEXT = SELECT_QUERY + " OFFSET %s ROWS FETCH NEXT %s ROWS ONLY";
    public static final String SELECT_WHERE_AND_RANDOM = SELECT_WHERE_AND + " ORDER BY DBMS_RANDOM.VALUE FETCH FIRST %s ROWS ONLY";

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

    public <T extends BaseEntity> T getRandomEntity(Class type, String... parameters) {
        StringBuilder conditionBuilder = new StringBuilder();
        for (int i = 0; i < parameters.length - 1; i += 4) {
            if (i < parameters.length - 4)
                conditionBuilder.append(parameters[i]).append(" " + parameters[i + 1] + " " + "'" + parameters[i + 2] + "'" + " " + parameters[i + 3] + " ");
            else
                conditionBuilder.append(parameters[i]).append(" " + parameters[i + 1] + " " + "'" + parameters[i + 2] + "'");
        }
        String query = String.format("SELECT * FROM " + this.name + " WHERE %s ORDER BY DBMS_RANDOM.VALUE FETCH FIRST %s ROWS ONLY",
                conditionBuilder, 1);

        return (T) queryForObject(query
                , new BeanPropertyRowMapper<>(type));
    }

    public List<BaseEntity> receiveEntities(Class clazz) {
        return query(String.format(SELECT_QUERY, this.name), new BeanPropertyRowMapper<>(clazz));
    }

    public <T extends BaseEntity> T getEntityWhere(Class type, String... parameters) {
        if (parameters.length > 2) {
            return (T) queryForObject(String
                            .format(SELECT_WHERE_AND_OFFSET, this.name
                                    , parameters[0], parameters[1], parameters[2], parameters[3]
                                    , generateRandomNumberBetween(0, 620), 1)
                    , new BeanPropertyRowMapper<>(type));
        }
        return (T) queryForObject(String.format(SELECT_WHERE_QUERY, this.name, parameters[0], parameters[1]), new BeanPropertyRowMapper<>(type));
    }

    public <T extends BaseEntity> T getEntityWhereV2(Class clazz, String... parameters) {
        return (T) queryForObject(String.format(SELECT_WHERE_OFFSET, this.name, parameters[0], parameters[1], generateRandomNumberBetween(0, 620), 1), new BeanPropertyRowMapper<>(clazz));
    }

    public List<BaseEntity> receiveEntitiesWithOffset(Class clazz) {
        return query(String.format(SELECT_OFFSET_NEXT, this.name, generateRandomNumberBetween(0, 350), 50), new BeanPropertyRowMapper<>(clazz));
    }

}
