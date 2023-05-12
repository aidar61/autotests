package com.ts.integration.tests.proc_sla_help;


import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.sla.SlaResponseBody;
import com.ts.common.controllers.sla.slaHelp.SlaHelpController;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.ComSlaOperations;
import com.ts.common.enums.SlaType;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.entitites.commonEntities.List.Constants.ACCUPDLST;
import static com.ts.common.entitites.commonEntities.Task.Constants.*;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.entitites.commonEntities.User.Constants.AKSENOV_ANDREY;
import static com.ts.common.entitites.commonEntities.User.Constants.ALTUNIN_NIKOLAY;
import static com.ts.common.enums.ComSlaOperations.*;
import static com.ts.common.enums.Users.EMPLOYEE;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.generateString;

public class SlaHelp1Test extends BaseIntegrationTest {
    private SlaHelpController slaHelpController;
    private GeneralTask slaTask;

    @BeforeClass
    public void beforeClass() {
        slaHelpController = apiController.getSlaHelpController();
    }

    @Test(groups = {"SlaHelp", "Regression"}, description = "Создание запроса на консультацию")
    public void catSlaHelp() {
        udf = refreshUdf();
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, AKKREDITIVES));
        udf.setSecondUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, MTBANK));
        slaTask = InitEntities.getSlaTask(SlaType.SLA_HElP, ComSlaOperations.CAT);
        slaTask.refreshUdf(udf);
        slaHelpController.createTask(slaTask);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(groups = {"SlaHelp", "Regression"}, description = "Принять на анализ", dependsOnMethods = "catSlaHelp")
    public void msgSlaHelpAnalize() {
        apiController.updateToken(generateAuthToken(EMPLOYEE));
        udf = refreshUdf();
        udf.setUdfUser(generateUdfUser(STDT_HANDLER, ALTUNIN_NIKOLAY));
        slaTask.setHandlerUser(generateUser(ALTUNIN_NIKOLAY));
        slaTask.refreshUdf(udf);
        slaHelpController.performCommonOperation(slaTask, ANALIZE);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(groups = {"SlaHelp", "Regression"}, description = "Изменить модуль системы", dependsOnMethods = "msgSlaHelpAnalize")
    public void msgSlaHelpChangeSdModule() {
        udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_SD_TASK_CODE, ACCUPDLST));
        udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, HEAD_BOOK));
        slaTask.refreshUdf(udf);
        slaHelpController.performCommonOperation(slaTask, CHANGE_MODULE);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(groups = {"SlaHelp", "Regression"}, description = "Изменить модуль системы", dependsOnMethods = "msgSlaHelpChangeSdModule")
    public void msgSlaHelpChangeAuthor() {
        udf = refreshUdf();
        udf.setUdfString(generateUdfString(UDF_SD_INITPERSON, generateString()));
        udf.setUdfUser(generateUdfUser(UDF_SD_AUTHORCLIENT_MSG, AKSENOV_ANDREY));
        slaTask.refreshUdf(udf);
        slaHelpController.performCommonOperation(slaTask, CHANGE_AUTHOR);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(groups = {"SlaHelp", "Regression"}, description = "Изменить список связанных задач", dependsOnMethods = "msgSlaHelpChangeAuthor")
    public void msgSlaHelpChangeLinkedTasks() {
        udf = refreshUdf();
        udf.setUdfTask(generateUdfTask(UDF_SD_LINKEDREQUEST, SERVICE_DESK));
        slaTask.refreshUdf(udf);
        slaHelpController.performCommonOperation(slaTask, CHANGE_LINKED_TASKS);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }

    @Test(groups = {"SlaHelp", "Regression"}, description = "Корректировка сорков SLA", dependsOnMethods = "msgSlaHelpChangeLinkedTasks")
    public void msgSlaHelpCorrectSlaDates() {
        udf = refreshUdf();
        udf.setUdfString(generateUdfString(UDF_SLA_CONSULTPROVIDEDATE, "1683797653000"));
        slaTask.refreshUdf(udf);
        slaHelpController.performCommonOperation(slaTask, CORRECT_SLA_DATES);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(SlaResponseBody.class);
    }
}
