package com.ts.common.controllers.performance;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.config.AppConfigProvider;
import com.ts.common.controllers.BaseController;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Performance;
import com.ts.common.enums.TaskType;
import com.ts.common.enums.Users;
import com.ts.common.utils.InitEntities;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.Map;

import static com.ts.common.application.controllers.TrackStudioEndPoints.*;
import static com.ts.common.enums.Operations.CANCEL;
import static com.ts.common.enums.Performance.*;

public class PerformanceController extends BaseController {
    public PerformanceController(String url, AuthToken authToken) {
        super(url, authToken);
    }

    @Step("Запрос портлета \"Оперативный контроль доработок\"")
    public void getOperationControlOf(Performance performanceColumn) {
        super.get(getEndpoint(REST, PORTLET, SLA_FEATURE_CONTROL, GET_SLA_FEATURE_CONTROL
                , formatParameters(Map.of(
                        "columns", COLUMNS.value
                        , "project", performanceColumn.value
                ))));
    }

    @Step("Запрос портлета \"Очередь задач сотрудника\"")
    public void getPortletOfEmployeeTasksQueue(User user) {
        super.get(getEndpoint(REST, PORTLET, PERSONAL_QUEUE, user.getLogin(), TASKS
                , formatParameters(Map.of(
                        "columns", COLUMNS_PORTLET_EMPLOYEE_QUEUE.value
                ))));
    }

    public void cancelTask(GeneralTask task) {
        task.refreshUdf();
        task.refreshTask();
        task.setTaskType(TaskType.WORK_TASK);
        super.performCommonOperation(task, CANCEL);
    }

}
