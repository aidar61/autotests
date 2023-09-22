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
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.hibernate.id.GUIDGenerator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.ABDULLAEV_BAHODIR;
import static com.ts.common.enums.Operations.*;
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
        var parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveRandomTaskByQuery("SELECT * FROM gr_task WHERE task_category = 'CAT_SDPROJECT' AND (task_status = 'STATUS_SDPROJECT_NEW' OR task_status = 'STATUS_SDPROJECT_WARRANTY') AND task_path LIKE '%/1/8860/758008/%' ORDER BY DBMS_RANDOM.VALUE FETCH FIRST 1 ROWS ONLY");
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        task = InitEntities.getGeneralTask(TaskType.SD_QUESTION, Operations.CAT);
        var employees = userController.receiveUserByTask(parent.getNumber());
        var creator = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Менеджер клиента")).findAny().get().getForUser();
        var handler = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Клиент") && !f.getForUser().getLogin().equals(creator.getLogin())).findAny().get().getForUser();
        var handler2 = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Клиент") && !f.getForUser().getLogin().equals(handler.getLogin()) && !f.getForUser().getLogin().equals(creator.getLogin())).findAny().get().getForUser();
        members.put("Менеджер клиента", creator);
        members.put("Клиент", handler);
        members.put("Клиент2", handler2);
    }

    @Test(groups = {"SdQuestion", "Regression"}, description = "создание CAT_SDQUESTION")
    public void createSdQuestion() {
        apiController.updateToken(InitEntities.generateAuthToken(members.get("Менеджер клиента")));
        task.setParent(parent);
        task.setName("Задача Вопрос клиенту: " + GUIDGenerator.GENERATOR_NAME);
        task.setDescription(generateString());
        task.setHandlerUser(generateUser(members.get("Клиент")));
        udf = refreshUdf();
        var sdModule = new Task("818181b03c7fc013013c7fcaae5406fd", "186726");
        var bdkuConf = new Task("818180a050c582480150c94f5cab3356", "462540");
        var sdRequest = new Task("8a8181df7740c235017749e6c0e20056", "1144909");
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, sdModule));
        udf.setSecondUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, bdkuConf));
        udf.setThirdUdfTask(generateUdfTask(UDF_WORKTASK_SDREQUEST, sdRequest));
        task.refreshUdf(udf);
        sdQuestionController.createSdQuestion(task);
        var response = sdQuestionController.getResponse();
        ApiAsserts.assertThat(response)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDQUESTION_NEW)
                .isEquals(task);
        CommonAssert.assertThat(response)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, "818182d33920daa3013920dde2800028");
    }

    @Test(groups = {"SdQuestion", "Regression"}, description = "Комментарий", dependsOnMethods = "createSdQuestion")
    public void taskComment() {
        apiController.updateToken(generateAuthToken(members.get("Клиент")));
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdQuestionController.performCommonOperation(task, COMMENT);
        var updateResponse = sdQuestionController.getResponse();
        ApiAsserts.assertThat(updateResponse)
                .checkingResponseMessageField("description", comment)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }

    @Test(groups = {"SdQuestion", "Regression"}, description = "Назначить наблюдателей клиента", dependsOnMethods = "taskComment")
    public void appointClientWatcher() {
        apiController.updateToken(generateAuthToken(members.get("Клиент")));
        var clientWatcher = "test@domain.com";
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);
        udf = refreshUdf();
        udf.setUdfString(generateUdfString(UDF_SD_CLIENTWATCHERS, clientWatcher));
        task.refreshUdf(udf);
        sdQuestionController.performCommonOperation(task, APPOINTCLIWATCHER);
        var updateResponse = sdQuestionController.getResponse();
        ApiAsserts.assertThat(updateResponse)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectStringField("Наблюдатели клиента", UDF_SD_CLIENTWATCHERS, clientWatcher);
    }

    @Test(groups = {"SdQuestion", "Regression"}, description = "Ответить", dependsOnMethods = "appointClientWatcher")
    public void answer() {
        apiController.updateToken(generateAuthToken(members.get("Клиент")));
        var supplier = "818182d33920daa3013920dde2200027";
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdQuestionController.performCommonOperation(task, ANSWER);
        var updateResponse = sdQuestionController.getResponse();
        ApiAsserts.assertThat(updateResponse)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDQUESTION_ANSWERED);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, supplier);
    }

    @Test(groups = {"SdQuestion", "Regression"}, description = "Задать уточняющий вопрос", dependsOnMethods = "answer")
    public void askMore() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер клиента")));
        var client = "818182d33920daa3013920dde2800028";
        task.refreshTask();
        task.setDescription(generateString());
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdQuestionController.performCommonOperation(task, ASKMORE);
        var updateResponse = sdQuestionController.getResponse();
        ApiAsserts.assertThat(updateResponse)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDQUESTION_NEW);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, client);
    }

    @Test(groups = {"SdQuestion", "Regression"}, description = "Приватный комментарий", dependsOnMethods = "askMore")
    public void privateComment() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер клиента")));
        task.refreshTask();
        task.setDescription(generateString());
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdQuestionController.performCommonOperation(task, PRIVATECOMMENT);
        var updateResponse = sdQuestionController.getResponse();
        ApiAsserts.assertThat(updateResponse)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
    }

    @Test(groups = {"SdQuestion", "Regression"}, description = "Назначить ответственного", dependsOnMethods = "privateComment")
    public void setHandler() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер клиента")));
        task.refreshTask();
        task.setDescription(generateString());
        task.setHandlerUser(generateUser(members.get("Клиент2")));
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, members.get("Клиент2")));
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdQuestionController.performCommonOperation(task, SDQUESTION_ASSIGN);
        var updateResponse = sdQuestionController.getResponse();
        ApiAsserts.assertThat(updateResponse)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        CommonAssert
                .assertThat(updateResponse)
                .isCorrectMessageField("handlerUser.login", members.get("Клиент2").getLogin());
    }

    @Test(groups = {"SdQuestion", "Regression"}, description = "Назначить наблюдателя", dependsOnMethods = "setHandler")
    public void setWatcher() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер клиента")));
        task.refreshTask();
        task.setDescription(generateString());
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(UDF_WATCHER, ABDULLAEV_BAHODIR));
        task.refreshUdf(udf);
        sdQuestionController.performCommonOperation(task, WATCH);
        var updateResponse = sdQuestionController.getResponse();
        ApiAsserts.assertThat(updateResponse)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfUSer(UDF_WATCHER, ABDULLAEV_BAHODIR.getLogin());
    }

    @Test(groups = {"SdQuestion", "Regression"}, description = "Ответить", dependsOnMethods = "setWatcher")
    public void answer2() {
        apiController.updateToken(generateAuthToken(members.get("Клиент")));
        var supplier = "818182d33920daa3013920dde2200027";
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdQuestionController.performCommonOperation(task, ANSWER);
        var updateResponse = sdQuestionController.getResponse();
        ApiAsserts.assertThat(updateResponse)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDQUESTION_ANSWERED);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, supplier);
    }

    @Test(groups = {"SdQuestion", "Regression"}, description = "Ответить", dependsOnMethods = "setWatcher")
    public void close() {
        apiController.updateToken(generateAuthToken(members.get("Клиент")));
        var nobody = "818182d33920daa3013920dde2b30029";
        var comment = generateString();
        task.refreshTask();
        task.setDescription(comment);
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdQuestionController.performCommonOperation(task, CLOSE);
        var updateResponse = sdQuestionController.getResponse();
        ApiAsserts.assertThat(updateResponse)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDQUESTION_CLOSED);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, nobody);
    }

    @Test(groups = {"SdQuestion", "Regression"}, description = "Задать уточняющий вопрос", dependsOnMethods = "close")
    public void askMore2() {
        apiController.updateToken(generateAuthToken(members.get("Менеджер клиента")));
        var client = "818182d33920daa3013920dde2800028";
        task.refreshTask();
        task.setDescription(generateString());
        udf = refreshUdf();
        task.refreshUdf(udf);
        sdQuestionController.performCommonOperation(task, ASKMORE);
        var updateResponse = sdQuestionController.getResponse();
        ApiAsserts.assertThat(updateResponse)
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_SDQUESTION_NEW);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectUdfList(UDF_SD_RESPONSIBLE_PARTY, client);
    }
}
