package com.ts.integration.tests.proc_gap_solution;

import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.gap.GapSolutionController;
import com.ts.common.controllers.gap.PotentialGapController;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.RandomUtils;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.FRONT_OFFICE;
import static com.ts.common.entitites.commonEntities.Task.Constants.SERVICE_DESK;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.*;
import static com.ts.common.enums.Operations.*;
import static com.ts.common.enums.TaskType.SOL_SELECTED;
import static com.ts.common.enums.Users.SECOND_EMPLOYEE;
import static com.ts.common.utils.InitEntities.*;

public class GapSolution1Test extends BaseIntegrationTest {
    private PotentialGapController potentialGapController;
    private GapSolutionController gapSolutionController;
    private GeneralTask task;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        potentialGapController = apiController.getPotentialGapController();
        gapSolutionController = apiController.getGapSolutionController();
    }

    @Test(groups = {"PROC_SOLUTION", "Regression"}, description = "Создание потенциального Gap")
    public void catPotentialGap() {
        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
        udf = refreshUdf();
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, FRONT_OFFICE));
        task = InitEntities.getGeneralTask(TaskType.POTENTIAL_GAP, Operations.CAT);
        task.setHandlerUser(generateUser(ARTEMEVA_MARINA));
        task.refreshUdf(udf);
        potentialGapController.createPotentialGap(task);
        ApiAsserts.assertThat(potentialGapController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"PROC_SOLUTION", "Regression"}, description = "Передать на согласование", dependsOnMethods = "catPotentialGap")
    public void msgGapPassForApproval() {
        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ARUTYANIN_YURIY));
        task.setHandlerUser(generateUser(ARUTYANIN_YURIY));
        task.refreshUdf(udf);
        potentialGapController.performCommonOperation(task, PASS_FOR_APPROVAL);
        ApiAsserts.assertThat(potentialGapController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"PROC_SOLUTION", "Regression"}, description = "Подтвердить и опубликовать", dependsOnMethods = "msgGapPassForApproval")
    public void msgGapConfirm() {
        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ARTEMEVA_MARINA));
        task.setHandlerUser(generateUser(ARTEMEVA_MARINA));
        task.refreshUdf(udf);
        potentialGapController.performCommonOperation(task, CONFIRM);
        ApiAsserts.assertThat(potentialGapController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"PROC_SOLUTION", "Regression"}, description = "Создать решение GAP", dependsOnMethods = "msgGapConfirm")
    public void catSolSelected() {
        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
        Parent parent = generateParent(task.getId(), task.getNumber());
        task = InitEntities.getGeneralTask(SOL_SELECTED, CAT, parent);
        task.setHandlerUser(generateUser(ARTEMEVA_MARINA));
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(UDF_WATCHER, ABDULLAEV_BAHODIR));
        udf.setUdfString(generateUdfString(UDF_REALIZATION_DECISION, RandomUtils.generateString()));
        udf.setSecondUdfString(generateUdfString(UDF_SDFEATURE_IMPLSTATEMENT, RandomUtils.generateString()));
        udf.setUdfList(generateUdfList(UDF_SOLUTION_PERIOD, MEDIUM_TERM));
        udf.setSecondUdfList(generateUdfList(UDF_CUSTOMIZATION_FLAG, YES_GAP));
        udf.setThirdUdfList(generateUdfList(UDF_LOCALIZATION_FLAG, NO_GAP));
        udf.setFourthUdfList(generateUdfList(UDF_WORKTASK_GENUSEFUTAG, YES_GAP_SECOND));
        task.refreshUdf(udf);
        gapSolutionController.createGapSolution(task);
        ApiAsserts.assertThat(gapSolutionController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"PROC_SOLUTION", "Regression"}, description = "Изменить аттрибуты решения", dependsOnMethods = "catSolSelected")
    public void msgGapSolutionChange() {
        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
        udf = refreshUdf();
        udf.setUdfString(generateUdfString(UDF_REALIZATION_DECISION, RandomUtils.generateString()));
        udf.setSecondUdfString(generateUdfString(UDF_SDFEATURE_IMPLSTATEMENT, RandomUtils.generateString()));
        udf.setUdfList(generateUdfList(UDF_SOLUTION_PERIOD, NOW));
        udf.setSecondUdfList(generateUdfList(UDF_CUSTOMIZATION_FLAG, O30));
        udf.setThirdUdfList(generateUdfList(UDF_LOCALIZATION_FLAG, YES_LOCAL_FLAG));
        udf.setFourthUdfList(generateUdfList(UDF_SOLUTION_IMPLTYPE, KP));
        udf.setFifthUdfList(generateUdfList(UDF_WORKTASK_GENUSEFUTAG, NO_GENESEFU));
        task.refreshUdf(udf);
        gapSolutionController.performCommonOperation(task, CHANGE);
        ApiAsserts.assertThat(gapSolutionController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"PROC_SOLUTION", "Regression"}, description = "Приватный комментариуй", dependsOnMethods = "msgGapSolutionChange")
    public void msgGapSolutionPrivateComment() {
        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
        task.refreshUdf();
        gapSolutionController.performCommonOperation(task, PRIVATE_COMMENT);
        ApiAsserts.assertThat(gapSolutionController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"PROC_SOLUTION", "Regression"}, description = "Изменить список связанных задач", dependsOnMethods = "msgGapSolutionPrivateComment")
    public void msgGapSolutionChangeLinkedTask() {
        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
        udf = refreshUdf();
        udf.setUdfTask(generateUdfTask(UDF_SD_LINKEDREQUEST, SERVICE_DESK));
        task.refreshUdf(udf);
        gapSolutionController.performCommonOperation(task, CHANGE_LINKED_TASK);
        ApiAsserts.assertThat(gapSolutionController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"PROC_SOLUTION", "Regression"}, description = "Снять решение", dependsOnMethods = "msgGapSolutionChangeLinkedTask")
    public void msgGapSolutionCancel() {
        apiController.updateToken(generateAuthToken(SECOND_EMPLOYEE));
        task.refreshUdf();
        gapSolutionController.performCommonOperation(task, CANCEL);
        ApiAsserts.assertThat(gapSolutionController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }
}
