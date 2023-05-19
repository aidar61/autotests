package com.ts.integration.tests.proc_sla_help;

import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.sla.SlaHelpController;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.ComSlaOperations;
import com.ts.common.enums.SlaType;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.RandomUtils;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.entitites.commonEntities.Task.Constants.AKKREDITIVES;
import static com.ts.common.entitites.commonEntities.Task.Constants.MTBANK;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.ComSlaOperations.*;
import static com.ts.common.enums.Users.CLIENT;
import static com.ts.common.utils.InitEntities.*;

public class SlaHelp2Test extends BaseIntegrationTest {
    private SlaHelpController slaHelpController;
    private GeneralTask slaTask;

    @BeforeClass(alwaysRun = true)
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
        apiController.updateToken(generateAuthToken(CLIENT));
        slaHelpController.createTask(slaTask);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaHelp", "Regression"}, description = "Задать вопрос", dependsOnMethods = "catSlaHelp")
    public void msgSlaHelpCliComment() {
        slaTask.refreshUdf();
        apiController.updateToken(generateAuthToken(CLIENT));
        slaHelpController.performCommonOperation(slaTask, CLI_COMMENT);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaHelp", "Regression"}, description = "Сообщить информацию( Комментарий )", dependsOnMethods = "msgSlaHelpCliComment")
    public void msgSlaHelpOurComment() {
        slaTask.refreshUdf();
        apiController.updateToken(generateAuthToken(CLIENT));
        slaHelpController.performCommonOperation(slaTask, COMMENT);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaHelp", "Regression"}, description = "Изменить аттрибуты запросы", dependsOnMethods = "msgSlaHelpOurComment")
    public void msgSlaHelpChangeAttrs() {
        udf = refreshUdf();
        udf.setUdfString(generateUdfString(UDF_SD_REMOTEID, RandomUtils.generateString()));
        udf.setSecondUdfString(generateUdfString(UDF_SD_INITPERSON, RandomUtils.generateString()));
        slaTask.refreshUdf(udf);
        apiController.updateToken(generateAuthToken(CLIENT));
        slaHelpController.performCommonOperation(slaTask, CHANGE_ATTR);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

    @Test(groups = {"SlaHelp", "Regression"}, description = "Изменить аттрибуты запросы", dependsOnMethods = "msgSlaHelpChangeAttrs")
    public void msgSlaHelpAppointClientWatchers() {
        udf = refreshUdf();
        udf.setUdfString(generateUdfString(UDF_SD_CLIENTWATCHERS, RandomUtils.generateEmail()));
        slaTask.refreshUdf(udf);
        apiController.updateToken(generateAuthToken(CLIENT));
        slaHelpController.performCommonOperation(slaTask, ADD_CLIENT_WATCHERS);
        ApiAsserts.assertThat(slaHelpController.getResponse())
                .isCorrectResponseCode(HTTP_OK)
                .isParseableBody(TaskResponseBody.class);
    }

}
