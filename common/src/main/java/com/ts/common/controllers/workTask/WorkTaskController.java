package com.ts.common.controllers.workTask;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.TaskRequestBody;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.JsonUtils;
import com.ts.common.utils.RandomUtils;
import io.restassured.response.Response;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.controllers.TaskRequestBody.Fields.HANDLER_USER;
import static com.ts.common.controllers.TaskRequestBody.Fields.PRIORITY;
import static com.ts.common.enums.TaskType.WORK_TASK;
import static com.ts.common.utils.InitEntities.generateCategory;
import static com.ts.common.utils.InitEntities.generateCategoryForWorkTask;

public class WorkTaskController extends BaseController {
    private static final TaskType TASK_TYPE = WORK_TASK;

    public WorkTaskController(String url, AuthToken authToken) {
        super(url, authToken);
        super.taskType = TASK_TYPE;
    }

    @Override
    protected Response createTask(String requestBody) {
        return super.createTask(requestBody);
    }

    public Response createAbstractWorkTask(GeneralTask task) {
        TaskRequestBody requestBody = new TaskRequestBody(task);
        this.response = createTask(requestBody.keepMandatoryAndCreateFieldsAnd(PRIORITY, HANDLER_USER));
        TaskResponseBody taskResponseBody = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        if (taskResponseBody != null) {
            task.setId(taskResponseBody.getId());
            task.setNumber(taskResponseBody.getNumber());
            task.setFinishStatus(taskResponseBody.getFinishStatus());
        }
        return this.response;
    }

    public TreeMap<TaskType.WorkTask, GeneralTask> createAllCategoriesOfWorkTaskProcess(GeneralTask task) {
        TreeMap<TaskType.WorkTask, GeneralTask> generalTasks = new TreeMap<>();
        TaskType.WorkTask[] workTaskCategories = TaskType.WorkTask.values();
        Arrays.stream(workTaskCategories).forEach(
                t -> {
                    task.setCategory(generateCategoryForWorkTask(t));
                    this.response = createAbstractWorkTask(task);
                    ApiAsserts.assertThat(this.response).isCorrectResponseCode(HTTP_OK).isParseableBody(TaskResponseBody.class);
                    generalTasks.put(t, task);
                }
        );
        return generalTasks;
    }

//    public Response performOperation(GeneralTask task, Operations operation) {
//        task.setOperation(InitEntities.generateOperationID(this.taskType, operation));
//        if (task.getDescription() == null) task.setDescription(RandomUtils.generateDescriptionForOperation(operation));
//        TaskRequestBody taskRequestBody = new TaskRequestBody(task);
//        return this.response = performOperationWithQueryParam(task, taskRequestBody.keepFields(DEFAULT_FIELDS_WITH_STATUS_AND_RESOLUTION));
//
//    }

}
