package com.ts.common.asserts;

import com.ts.common.entitites.BaseEntity;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class EntityAssert {
    protected BaseEntity entity;

    public EntityAssert(BaseEntity entity) {
        this.entity = entity;
    }

    public void isExist() {
        assertNotNull(entity, "Object is exist");
    }

    public void isNotExist() {
        assertNull(entity, "Object is not exist");
    }
}
