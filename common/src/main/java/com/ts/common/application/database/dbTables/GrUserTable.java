package com.ts.common.application.database.dbTables;

import com.ts.common.application.database.AbstractDbTable;
import com.ts.common.application.database.dbEntities.GrUserDbEntity;
import com.ts.common.entitites.BaseEntity;
import com.ts.common.utils.RandomUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class GrUserTable extends AbstractDbTable {
    public static final String NAME = "GR_USER";

    public GrUserTable(JdbcTemplate template) {
        super(NAME, template);
    }

    public BaseEntity receiveRandomPerson() {
        List<BaseEntity> baseEntities = super.receiveEntitiesWithOffset(GrUserDbEntity.class);
        return baseEntities.get(RandomUtils.generateRandomNumberBetween(0, baseEntities.size() - 1));
    }
}
