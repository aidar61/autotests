package com.ts.common.application.database.dbTables;

import com.ts.common.application.database.AbstractDbTable;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.entitites.BaseEntity;
import com.ts.common.enums.TaskStatuses;
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.testng.AssertJUnit.assertNotNull;

@Slf4j
public class GrTaskTable extends AbstractDbTable {
    public static final String NAME = "gr_task";

    public GrTaskTable(JdbcTemplate template) {
        super(NAME, template);
    }

    public BaseEntity receiveByTaskNumber(String number) {
        return super.getEntityWhere(GrTaskDbEntity.class, "task_number", number);
    }

    public BaseEntity receiveByCategoryAndTaskStatus(String category, TaskStatuses taskStatus) {
        BaseEntity entityWhere = super.getEntityWhere(GrTaskDbEntity.class, "task_category", category, "task_status", taskStatus.name());
        log.warn("Found following task with number {} from Database", entityWhere.receiveTaskNumber());
        return entityWhere;
    }

    @Step("[ASSERT] Check db by taskNumber: {}")
    public void isExistByTaskNumber(String number) {
        BaseEntity baseEntity = receiveByTaskNumber(number);
        assertNotNull("Object is not appear ", baseEntity);
        log.info("Object appear in db: {}", baseEntity);
    }
}
