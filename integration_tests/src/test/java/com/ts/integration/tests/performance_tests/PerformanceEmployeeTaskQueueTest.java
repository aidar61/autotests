//package com.ts.integration.tests.performance_tests;
//
//import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
//import com.ts.common.asserts.ApiAsserts;
//import com.ts.common.controllers.TaskResponseBody;
//import com.ts.common.controllers.performance.PerformanceController;
//import com.ts.common.entitites.commonEntities.User;
//import com.ts.common.entitites.tasks.GeneralTask;
//import com.ts.common.enums.Operations;
//import com.ts.common.enums.TaskType;
//import com.ts.common.enums.Users;
//import com.ts.common.utils.InitEntities;
//import com.ts.integration.tests.BaseIntegrationTest;
//import com.ts.integration.tests.proc_work_task.CommonWorkTaskProcessTest;
//import org.testng.annotations.AfterClass;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//
//import static com.ts.common.enums.Operations.CANCEL;
//import static com.ts.common.enums.Users.ROOT;
//import static com.ts.common.utils.InitEntities.generateAuthToken;
//
//public class PerformanceEmployeeTaskQueueTest extends BaseIntegrationTest {
//    private PerformanceController performanceController;
//    private CommonWorkTaskProcessTest commonWorkTaskProcessTest;
//    private User HANDLER_USER;
//    private Map<TaskType.WorkTask, GeneralTask> allCategoriesOfWorkTaskProcess;
//    private List<GeneralTask> createdGeneralTasks;
//
//    @BeforeClass(alwaysRun = true)
//    public void beforeClass() {
//        createdGeneralTasks = new ArrayList<>();
//        commonWorkTaskProcessTest = new CommonWorkTaskProcessTest();
//        performanceController = apiController.getPerformanceController();
//        TaskType.WorkTask[] workTask = TaskType.WorkTask.values();
//        commonWorkTaskProcessTest.beforeClass();
//        for (int i = 0; i < 7; i++) {
//            Arrays.stream(workTask).forEach(taskType -> {
//                        commonWorkTaskProcessTest.createTask(taskType);
//                        allCategoriesOfWorkTaskProcess = commonWorkTaskProcessTest.getAllCategoriesOfWorkTaskProcess();
//                        createdGeneralTasks.add(allCategoriesOfWorkTaskProcess.get(taskType));
//                    }
//            );
//        }
//        HANDLER_USER = commonWorkTaskProcessTest.getHANDLER_USER();
//        System.err.println(createdGeneralTasks);
//        System.err.println(createdGeneralTasks.size());
//    }
//
//    @AfterClass(alwaysRun = true)
//    public void afterClass() {
//        apiController.updateToken(generateAuthToken(ROOT));
//        createdGeneralTasks.forEach(task ->
//                performanceController.cancelTask(task)
//        );
//    }
//
//    @Test(groups = {"Performance"}, description = "Запрос данных для портлета \"Очередь задач сотрудника\"")
//    public void getPortletOfEmployeeTaskQueue() {
//        apiController.updateToken(generateAuthToken(ROOT));
//
//        performanceController.getPortletOfEmployeeTasksQueue(HANDLER_USER);
//        ApiAsserts.assertThat(performanceController.getResponse())
//                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
//    }
//
//}
