package com.ts.common.asserts;

import com.ts.common.entitites.BaseEntity;

public class SettingsAssert extends EntityAssert{
    BaseEntity[] entity;

    public SettingsAssert(BaseEntity[] entity) {
        super(entity);
    }

    public static SettingsAssert assertThat(BaseEntity[] entity) {
        return new SettingsAssert(entity);
    }

}
