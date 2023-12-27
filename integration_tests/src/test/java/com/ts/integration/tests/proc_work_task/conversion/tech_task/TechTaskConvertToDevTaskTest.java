package com.ts.integration.tests.proc_work_task.conversion.tech_task;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.asserts.CommonAssert;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.workTask.TechTaskController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.UserData;
import com.ts.common.entitites.commonEntities.udf.UdfTask;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.DateUtils;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ts.common.application.database.DbQueryHelper.Operators.*;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.*;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.ABDULLAEV_BAHODIR;
import static com.ts.common.entitites.commonEntities.User.Constants.BABUSHKIN_IVAN;
import static com.ts.common.enums.Operations.CHANGE_CAT;
import static com.ts.common.enums.TaskStatuses.STATUS_PROJECT_PLANNED;
import static com.ts.common.enums.TaskStatuses.STATUS_WORKTASK_ONANALYSIS;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.generateString;

public class TechTaskConvertToDevTaskTest extends BaseIntegrationTest {
    public TechTaskController techTaskController;
    private GeneralTask task;
    private String misService;
    private String cdpBl;
    private UdfTask productTask;
    private Parent parent;
    private GrTaskTable grTaskTable;
    private GrTaskDbEntity parentTaskFromDb;
    private String expectedCompletionDate;
    private UdfTask customerRequest;
    private User creator;
    private User handlerUser;
    private User randomUser;
    private Map<String, UdfTask> tasks;


    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        techTaskController = apiController.getTechTaskController();
        userController = apiController.getUserController();
        grTaskTable = dbHelper.getGrTaskTable();
        parentTaskFromDb = (GrTaskDbEntity) grTaskTable.receiveRandomTask(
                "task_category", EQUAL.operator, "CAT_GENPLAN",
                AND.operator,
                "task_status", EQUAL.operator, STATUS_PROJECT_PLANNED.name(),
                AND.operator,
                "task_path", LIKE.operator, "%/2405/758009%");
        parent = InitEntities.generateParent(parentTaskFromDb.getTask_id(), parentTaskFromDb.getTask_number());
        task = InitEntities.getGeneralTask(TaskType.CAT_TECHTASK, Operations.CAT);
        tasks = techTaskController.getTaskForSDRequest(parent.getNumber());
        productTask = tasks.get("UDF_PRODUCT");
        var taskSlaBug = (GrTaskDbEntity) grTaskTable.receiveRandomTask(
                "task_category", EQUAL.operator, "CAT_SLABUG",
                AND.operator,
                "task_status", NOT_EQUAL.operator, STATUS_PROJECT_PLANNED.name());
        customerRequest = InitEntities.generateUdfTask(UDF_WORKTASK_SDREQUEST, new Task(taskSlaBug.getTask_id(), taskSlaBug.getTask_number()));
        misService = techTaskController.getMisService().get(0);
        cdpBl = techTaskController.getCdpBl();

        //Employees
        var employees = userController.receiveUserByTask(parent.getNumber());
        Collections.shuffle(employees);
        var creators = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Менеджер проекта") && f.getForUser().getActive() == true).collect(Collectors.toList());
        var handlers = employees.stream().filter(f -> f.getAssignedRole().getName().equals("Участник проекта") && f.getForUser().getActive() == true).collect(Collectors.toList());
        creator = creators.get(0).getForUser();
        handlerUser = handlers.get(0).getForUser();
        randomUser = handlers.get(1).getForUser();


        // TODO: 29.11.2023 Check await date
    }


    @Test(groups = {"WorkTask", "Regression"}, description = "Создание CAT_TECHTASK")
    public void createTask() {
        udf = refreshUdf();
        task.refreshTask();
        apiController.updateToken(InitEntities.generateAuthToken(creator));
        task.setParent(parent);
        task.setName("Новая задача категории Технологическая задача" + LocalDateTime.now().getNano());
        task.setDescription(task.getDescription() + generateString());
        task.setHandlerUser(handlerUser);
        expectedCompletionDate = DateUtils.getCurrentDate(1);
        udf.setUdfUser(generateUdfUser(UDF_WORKTASK_SUPERVISER, ABDULLAEV_BAHODIR));
        udf.setSecondUdfUser(generateUdfUser(UDF_WATCHER, BABUSHKIN_IVAN));
        if (cdpBl != null) udf.setUdfList(generateUdfList(UDF_CDP_BL, cdpBl));
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, CORE));
        udf.setUdfString(generateUdfString(UDF_SD_NOMODULE_REASON, generateString()));
        if (misService != null) udf.setNinethUdfList(generateUdfList(UDF_MIS_SERVICE, misService));
        udf.setSecondUdfList(generateUdfList(UDF_CDP_ACCEPTANCE, REQBYAUTHOR));
        udf.setSeventhUdfList(generateUdfList(UDF_WORKTASK_ANALYSIS, YES_V2));
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_ANALYSISFD, DateUtils.getCurrentDate(0)));
        udf.setSecondUdfDate(generateUdfDate(UDF_WORKTASK_PLANTD, expectedCompletionDate));
        Map<Task.Constants, UserData> dependTaskFNC = new HashMap<>();
        var userData1 = new UserData(null, "{\"type\":\"REQUIRED\",\"comment\":\"Required\"}");
        var userData2 = new UserData(null, "{\"type\":\"RECOMMENDED\",\"comment\":\"RECOMMENDED\"}");
        var userData3 = new UserData(null, "{\"type\":\"OPTIONAL\",\"option\":\"949177\",\"optionNot\":\"773226\",\"comment\":\"Optional\"}");
        dependTaskFNC.put(RYSGAL_BANK, userData1);
        dependTaskFNC.put(WORKTASK_TESTTASK, userData2);
        dependTaskFNC.put(CUSTOMER_REQUEST, userData3);
        udf.setEighthUdfTask(generateUdfTask(UDF_WORKTASK_DEPENDTASKFNC, dependTaskFNC));
        udf.setSecondUdfTask(generateUdfTask(UDF_PRODUCT, productTask.getTaskValue()[0]));
        udf.setThirdUdfList(generateUdfList(UDF_WORKTASK_WAYCODEREVIEW, WAY_CODE_REVIEW_OFF));
        udf.setSeventhUdfTask(generateUdfTask(UDF_SD_LINKEDREQUEST, AKKREDITIVES));
        udf.setFifthUdfTask(customerRequest);
        udf.setFourthUdfList(generateUdfList(UDF_CDP_BP, INVESTMENTS_IN_PRODUCTS));
        udf.setSixthUdfTask(generateUdfTask(UDF_WORKTASK_DEPENDBF, MTBANK));
        udf.setSixthUdfList(generateUdfList(UDF_WORKTASK_CHANGEWORKERINRQST, CHANGEWORKERINRQST_NO));
        task.refreshUdf(udf);
        techTaskController.create(task);
        var response = techTaskController.getResponse();
        ApiAsserts.assertThat(response).isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK).isParseableBody(TaskResponseBody.class).assertTask().isCorrectStatus(STATUS_WORKTASK_ONANALYSIS).isEquals(task);
    }

    @Test(groups = {"WorkTask", "Regression"}, description = "Изменить категорию на доработку", dependsOnMethods = "createTask")
    public void changeCategory() {
        udf = refreshUdf();
        task.refreshTask();
        var product = productTask.getTaskValue().length > 1 ? productTask.getTaskValue()[1] : productTask.getTaskValueSelector()[0];
        udf.setUdfDate(generateUdfDate(UDF_WORKTASK_ANALYSISFD, DateUtils.getCurrentDate(5)));
        udf.setUdfTask(generateUdfTask(UDF_PRODUCT, product));
        udf.setUdfList(generateUdfList(UDF_SDFEATURE_GENUSE, LOCAL, "{\"username\":\"" + randomUser.getLogin() +
                "\",\"name\":\"" + randomUser.getName() +
                "\"}"));
        udf.setUdfMemo(generateUdfMemo(UDF_CDP_STEPPROGRESS, "[{\"id\":\"818181698c1b5016018c243d9a134ca4\",\"name\":\"Предварительный анализ\",\"order\":0,\"" + task.getId() +
                "\":\"818181698c1b5016018c243d8daf4c79\",\"weight\":0,\"budget\":7200,\"progress\":0,\"description\":\"\",\"status\":\"ACTUAL\",\"workTypeId\":" +
                "\"402881c2516c220101516c711ff80024\",\"workTypeNorm\":2.0,\"hrs\":0,\"deletable\":true,\"actualBudget\":0,\"planby\":\"budget\",\"workTypeDraftAsString\"" +
                ":\"-\",\"workTypeAsString\":\"[0701] Иное\",\"draftChanged\":true}]"));
        task.refreshUdf(udf);
        techTaskController.performCommonOperation(task, CHANGE_CAT);
        ApiAsserts.assertThat(techTaskController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                .isParseableBody(TaskResponseBody.class)
                .assertTask()
                .isCorrectStatus(STATUS_WORKTASK_ONANALYSIS);

        var taskDetail = apiController.receiveTask(task.getNumber());
        CommonAssert
                .assertThat(taskDetail)
                .isCorrectTaskCategory("CAT_DEVTASK");
    }
}
