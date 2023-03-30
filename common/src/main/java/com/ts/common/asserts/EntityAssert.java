package com.ts.common.asserts;

import com.ts.common.entitites.BaseEntity;
import com.ts.common.entitites.commonEntities.Status;

import static org.testng.Assert.*;

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
    public void isEquals(BaseEntity actualEntity) {
        assertTrue(entity.isEquals(actualEntity), entity.getClass().getName() + " parameters is match: " + entity);
    }
    public <T extends BaseEntity> void isCorrectStatus(Status expectedStatus) {
        assertEquals(expectedStatus, entity.receiveTaskStatus());
    }
}
