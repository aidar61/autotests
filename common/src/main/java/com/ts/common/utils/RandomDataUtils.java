package com.ts.common.utils;

import com.ts.common.application.database.DbHelper;
import com.ts.common.application.database.dbEntities.GrUserDbEntity;
import com.ts.common.controllers.BaseController;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.enums.Users;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.ts.common.config.AppConfigProvider.STAND_URL;
import static com.ts.common.utils.InitEntities.generateAuthToken;

@Slf4j
public class RandomDataUtils {
    private final static DbHelper dbHelper = new DbHelper();
    private final static BaseController baseController = new BaseController(STAND_URL, generateAuthToken(Users.ROOT));

    private RandomDataUtils() {
    }

    public static User getRandomUser() {
        GrUserDbEntity user = (GrUserDbEntity) dbHelper.getGrUserTable().receiveRandomPerson();
        return user.mapTo();
    }

    public static String getRandomUserID() {
        return getRandomUser().getId();
    }

    private static List<Task> receiveTaskForUdf(Udfs.UdfSd udfSd, String taskNumber, String parentNumber) {
        Task[] tasks = baseController.receiveDaughterTasksForUdfFields(udfSd, taskNumber, parentNumber).as(Task[].class);
        return List.of(tasks);
    }

    public static Task receiveRandomTaskForUdf(Udfs.UdfSd udfSd, String taskNumber, String parentNumber) {
        List<Task> tasks = receiveTaskForUdf(udfSd, taskNumber, parentNumber);
        Task task = tasks.get(RandomUtils.generateRandomNumberBetween(0, tasks.size() - 1));
        log.warn("Receiving random task for Udf: {}", task);
        return task;
    }
}
