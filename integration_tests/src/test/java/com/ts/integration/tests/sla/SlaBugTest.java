//package com.ts.integration.tests.sla;
//
//import com.ts.common.asserts.ApiAsserts;
//import com.ts.common.controllers.TaskResponseBody;
//import com.ts.common.controllers.sla.SlaBugController;
//import com.ts.common.entitites.commonEntities.Udfs;
//import com.ts.common.entitites.commonEntities.User;
//import com.ts.common.entitites.tasks.GeneralTask;
//import com.ts.common.enums.ComSlaOperations;
//import com.ts.common.enums.SlaType;
//import com.ts.common.listeners.TestListener;
//import com.ts.common.utils.InitEntities;
//import com.ts.integration.tests.BaseIntegrationTest;
//import jdk.jfr.Description;
//import org.testng.annotations.AfterClass;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Listeners;
//import org.testng.annotations.Test;
//
//import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
//import static com.ts.common.entitites.commonEntities.List.Constants.CRITICAL;
//import static com.ts.common.entitites.commonEntities.List.Constants.REMOTE_ACCESS;
//import static com.ts.common.entitites.commonEntities.Task.Constants.AKKREDITIVES;
//import static com.ts.common.entitites.commonEntities.Task.Constants.MTBANK;
//import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
//import static com.ts.common.entitites.commonEntities.User.Constants.ALTUNIN_NIKOLAY;
//import static com.ts.common.enums.Users.*;
//import static com.ts.common.utils.InitEntities.*;
//
//@Listeners({TestListener.class})
//public class SlaBugTest extends BaseIntegrationTest {
//    private static SlaBugController slaBugController;
//    private GeneralTask task;
//    private Udfs udf;
//
//    @BeforeClass(alwaysRun = true)
//    public void beforeClass() {
//        udf = refreshUdf();
//        udf.setUdfTask(InitEntities.generateUdfTask(UDF_SD_MODULE, AKKREDITIVES));
//        udf.setSecondUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, MTBANK));
//        udf.setUdfList(InitEntities.generateUdfList(UDF_SDBUG_PRIORITYBUG, CRITICAL));
//        udf.setSecondUdfList(InitEntities.generateUdfList(UDF_SD_REMOTEACCESS, REMOTE_ACCESS));
//        task = InitEntities.getSlaTask(SlaType.SLA_BUG, ComSlaOperations.CAT);
//        task.refreshUdf(udf);
//        task.setHandlerUser(generateUser(ALTUNIN_NIKOLAY));
//        apiController.updateToken(generateAuthToken(CLIENT));
//        slaBugController.createSlaBugTask(task);
//        ApiAsserts.assertThat(slaBugController.getResponse())
//                .isCorrectResponseCode(HTTP_OK)
//                .isParseableBody(TaskResponseBody.class);
//    }
//
//    @AfterClass(alwaysRun = true)
//    public void afterClass() {
//        slaBugController.performCommonOperation(task, ComSlaOperations.REMOVE_REQUEST);
//    }
//
//    @Test(priority = 0)
//    @Description("Test description: Receive task")
//    public void receiveTask() {
//        apiController.updateToken(generateAuthToken(ROOT));
//        slaBugController.receiveActualTask(task.getNumber());
//        ApiAsserts.assertThat(slaBugController.getResponse())
//                .isCorrectResponseCode(HTTP_OK)
//                .isParseableBody(TaskResponseBody.class);
//    }
//
//    @Test(priority = 1)
//    @Description("Test description: Perform operation to change author")
//    public void commonOperations() {
//        udf = refreshUdf();
//        udf.setUdfUser(generateUdfUser(UDF_SD_AUTHORCLIENT_MSG, User.Constants.AKSENOV_ANDREY));
//        task.setUdfs(udf);
//        apiController.updateToken(generateAuthToken(EMPLOYEE));
//        slaBugController.performCommonOperation(task, ComSlaOperations.CHANGE_AUTHOR);
//        ApiAsserts.assertThat(slaBugController.getResponse())
//                .isCorrectResponseCode(HTTP_OK)
//                .isParseableBody(TaskResponseBody.class);
//    }
//}
