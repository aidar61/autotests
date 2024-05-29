package com.ts.integration.tests.proc_sd_question;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.sdquestion.SdQuestionController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.DateUtils;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.JsonUtils;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.ABDULLAEV_BAHODIR;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.TaskStatuses.STATUS_SDQUESTION_CLOSED;
import static com.ts.common.enums.TaskStatuses.STATUS_SDQUESTION_NEW;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.generateString;

public class SdQuestionBaseTest2 extends BaseIntegrationTest {
    public SdQuestionController sdQuestionController;
    private GeneralTask task;
    private Parent parent;
    private Map<String, User> members;

    private Task[] BDKU_CONFIGURATION;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        members = new HashMap<>();
        sdQuestionController = apiController.getSdQuestionController();
        userController = apiController.getUserController();
        var grTaskTable = dbHelper.getGrTaskTable();
        var parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveRandomTaskByQuery("SELECT * FROM gr_task WHERE task_category = 'CAT_SDPROJECT' AND (task_status = 'STATUS_SDPROJECT_NEW' OR task_status = 'STATUS_SDPROJECT_WARRANTY') AND task_path LIKE '%/1/8860/758008/%' ORDER BY DBMS_RANDOM.VALUE FETCH FIRST 1 ROWS ONLY");
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        task = InitEntities.getGeneralTask(TaskType.SD_QUESTION, Operations.CAT);

        var parentPayloadResponse = sdQuestionController.getParentPayload(parent.getNumber(), "CAT_SDQUESTION");
        ApiAsserts.assertThat(parentPayloadResponse).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        var parentPayload = JsonUtils.removeExtraCharacters(parentPayloadResponse);
        BDKU_CONFIGURATION = sdQuestionController.getParent_UDF_BDKU_CONFIGURATION(parentPayload);

        var employees = userController.receiveUserByTask(parent.getNumber());
        var creators = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Менеджер клиента") && !Objects.equals(f.getForUser().getLogin(), "root") && f.getForUser().getActive()).collect(Collectors.toList());
        var handlers = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Клиент") && !Objects.equals(f.getForUser().getLogin(), "root") && f.getForUser().getActive()).collect(Collectors.toList());
        Collections.shuffle(handlers);
        Collections.shuffle(creators);
        var creator = creators.get(0).getForUser();
        var handler = handlers.get(0).getForUser();
        var creator2 = creators.get(1).getForUser();
        creator.setActive(null);
        creator2.setActive(null);
        handler.setActive(null);

        members.put("Менеджер клиента", creator);
        members.put("Менеджер клиента2", creator2);
        members.put("Клиент", handler);
    }

    @Test(groups = {"PROC_SDQUESTION", "Regression"}, description = "создание CAT_SDQUESTION")
    public void createSdQuestion() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер клиента")));
        task.setParent(parent);
        task.setName("Задача Вопрос клиенту: " + DateUtils.getCurrentDate(0));
        task.setDescription(generateString());
        task.setHandlerUser(generateUser(members.get("Клиент")));
        udf = refreshUdf();
        var sdModule = new Task("818181b03c7fc013013c7fcaae5406fd", "186726");
        var sdRequest = new Task("8a8181df7740c235017749e6c0e20056", "1144909");
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, sdModule));
        udf.setSecondUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, BDKU_CONFIGURATION[0]));
        udf.setThirdUdfTask(generateUdfTask(UDF_WORKTASK_SDREQUEST, sdRequest));
        task.refreshUdf(udf);
        sdQuestionController.createSdQuestion(task);
        var response = sdQuestionController.getResponse();
        ApiAsserts.assertThat(response).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_SDQUESTION_NEW).isEquals(task);
        CommonAssert.assertThat(response).isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, "818182d33920daa3013920dde2800028");
    }

    @Test(groups = {"PROC_SDQUESTION", "Regression"}, description = "Изменить автора", dependsOnMethods = "createSdQuestion")
    public void changeAuthor() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер клиента")));
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(UDF_SDQUESTION_NEWAUTHOR_MSG, ABDULLAEV_BAHODIR));
        task.refreshUdf(udf);
        sdQuestionController.performCommonOperation(task, CHANGE_AUTHOR);
        var changeAuthorResponse = sdQuestionController.getResponse();
        ApiAsserts.assertThat(changeAuthorResponse).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail).isCorrectSubmitterUser(ABDULLAEV_BAHODIR.login);
    }

    @Test(groups = {"PROC_SDQUESTION", "Regression"}, description = "Изменить автора", dependsOnMethods = "createSdQuestion")
    public void changeAuthor2() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер клиента2")));
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(UDF_SDQUESTION_NEWAUTHOR_MSG, members.get("Менеджер клиента")));
        task.refreshUdf(udf);
        sdQuestionController.performCommonOperation(task, CHANGE_AUTHOR);
        var changeAuthorResponse = sdQuestionController.getResponse();
        ApiAsserts.assertThat(changeAuthorResponse).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail).isCorrectSubmitterUser(members.get("Менеджер клиента").getLogin());
    }

    @Test(groups = {"PROC_SDQUESTION", "Regression"}, description = "Снять вопрос", dependsOnMethods = "changeAuthor2")
    public void closeTask() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер клиента")));
        task.refreshTask();
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdQuestionController.performCommonOperation(task, CANCEL);
        var updateResponse = sdQuestionController.getResponse();
        ApiAsserts.assertThat(updateResponse).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_SDQUESTION_CLOSED);
        var taskDetail = apiController.receiveTask(task.getNumber());
        var nobody = "818182d33920daa3013920dde2b30029";
        CommonAssert.assertThat(taskDetail).isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, nobody);
    }

    @Test(groups = {"PROC_SDQUESTION", "Regression"}, description = "Задать уточняющий вопрос", dependsOnMethods = "closeTask")
    public void askMore() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер клиента")));
        var client = "818182d33920daa3013920dde2800028";
        task.refreshTask();
        task.setDescription(generateString());
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdQuestionController.performCommonOperation(task, ASKMORE);
        var updateResponse = sdQuestionController.getResponse();
        ApiAsserts.assertThat(updateResponse).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_SDQUESTION_NEW);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert.assertThat(taskDetail).isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, client);
    }
}
