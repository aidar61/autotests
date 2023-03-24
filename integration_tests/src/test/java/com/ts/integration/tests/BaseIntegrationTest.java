package com.ts.integration.tests;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.application.controllers.TrackStudioApiControllers;
import com.ts.common.application.database.DbHelper;
import com.ts.common.listeners.LogCatchListener;
import com.ts.common.tests.AbstractBaseTest;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

@Slf4j
@Listeners({LogCatchListener.class})
public class BaseIntegrationTest extends AbstractBaseTest {
    protected Response response;
    protected AuthToken authToken;

    @BeforeSuite(alwaysRun = true)
    public void setUp() {
        apiController = new TrackStudioApiControllers();
        dbHelper = new DbHelper();
        log.warn("=====================API TESTS IS STARTED=====================");
    }
}
