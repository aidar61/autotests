package com.ts.common.asserts;

import com.ts.common.entitites.BaseEntity;
import com.ts.common.entitites.commonEntities.Status;

import java.util.Arrays;

import static org.testng.Assert.*;

public class EntityAssert {
    protected BaseEntity entity;
    protected BaseEntity[] entities;

    public EntityAssert(BaseEntity entity) {
        this.entity = entity;
    }

    public EntityAssert(BaseEntity[] entities) {
        this.entities = entities;
    }

    public void isContainsInList(BaseEntity expectedEntity) {
        boolean contains = Arrays.stream(this.entities).anyMatch(a -> a.isEqualsNoLog(expectedEntity));
        assertTrue(contains, "Entity [" + expectedEntity + "] is not in " + Arrays.toString(this.entities));
    }

    public void isExist() {
        assertNotNull(entity, "Object is exist");
    }

    public void isNotExist() {
        assertNull(entity, "Object is not exist");
    }


    public void isEquals(BaseEntity expectedEntity) {
        assertTrue(entity.isEquals(expectedEntity), entity.getClass().getName() + " parameters is match: " + entity);
    }

    public <T extends BaseEntity> void isCorrectStatus(Status expectedStatus) {
//        assertEquals(expectedStatus, entity.receiveTaskStatus());
        assertTrue(expectedStatus.isEquals(entity.receiveTaskStatus()), entity.getClass().getName() + " parameters is match: " + entity);
    }
}
