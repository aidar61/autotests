package com.ts.integration.tests.excel_task;

import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.CreateFromExcelTaskController;
import com.ts.common.entitites.commonEntities.*;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.enums.TaskType;
import com.ts.common.services.ExcelService;
import com.ts.common.services.models.TaskFromExcelModel;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.StringUtils;
import com.ts.integration.tests.BaseIntegrationTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.utils.InitEntities.*;

public class CreateExcelTask extends BaseIntegrationTest {
    private static final String pathToFile = "src/test/resources/tasks.xlsx";
    private static final String parentNumber = "849976";
    public CreateFromExcelTaskController controller;
    private GeneralTask task;
    private List<TaskFromExcelModel> tasksFromExcel;
    private List<UserRole> userRoles;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() throws IOException {
        controller = apiController.getCreateFromExcelTaskController();
        userController = apiController.getUserController();
        task = InitEntities.getGeneralTask(TaskType.CAT_CFGTASK, Operations.CAT);
        tasksFromExcel = ExcelService.readFromExcel(pathToFile);
    }

    @Test(groups = {"EXCEL"}, description = "создание CAT_SDQUESTION")
    public void createTask() {
        int j = 0;
        for (TaskFromExcelModel task1 : tasksFromExcel) {
            controller.getParentPayload(parentNumber);
            getParticipants(parentNumber);
            var creator = getByName(task1.getCreator());
            var handler = getByName(task1.getHandler());
            var watcher = getByName(task1.getUDF_WATCHER());

            apiController.updateToken(generateAuthToken(creator));
            var cdpBl = controller
                    .getParentUdfListValueSelector(task1.getUDF_CDP_BL(), UDF_CDP_BL);
            var module = controller
                    .getModuleByName(task1.getUDF_SD_MODULE(), parentNumber);
            var misService = controller
                    .getParentUdfListValueSelector(task1.getUDF_MIS_SERVICE(), UDF_MIS_SERVICE);
            var taskPriority = Status
                    .builder()
                    .id(controller.getParentFieldId(task1.getPriority(), "priorities").getId())
                    .build();
            var workTaskAnalysis = task1.getUDF_WORKTASK_ANALYSIS().equals("Не требуется") ?
                    UDF_WORKTASK_ANALYSIS_NO : UDF_WORKTASK_ANALYSIS_YES;
            var bdkuConfiguration = Task
                    .builder()
                    .number(StringUtils.getTaskNumber(task1.getUDF_BDKU_CONFIGURATION()))
                    .build();
            var linkedTask = Task
                    .builder()
                    .number(StringUtils.getTaskNumber(task1.getUDF_SD_LINKEDREQUEST()))
                    .build();
            var udfProduct = controller.getUdfProduct().getTaskValueSelector()[0];

            task.setName(task1.getName());
            task.setDescription(task1.getDescription());
            task.setParent(new Parent("818181df67e816130167eb1d99d41c73", parentNumber));
            task.setPriority(taskPriority);
            udf = refreshUdf();
            udf.setUdfList(generateUdfList(UDF_CDP_BL, cdpBl.getId()));
            udf.setUdfTask(generateUdfTask(UDF_SD_MODULE, module));
            udf.setSecondUdfList(generateUdfList(UDF_MIS_SERVICE, misService.getId()));
            udf.setFourthUdfList(generateUdfList(UDF_WORKTASK_ANALYSIS, workTaskAnalysis.getId()));
            udf.setSecondUdfTask(generateUdfTask(UDF_SD_LINKEDREQUEST, linkedTask));
            udf.setThirdUdfTask(generateUdfTask(UDF_BDKU_CONFIGURATION, bdkuConfiguration));
            udf.setFourthUdfTask(generateUdfTask(UDF_PRODUCT, udfProduct));
            udf.setFifthUdfList(generateUdfList(UDF_CDP_ACCEPTANCE, UDF_CDP_ACCEPTANCE_NO));
            if (handler != null) {
                task.setHandlerUser(handler);
                udf.setSecondUdfUser(generateUdfUser(STDT_HANDLER, handler));
            }
            if (watcher != null) {
                udf.setUdfUser(generateUdfUser(UDF_WATCHER, watcher));
            }
            task.refreshUdf(udf);
            controller.createExcelTask(task);
            var response = controller.getResponse();
            ApiAsserts.assertThat(response)
                    .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);

            var addTagResponse = controller.addTag(task.getNumber(), Tag
                    .builder()
                    .tag(task1.getTag())
                    .build());
            ApiAsserts.assertThat(addTagResponse)
                    .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
//            CommonAssert
//                    .assertThat(response)
//                    .isCorrectUdfList(UDF_CDP_BL, cdpBl.getId())
//                    .isCorrectUdfList(UDF_MIS_SERVICE, misService.getId())
//                    .isCorrectUdfList(UDF_WORKTASK_ANALYSIS, workTaskAnalysis.getId())
//                    .isCorrectUdfList(UDF_CDP_ACCEPTANCE, UDF_CDP_ACCEPTANCE_NO.getId())
//                    .isCorrectUdfTask(UDF_SD_MODULE, module.getNumber())
//                    .isCorrectUdfTask(UDF_BDKU_CONFIGURATION, bdkuConfiguration.getNumber())
//                    .isCorrectUdfTask(UDF_PRODUCT, udfProduct.getNumber());

            System.out.printf("======================================TASK NUMBER IS %s=======================================", j++);
        }
    }

    private void getParticipants(String parentNumber) {
        userRoles = userController.receiveUserByTask(parentNumber);
    }

    private User getByName(String name) {
        return Objects.requireNonNullElse(userRoles.stream()
                .filter(f -> f.getForUser().getName().equals(name) &&
                        f.getForUser().getActive())
                .findFirst().orElse(null), new UserRole()).getForUser();
    }
}
