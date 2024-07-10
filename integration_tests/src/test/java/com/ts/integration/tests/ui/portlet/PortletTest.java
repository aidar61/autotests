package com.ts.integration.tests.ui.portlet;

import com.codeborne.selenide.Condition;
import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.portlet.GetUserProjectResponseBody;
import com.ts.common.controllers.portlet.PortletController;
import com.ts.common.entitites.commonEntities.Role;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.UserProjectAssign;
import com.ts.common.ui.pages.PortletPage;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseUiTest;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.ts.common.application.controllers.TrackStudioEndPoints.*;
import static com.ts.common.config.AppConfigProvider.STAND_URL;
import static com.ts.common.config.AppConfigProvider.getUserConfig;
import static com.ts.common.request.ApiRequest.getEndpoint;
import static org.testng.Assert.assertTrue;

public class PortletTest extends BaseUiTest {

    PortletController portletController;
    UserProjectAssign userProjectAssign;
    private User user;
    private PortletPage portletPage;

    @BeforeClass(alwaysRun = true)
    void setup() {
        portletPage = trackStudioPages.getPortletPage();
        user = userController.getUserBy(userRoles, Role.RoleConstants.ROLE_WORKER, getUserConfig().at_support_manager());
        portletController = apiController.getPortletController();
        userProjectAssign = InitEntities.generateProjectAssign(user, "PRJ-858", "8181839e61e1114d0161e19dc34a003a", 100);

        portletController.assignUserProject(userProjectAssign);
        ApiAsserts.assertThat(portletController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);

        GetUserProjectResponseBody assignedUserProjects = portletController.getAssignedUserProjects(userProjectAssign);
        Assertions.assertThat(assignedUserProjects.getProjectsData())
                .isNotNull();
    }

    @Test(groups = {"UI", "PORTLET"}, description = "Сценарий для демонстрации фреймворка")
    void checkAvailabilityEditSaveButtonOnPortletTest() {
        auth(getEndpoint(STAND_URL, APP, PORTLET, CONTROL_PLAN), "root");

        portletPage.chooseUser(user.getLogin())
                .refresh()
                .edit();

        assertTrue(portletPage.getDeleteButton().shouldBe(Condition.visible).isDisplayed(), "Delete button is not displayed");
        assertTrue(portletPage.getSaveButton().shouldBe(Condition.visible).isDisplayed(), "Save button is not displayed");
    }
}
