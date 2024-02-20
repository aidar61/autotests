package com.ts.integration.tests.performance_tests;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.performance.PerformanceController;
import com.ts.common.enums.Performance;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.enums.Performance.PROJECT_INTEGRATION;
import static com.ts.common.enums.Performance.PROJECT_MAINTENANCE;

public class PerformanceTests extends BaseIntegrationTest {

    PerformanceController performanceController;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        performanceController = apiController.getPerformanceController();
    }

    @Test(groups = {"Regression", "Performance"}, description = "Загрузка портлета \"Оперативный контроль доработок\" по всем проектам сопровождения")
    public void getControlOperationOfMaintenance() {
        performanceController.getOperationControlOf(PROJECT_MAINTENANCE);
        ApiAsserts.assertThat(performanceController.getResponse())
                .isCorrectResponseCode(HTTP_OK);
    }

    @Test(groups = {"Performance"}, description = "Загрузка портлета \"Оперативный контроль доработок\" по всем проектам внедрения")
    public void getControlOperationOfIntegration() {
        performanceController.getOperationControlOf(PROJECT_INTEGRATION);
        ApiAsserts.assertThat(performanceController.getResponse())
                .isCorrectResponseCode(HTTP_OK);
    }
}
