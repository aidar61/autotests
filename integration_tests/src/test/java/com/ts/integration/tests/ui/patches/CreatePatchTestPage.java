package com.ts.integration.tests.ui.patches;

import com.ts.common.application.database.DbHelper;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.controllers.folder.SdPatchFolderController;
import com.ts.common.controllers.installation.InstallationController;
import com.ts.common.entitites.BaseEntity;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.ui.pages.LoginPage;
import com.ts.common.ui.pages.portlets.PatchPage;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseUiTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.codeborne.selenide.Selenide.open;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.UDF_BDKU_CSCCLIENT;
import static com.ts.common.enums.TaskType.BDKU_INSTALLATION;
import static com.ts.common.enums.TaskType.SDPATCHFOLDER;
import static com.ts.common.utils.InitEntities.generateUdfString;


public class CreatePatchTestPage extends BaseUiTest {
    LoginPage loginPage;
    PatchPage patchPage;
    InstallationController installationController;
    SdPatchFolderController sdPatchFolderController;
    GeneralTask generalTask;
    Udfs udf;
    GrTaskTable grTaskTable;
    Parent parent;

    String url = "http://tsdev7.dev.colvir.ru/TrackStudio/app/portlet/patch?branch=AZPST";

    @BeforeClass(alwaysRun = true)
    public void beforeClass() {
        loginPage = trackStudioPages.getLoginPage();
        patchPage = trackStudioPages.getPatchPage();

        installationController = apiController.getInstallationController();
        sdPatchFolderController = apiController.getSdPatchfolderController();
        //grTaskTable = dbHelper.getGrTaskTable();
        //GrTaskDbEntity grTaskDbEntity = (GrTaskDbEntity) grTaskTable.receiveByTaskNumber("462311");

        parent = InitEntities.generateParent("818180a050c582480150c947cb252eb5", "462311");
        generalTask = InitEntities.getGeneralTask(BDKU_INSTALLATION, Operations.CAT);
        udf = InitEntities.refreshUdf();
        generalTask.setName("AT_INSTALLATION");
        generalTask.setParent(parent);
        udf.setUdfString(generateUdfString(UDF_BDKU_CSCCLIENT, "AT_INSTALLATION"));
        generalTask.refreshUdf(udf);
        installationController.createInstallation(generalTask);

        parent = InitEntities.generateParent(generalTask.getId(), generalTask.getNumber());
        generalTask = InitEntities.getGeneralTask(SDPATCHFOLDER, Operations.CAT);
        udf = InitEntities.refreshUdf();
        generalTask.setName("AT_SDPATCHFOLDER");
        generalTask.setParent(parent);
        //udf.setUdfString(generateUdfString(UDF_BDKU_CSCCLIENT, "AT_INSTALLATION"));
        generalTask.refreshUdf(udf);
        sdPatchFolderController.createSdPatchFolder(generalTask);

    }

    @Test
    public void createByAdmin() {
        String user = "root";

        open(url);
        loginPage.loginNoToken(user);
        patchPage.setConfiguration()
                .openTaskForm()
                .saveTaskForm();


    }
}
