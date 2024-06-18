package com.ts.integration.tests.settings;

import com.ts.common.asserts.SettingsAssert;
import com.ts.common.controllers.settings.SettingsController;
import com.ts.common.entitites.commonEntities.Role;
import com.ts.common.entitites.helpers.Permissions;
import com.ts.common.entitites.helpers.Transition;
import com.ts.common.enums.Permission;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.ts.common.entitites.commonEntities.Role.RoleConstants.*;
import static com.ts.common.enums.Operations.WATCH;
import static com.ts.common.enums.Permission.*;
import static com.ts.common.enums.TaskType.SDPATCH;

public class SettingsTest extends BaseIntegrationTest {
    SettingsController settingsController;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        settingsController = apiController.getSettingsController();
    }

    @Test(groups = {"Settings", "Regression"}, description = "Проверка настроек на операцию MSG_SDPATCH_WATCH")
    void settingMsgSdPatchWatch() {
        Permissions[] actualPermissions = settingsController.getPermissionsFor(SDPATCH, WATCH);
        SettingsAssert.assertThat(actualPermissions)
                .isHavePermission(
                        ROLE_MAIN_ADMINISTRATOR,
                        VIEW_OPERATION, PROCESS_OPERATION
                )
                .isHavePermission(
                        ROLE_WORKER,
                        VIEW_OPERATION, PROCESS_OPERATION
                )
                .isHavePermission(
                        ROLE_GROUP_TECHNOLOGYSERVICE,
                        VIEW_OPERATION, PROCESS_OPERATION
                )
                .isHavePermission(
                        ROLE_SUPPORT_MANAGER,
                        VIEW_OPERATION, PROCESS_OPERATION
                );

        Transition[] transitions = settingsController.getTransitionsFor(SDPATCH, WATCH);
        SettingsAssert.assertThat(transitions)
                .isCorrectTransitionStatusName("Планирование")
                .isCorrectTransitionStatusName("Тиражирование и корректировка")
                .isCorrectTransitionStatusName("Формирование")
                .isCorrectTransitionStatusName("Функциональное тестирование")
                .isCorrectTransitionStatusName("Технологическое тестирование")
                .isCorrectTransitionStatusName("Поставка (отправлен)")
                .isCorrectTransitionStatusName("Эксплуатация");

    }
}
