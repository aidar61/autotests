package com.ts.integration.tests.proc_sd_question;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.sdquestion.SdQuestionController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.hibernate.id.GUIDGenerator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.ts.common.application.database.DbQueryHelper.Operators.*;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.TaskStatuses.*;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.generateString;

public class SdQuestionBaseTest extends BaseIntegrationTest {
    public SdQuestionController sdQuestionController;
    private GeneralTask task;
    private Parent parent;
    private Map<String, User> members;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        members = new HashMap<>();
        sdQuestionController = apiController.getSdQuestionController();
        userController = apiController.getUserController();
        var grTaskTable = dbHelper.getGrTaskTable();
        var parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveRandomTask(
                "task_category", EQUAL.operator, "CAT_SDPROJECT",
                AND.operator,
                "task_status", EQUAL.operator, STATUS_SDPROJECT_NEW.name(),
                OR.operator,
                "task_status", EQUAL.operator, STATUS_SDPROJECT_WARRANTY.name(),
                AND.operator,
                "task_path", LIKE.operator, "%/8860/758008(TPRJ-02-04)%");
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        task = InitEntities.getGeneralTask(TaskType.SD_QUESTION, Operations.CAT);
        var employees = userController.receiveUserByTask(parent.getNumber());
        var creator = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Менеджер клиента")).findAny().get().getForUser();
        var handler = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Клиент") && !f.getForUser().getLogin().equals(creator.getLogin())).findAny().get().getForUser();
        members.put("Менеджер клиента", creator);
        members.put("Клиент", handler);
    }

    @Test(groups = {"SdQuestion", "Regression"}, description = "создание CAT_SDQUESTION")
    public void createSdQuestion() {
        apiController.updateToken(InitEntities.generateAuthToken(members.get("Менеджер клиента")));
        task.setHandlerUser(members.get("Клиент"));
        task.setParent(parent);
        task.setName("Задача Вопрос клиенту: " + GUIDGenerator.GENERATOR_NAME);
        task.setDescription(generateString());

        task.refreshTask();
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, new Task("818181b03c7fc013013c7fcaae5406fd", "186726")));
        udf.setSecondUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, new Task("818180a050c582480150c94f5cab3356", "462540")));
        udf.setThirdUdfTask(generateUdfTask(UDF_WORKTASK_SDREQUEST, new Task("8a8181df7740c235017749e6c0e20056", "1144909")));

        task.refreshUdf(udf);
        sdQuestionController.createSdQuestion(task);

        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
//                .isCorrectStatus(STATUS_WORKTASK_ASSIGNED)
                .isEquals(task);
    }
}
