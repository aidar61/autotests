package com.ts.integration.tests.ui.experimental;

import com.ts.common.controllers.BaseController;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.equalTo;

public class CheckOperationPermission extends BaseIntegrationTest {
    BaseController baseControler;

    @Test
    public void checkMsgSdpatchWatchPermissions(){
        baseControler = apiController.getBaseController();
        response = baseControler.getOpretionPermission("MSG_SDPATCH_WATCH");
        response.then()
                .body("$.[0].role.id", equalTo("ROLE_WORKER"));
//                .body("$.[1].role.id", equalTo("ROLE_SUPPORT_MANAGER"))
//                .body("$.[2].role.id", equalTo("ROLE_GROUP_TECHNOLOGYSERVICE"))
//                .body("$.[3].role.id", equalTo("ROLE_MAIN_ADMINISTRATOR"));

    }

}
