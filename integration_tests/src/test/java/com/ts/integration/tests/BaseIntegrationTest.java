package com.ts.integration.tests;

import com.ts.common.application.TrackStudioApiControllers;
import com.ts.common.tests.AbstractBaseTest;
import io.restassured.response.Response;
import org.testng.annotations.BeforeSuite;

public class BaseIntegrationTest extends AbstractBaseTest {
    protected Response response;
    @BeforeSuite(alwaysRun = true)
    public void setUp() {
        apiController = new TrackStudioApiControllers();
    }
}
