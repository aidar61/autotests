package com.ts.common.application.database.dbTables;

import com.ts.common.application.database.AbstractDbTable;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.entitites.BaseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

public class GrTaskTable extends AbstractDbTable {
    public static final String NAME = "gr_task";

    public GrTaskTable(JdbcTemplate template) {
        super(NAME, template);
    }

    public BaseEntity receiveByTaskNumber(String number) {
        return super.getEntityWhere(GrTaskDbEntity.class, "task_number", number);
    }
}
