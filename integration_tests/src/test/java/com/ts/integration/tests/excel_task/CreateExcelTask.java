package com.ts.integration.tests.excel_task;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.CreateFromExcelTaskController;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Status;
import com.ts.common.entitites.commonEntities.Task;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.udf.UdfList;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.services.ExcelService;
import com.ts.common.services.models.TaskFromExcelModel;
import com.ts.common.utils.InitEntities;
import com.ts.integration.tests.BaseIntegrationTest;
import io.restassured.path.json.JsonPath;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.TaskStatuses.STATUS_WORKTASK_ASSIGNED;
import static com.ts.common.utils.InitEntities.*;

public class CreateExcelTask extends BaseIntegrationTest {
    public CreateFromExcelTaskController controller;
    private GeneralTask task;
    private List<TaskFromExcelModel> tasksFromExcel;
    private static final String pathToFile = "src/test/resources/tasks.xlsx";
    private static final String parentNumber = "849976";


    @BeforeClass(alwaysRun = true)
    public void beforeClass() throws IOException {
        controller = apiController.getCreateFromExcelTaskController();
        userController = apiController.getUserController();
        task = InitEntities.getGeneralTask(TaskType.CAT_CFGTASK, Operations.CAT);
        tasksFromExcel = ExcelService.readFromExcel(pathToFile);
    }

    @Test(groups = {"SdQuestion", "Regression"}, description = "создание CAT_SDQUESTION")
    public void createTask() {
        apiController.updateToken(new AuthToken("root", "password"));
        for (TaskFromExcelModel task1 : tasksFromExcel) {
            controller.getParentPayload(parentNumber);
            var module = controller.getModuleByName(task1.getUDF_SD_MODULE(), parentNumber);
            var misService = controller.getParentUdfListValueSelector(task1.getUDF_MIS_SERVICE(), UDF_MIS_SERVICE);
            var cdpBl = controller.getParentUdfListValueSelector(task1.getUDF_CDP_BL(), UDF_CDP_BL);
            var taskPriority = new Status();
            taskPriority.setId(controller.getParentFieldId(task1.getPriority(), "priorities").getId());
            var bdku = controller.getParentUdfTask(UDF_BDKU_CONFIGURATION.udfId);
            var workTaskAnalysis = controller.getParentUdfListValueSelector(task1.getWorkTaskAnalysis(), UDF_WORKTASK_ANALYSIS);
            task.setName(task1.getName());
            task.setDescription(task1.getDescription());
            task.setParent(new Parent("818181df67e816130167eb1d99d41c73", parentNumber));
            task.setPriority(taskPriority);
            udf = refreshUdf();
            udf.setUdfList(generateUdfList(UDF_CDP_BL, cdpBl.getId()));
            udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, module));
            udf.setSecondUdfList(generateUdfList(UDF_MIS_SERVICE, misService.getId()));
            udf.setThirdUdfList(generateUdfList(UDF_CDP_ACCEPTANCE, "ff8081813fce5b48013fce5de6b40002"));
            udf.setFourthUdfList(generateUdfList(UDF_WORKTASK_ANALYSIS, workTaskAnalysis.getId()));
            udf.setSecondUdfTask(generateUdfTask(UDF_PRODUCT, bdku.getTaskValue()[0]));
            udf.setThirdUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, new Task("8a8181df6c02a6a3016c055825e26bd7", "931164")));
            task.refreshUdf(udf);
            controller.createExcelTask(task);
            var response = controller.getResponse();
            ApiAsserts.assertThat(response)
                    .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK)
                    .isParseableBody(TaskResponseBody.class)
                    .assertTask()
                    .isEquals(task);
        }
    }
}
