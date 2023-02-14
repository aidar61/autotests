package com.ts.common.asserts;

import com.ts.common.entitites.BaseEntity;

public class TaskAsserts extends EntityAssert {
    public TaskAsserts(BaseEntity entity) {
        super(entity);
    }
    public static TaskAsserts assertThat(BaseEntity entity) {
        return new TaskAsserts(entity);
    }
}
