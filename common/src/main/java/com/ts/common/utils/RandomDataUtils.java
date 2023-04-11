package com.ts.common.utils;

import com.ts.common.application.database.DbHelper;
import com.ts.common.application.database.dbEntities.GrUserDbEntity;
import com.ts.common.entitites.commonEntities.User;

public class RandomDataUtils {
    private final static DbHelper dbHelper = new DbHelper();

    private RandomDataUtils() {
    }

    public static User getRandomUser() {
        GrUserDbEntity user = (GrUserDbEntity) dbHelper.getGrUserTable().receiveRandomPerson();
        return user.mapTo();
    }

    public static String getRandomUserID() {
        return getRandomUser().getId();
    }
}
