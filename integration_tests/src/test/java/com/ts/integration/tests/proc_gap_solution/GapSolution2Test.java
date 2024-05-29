//package com.ts.integration.tests.proc_gap_solution;
//
//import com.ts.common.asserts.ApiAsserts;
//import com.ts.common.controllers.TaskResponseBody;
//import com.ts.common.controllers.user.UserController;
//import com.ts.common.controllers.gap.GapSolutionController;
//import com.ts.common.controllers.gap.PotentialGapController;
//import com.ts.common.entitites.commonEntities.Parent;
//import com.ts.common.entitites.commonEntities.User;
//import com.ts.common.entitites.tasks.GeneralTask;
//import com.ts.common.enums.Parents;
//import com.ts.common.enums.TaskType;
//import com.ts.common.enums.Users;
//import com.ts.common.utils.InitEntities;
//import com.ts.common.utils.RandomUtils;
//import com.ts.integration.tests.BaseIntegrationTest;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;
//
//import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
//import static com.ts.common.entitites.commonEntities.List.Constants.*;
//import static com.ts.common.entitites.commonEntities.List.Constants.YES_GAP_SECOND;
//import static com.ts.common.entitites.commonEntities.Task.Constants.FRONT_OFFICE;
//import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
//import static com.ts.common.entitites.commonEntities.User.Constants.*;
//import static com.ts.common.enums.TaskType.SOL_SELECTED;
//import static com.ts.common.enums.Users.CLIENT;
//import static com.ts.common.enums.Users.SECOND_EMPLOYEE;
//import static com.ts.common.utils.InitEntities.*;
//import static com.ts.common.utils.InitEntities.generateUdfList;
//
//public class GapSolution2Test extends BaseIntegrationTest {
//    private PotentialGapController potentialGapController;
//    private GapSolutionController gapSolutionController;
//    private GeneralTask task;
//
////    protected User SECOND_EMPLOYEE;
////    protected User CLIENT;
//
//    @BeforeClass(alwaysRun = true)
//    public void beforeClass() {
////        SECOND_EMPLOYEE = userController.receiveRandomSECOND_EMPLOYEEs(Parents.RYSGAL_BANK).getForUser();
////        CLIENT = userController.receiveRandomSECOND_EMPLOYEEs(Parents.RYSGAL_BANK).getForUser();
//        potentialGapController = apiController.getPotentialGapController();
//        gapSolutionController = apiController.getGapSolutionController();
//    }
//
//    @Test(groups = {"GapSolution", "Regression"}, description = "Создание потенциального Gap")
//    public void catPotentialGap() {
////        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
////        udf = refreshUdf();
////        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, FRONT_OFFICE));
////        task = InitEntities.getSlaTask(TaskType.POTENTIAL_GAP, ComSlaOperations.CAT);
////        task.setHandlerUser(generateUser(ARTEMEVA_MARINA));
////        task.refreshUdf(udf);
////        potentialGapController.createPotentialGap(task);
////        ApiAsserts.assertThat(potentialGapController.getResponse())
////                .isCorrectResponseCode(HTTP_OK)
////                .isParseableBody(TaskResponseBody.class);
//    }
//
//    @Test(groups = {"GapSolution", "Regression"}, description = "Передать на согласование", dependsOnMethods = "catPotentialGap")
//    public void msgGapPassForApproval() {
////        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
////        udf = refreshUdf();
////        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ARUTYANIN_YURIY));
////        task.setHandlerUser(generateUser(ARUTYANIN_YURIY));
////        task.refreshUdf(udf);
////        potentialGapController.performCommonOperation(task, PASS_FOR_APPROVAL);
////        ApiAsserts.assertThat(potentialGapController.getResponse())
////                .isCorrectResponseCode(HTTP_OK)
////                .isParseableBody(TaskResponseBody.class);
//    }
//
//    @Test(groups = {"GapSolution", "Regression"}, description = "Подтвердить и опубликовать", dependsOnMethods = "msgGapPassForApproval")
//    public void msgGapConfirm() {
////        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
////        udf = refreshUdf();
////        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ARTEMEVA_MARINA));
////        task.setHandlerUser(generateUser(ARTEMEVA_MARINA));
////        task.refreshUdf(udf);
////        potentialGapController.performCommonOperation(task, CONFIRM);
////        ApiAsserts.assertThat(potentialGapController.getResponse())
////                .isCorrectResponseCode(HTTP_OK)
////                .isParseableBody(TaskResponseBody.class);
//    }
//
//    @Test(groups = {"GapSolution", "Regression"}, description = "Создать решение GAP", dependsOnMethods = "msgGapConfirm")
//    public void catSolSelected() {
////        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
////        Parent parent = generateParent(task.getId(), task.getNumber());
////        task = InitEntities.getSlaTask(SOL_SELECTED, CAT, parent);
////        task.setHandlerUser(generateUser(ARTEMEVA_MARINA));
////        udf = refreshUdf();
////        udf.setUdfUser(generateUdfUser(UDF_WATCHER, ABDULLAEV_BAHODIR));
////        udf.setUdfString(generateUdfString(UDF_REALIZATION_DECISION, RandomUtils.generateString()));
////        udf.setSecondUdfString(generateUdfString(UDF_SDFEATURE_IMPLSTATEMENT, RandomUtils.generateString()));
////        udf.setUdfList(generateUdfList(UDF_SOLUTION_PERIOD, MEDIUM_TERM));
////        udf.setSecondUdfList(generateUdfList(UDF_CUSTOMIZATION_FLAG, YES_GAP));
////        udf.setThirdUdfList(generateUdfList(UDF_LOCALIZATION_FLAG, NO_GAP));
////        udf.setFourthUdfList(generateUdfList(UDF_WORKTASK_GENUSEFUTAG, YES_GAP_SECOND));
////        task.refreshUdf(udf);
////        gapSolutionController.createGapSolution(task);
////        ApiAsserts.assertThat(gapSolutionController.getResponse())
////                .isCorrectResponseCode(HTTP_OK)
////                .isParseableBody(TaskResponseBody.class);
//    }
//
//    @Test(groups = {"GapSolution", "Regression"}, description = "Передать на внутреннее согласование", dependsOnMethods = "catSolSelected")
//    public void passForApproval() {
////        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
////        udf = refreshUdf();
////        udf.setUdfString(generateUdfString(UDF_REALIZATION_DECISION, RandomUtils.generateString()));
////        udf.setSecondUdfString(generateUdfString(UDF_SDFEATURE_IMPLSTATEMENT, RandomUtils.generateString()));
////        udf.setUdfList(generateUdfList(UDF_CUSTOMIZATION_FLAG, O30));
////        udf.setSecondUdfList(generateUdfList(UDF_LOCALIZATION_FLAG, YES_LOCAL_FLAG));
////        udf.setThirdUdfList(generateUdfList(UDF_WORKTASK_GENUSEFUTAG, YES_GAP_SECOND));
////        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ARUTYANIN_YURIY));
////        task.refreshUdf(udf);
////        task.setHandlerUser(generateUser(ARUTYANIN_YURIY));
////        gapSolutionController.performCommonOperation(task, PASS_FOR_APPROVAL);
////        ApiAsserts.assertThat(gapSolutionController.getResponse())
////                .isCorrectResponseCode(HTTP_OK)
////                .isParseableBody(TaskResponseBody.class);
//    }
//
//    @Test(groups = {"GapSolution", "Regression"}, description = "Изменить решение на постановку реализацию", dependsOnMethods = "passForApproval")
//    public void changeDecision() {
////        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
////        udf = refreshUdf();
////        udf.setUdfString(generateUdfString(UDF_REALIZATION_DECISION, RandomUtils.generateString()));
////        udf.setSecondUdfString(generateUdfString(UDF_SDFEATURE_IMPLSTATEMENT, RandomUtils.generateString()));
////        task.refreshUdf(udf);
////        gapSolutionController.performCommonOperation(task, CHANGE_DECISION);
////        ApiAsserts.assertThat(gapSolutionController.getResponse())
////                .isCorrectResponseCode(HTTP_OK)
////                .isParseableBody(TaskResponseBody.class);
//    }
//
//    @Test(groups = {"GapSolution", "Regression"}, description = "Включить GAP лист по модулю", dependsOnMethods = "changeDecision")
//    public void inClientGapList() {
////        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
////        udf = refreshUdf();
////        udf.setUdfList(generateUdfList(UDF_SOLUTION_PERIOD, MEDIUM_TERM));
////        udf.setSecondUdfList(generateUdfList(UDF_SOLUTION_IMPLTYPE, NKP));
////        task.refreshUdf(udf);
////        gapSolutionController.performCommonOperation(task, IN_CLIENT_GAP_LIST);
////        ApiAsserts.assertThat(gapSolutionController.getResponse())
////                .isCorrectResponseCode(HTTP_OK)
////                .isParseableBody(TaskResponseBody.class);
//    }
//
//    @Test(groups = {"GapSolution", "Regression"}, description = "Вернуть на анализ", dependsOnMethods = "inClientGapList")
//    public void returnToClient() {
////        apiController.updateToken(generateAuthToken(CLIENT));
////        udf = refreshUdf();
////        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ARTEMEVA_MARINA));
////        task.refreshUdf(udf);
////        task.setHandlerUser(generateUser(ARTEMEVA_MARINA));
////        gapSolutionController.performCommonOperation(task, RETURN_TO_CLIENT);
////        ApiAsserts.assertThat(gapSolutionController.getResponse())
////                .isCorrectResponseCode(HTTP_OK)
////                .isParseableBody(TaskResponseBody.class);
//    }
//
//    @Test(groups = {"GapSolution", "Regression"}, description = "Назначить наблюдателя", dependsOnMethods = "returnToClient")
//    public void watch() {
////        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
////        udf = refreshUdf();
////        udf.setUdfUser(generateUdfUser(UDF_WATCHER, ABDULLAEV_BAHODIR));
////        task.refreshUdf(udf);
////        gapSolutionController.performCommonOperation(task, WATCH);
////        ApiAsserts.assertThat(gapSolutionController.getResponse())
////                .isCorrectResponseCode(HTTP_OK)
////                .isParseableBody(TaskResponseBody.class);
//    }
//
//    @Test(groups = {"GapSolution", "Regression"}, description = "Передать на внутреннее согласование", dependsOnMethods = "watch")
//    public void passForApprovalRetry() {
////        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
////        udf = refreshUdf();
////        udf.setUdfString(generateUdfString(UDF_REALIZATION_DECISION, RandomUtils.generateString()));
////        udf.setSecondUdfString(generateUdfString(UDF_SDFEATURE_IMPLSTATEMENT, RandomUtils.generateString()));
////        udf.setUdfList(generateUdfList(UDF_CUSTOMIZATION_FLAG, O30));
////        udf.setSecondUdfList(generateUdfList(UDF_LOCALIZATION_FLAG, YES_LOCAL_FLAG));
////        udf.setThirdUdfList(generateUdfList(UDF_WORKTASK_GENUSEFUTAG, YES_GAP_SECOND));
////        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ARUTYANIN_YURIY));
////        task.refreshUdf(udf);
////        task.setHandlerUser(generateUser(ARUTYANIN_YURIY));
////        gapSolutionController.performCommonOperation(task, PASS_FOR_APPROVAL);
////        ApiAsserts.assertThat(gapSolutionController.getResponse())
////                .isCorrectResponseCode(HTTP_OK)
////                .isParseableBody(TaskResponseBody.class);
//    }
//
//    @Test(groups = {"GapSolution", "Regression"}, description = "Включить GAP лист по модулю", dependsOnMethods = "passForApprovalRetry")
//    public void inClientGapListRetry() {
////        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
////        udf = refreshUdf();
////        udf.setUdfList(generateUdfList(UDF_SOLUTION_PERIOD, MEDIUM_TERM));
////        udf.setSecondUdfList(generateUdfList(UDF_SOLUTION_IMPLTYPE, NKP));
////        task.refreshUdf(udf);
////        gapSolutionController.performCommonOperation(task, IN_CLIENT_GAP_LIST);
////        ApiAsserts.assertThat(gapSolutionController.getResponse())
////                .isCorrectResponseCode(HTTP_OK)
////                .isParseableBody(TaskResponseBody.class);
//    }
//
//    @Test(groups = {"GapSolution", "Regression"}, description = "Согласовать решение", dependsOnMethods = "inClientGapListRetry")
//    public void approve() {
////        apiController.updateToken(generateAuthToken(CLIENT));
////        task.refreshUdf();
////        gapSolutionController.performCommonOperation(task, APPROVE);
////        ApiAsserts.assertThat(gapSolutionController.getResponse())
////                .isCorrectResponseCode(HTTP_OK)
////                .isParseableBody(TaskResponseBody.class);
//    }
//
//    @Test(groups = {"GapSolution", "Regression"}, description = "Комментарии", dependsOnMethods = "approve")
//    public void comment() {
////        apiController.updateToken(generateAuthToken(CLIENT));
////        task.refreshUdf();
////        gapSolutionController.performCommonOperation(task, COMMENT);
////        ApiAsserts.assertThat(gapSolutionController.getResponse())
////                .isCorrectResponseCode(HTTP_OK)
////                .isParseableBody(TaskResponseBody.class);
//    }
//
//    @Test(groups = {"GapSolution", "Regression"}, description = "Включить в сводный GAP лист", dependsOnMethods = "comment")
//    public void toConList() {
////        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
////        User user = userController.receiveRandomUser(task.getNumber(), STDT_HANDLER, TO_CONS_LIST, gapSolutionController.getTaskType());
////        task.setHandlerUser(generateUser(user));
//    }
//
//    @Test(groups = {"GapSolution", "Regression"}, description = "Приватный комментарий", dependsOnMethods = "toConList")
//    public void privateComment() {
////        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
////        task.refreshUdf();
////        gapSolutionController.performCommonOperation(task, PRIVATE_COMMENT);
////        ApiAsserts.assertThat(gapSolutionController.getResponse())
////                .isCorrectResponseCode(HTTP_OK)
////                .isParseableBody(TaskResponseBody.class);
//    }
//
//    @Test(groups = {"GapSolution", "Regression"}, description = "Передать на реализацию", dependsOnMethods = "privateComment")
//    public void passToRealize() {
////        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
////        udf = refreshUdf();
////        User user = userController.receiveRandomUser(task.getNumber(), STDT_HANDLER, TO_CONS_LIST, gapSolutionController.getTaskType());
////        udf.setUdfUser(generateUdfUser(STDT_HANDLER, user));
////        task.setHandlerUser(generateUser(user));
////        gapSolutionController.performCommonOperation(task, PASS_TO_REALIZE);
////        ApiAsserts.assertThat(gapSolutionController.getResponse())
////                .isCorrectResponseCode(HTTP_OK)
////                .isParseableBody(TaskResponseBody.class);
//    }
//
//    @Test(groups = {"GapSolution", "Regression"}, description = "Завершить реализацию", dependsOnMethods = "toConList")
//    public void finish() {
////        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
////        udf = refreshUdf();
////        User user = userController.receiveRandomUser(task.getNumber(), STDT_HANDLER, TO_CONS_LIST, gapSolutionController.getTaskType());
////        udf.setUdfUser(generateUdfUser(UDF_CLIENT_HANDLER, user));
////        task.refreshUdf(udf);
////        gapSolutionController.performCommonOperation(task, FINISH);
////        ApiAsserts.assertThat(gapSolutionController.getResponse())
////                .isCorrectResponseCode(HTTP_OK)
////                .isParseableBody(TaskResponseBody.class);
//    }
//}
