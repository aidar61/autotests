package com.ts.integration.tests;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.application.controllers.TrackStudioApiControllers;
import com.ts.common.application.database.DbHelper;
import com.ts.common.controllers.sla.BaseSlaController;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.sla.SlaTask;
import com.ts.common.enums.Users;
import com.ts.common.listeners.LogCatchListener;
import com.ts.common.tests.AbstractBaseTest;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

import static com.ts.common.utils.InitEntities.generateAuthToken;

@Slf4j
@Listeners({LogCatchListener.class})
public class BaseIntegrationTest extends AbstractBaseTest {
    protected Response response;
    protected AuthToken authToken;
    protected BaseSlaController slaController;
    protected SlaTask slaTask;
    protected Udfs udf;

    @BeforeSuite(alwaysRun = true)
    public void setUp() {
        this.authToken = generateAuthToken(Users.CLIENT);
        apiController = new TrackStudioApiControllers(authToken);
        slaController = apiController.getSlaController();
        dbHelper = new DbHelper();
        log.warn("=====================API TESTS IS STARTED=====================");
    }
}
