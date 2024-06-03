package com.ts.common.generators;

import com.ts.common.application.controllers.TrackStudioApiControllers;
import com.ts.common.application.controllers.TrackStudioHttpStatusCodes;
import com.ts.common.application.database.DbHelper;
import com.ts.common.application.database.dbEntities.GrTaskDbEntity;
import com.ts.common.application.database.dbTables.GrTaskTable;
import com.ts.common.asserts.ApiAsserts;
import com.ts.common.controllers.project.ProjectController;
import com.ts.common.controllers.settings.UdfController;
import com.ts.common.entitites.commonEntities.List;
import com.ts.common.entitites.commonEntities.Parent;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.enums.Operations;
import com.ts.common.utils.InitEntities;
import lombok.Getter;

import java.util.Arrays;

import static com.ts.common.entitites.commonEntities.List.Constants.NO_PROJECT_MANAGING;
import static com.ts.common.entitites.commonEntities.List.Constants.PROJECT_VIEWERS;
import static com.ts.common.entitites.commonEntities.Status.Priority.PRIORITY;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.TaskType.GENPLAN;
import static com.ts.common.enums.TaskType.GROUP_TASKS;
import static com.ts.common.utils.InitEntities.*;

public class TaskGenerator {
    @Getter
    private GeneralTask genPlan;
    @Getter
    private GeneralTask groupTask;
    @Getter
    private List atCdpCustomer;
    private final ProjectController projectController;
    private final UdfController udfController;
    private final GrTaskTable grTaskTable;

    private TaskGenerator(TrackStudioApiControllers apiController, DbHelper dbHelper) {
        this.projectController = apiController.getProjectController();
        this.udfController = apiController.getUdfController();
        this.grTaskTable = dbHelper.getGrTaskTable();
    }

    public static TaskGenerator create(TrackStudioApiControllers apiController, DbHelper dbHelper) {
        return new TaskGenerator(apiController, dbHelper);
    }

    public void generateTasks(String project) {
        atCdpCustomer = generateList("AT_CDP_CUSTOMER", "000");
        atCdpCustomer = setValueToList(atCdpCustomer, UDF_CDP_CUSTOMER);
        createGroupTask(project);
        createGenPlan();
    }

    private void createGroupTask(String project) {
        String taskName = "AT_GROUPTASKS";
        GrTaskDbEntity grTaskDbEntity = (GrTaskDbEntity) grTaskTable.receiveByTaskName(taskName);
        if (grTaskDbEntity == null) {
            GrTaskDbEntity projectTask = (GrTaskDbEntity) grTaskTable.receiveByTaskNumber(project);
            Parent groupTaskParent = projectTask.mapToParent();
            groupTask = getGeneralTask(GROUP_TASKS, Operations.CAT);

            Udfs udf = refreshUdf();
            udf.setUdfList(generateUdfList(UDF_CDP_CUSTOMER, atCdpCustomer.getId()));
            groupTask.refreshUdf(udf);

            groupTask.setName(taskName);
            groupTask.setParent(groupTaskParent);
            groupTask.setPriority(generatePriority(PRIORITY));

            projectController.createProject(groupTask);
            ApiAsserts.assertThat(projectController.getResponse())
                    .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        } else {
            groupTask = grTaskDbEntity.mapToGeneralTask();
        }
    }

    private void createGenPlan() {
        String taskName = "AT_GENPLAN";
        GrTaskDbEntity grTaskDbEntity = (GrTaskDbEntity) grTaskTable.receiveByTaskName(taskName);
        if (grTaskDbEntity == null) {
            genPlan = getGeneralTask(GENPLAN, Operations.CAT);
            Parent genPlanParent = InitEntities.generateParent(groupTask.getId(), groupTask.getNumber());

            genPlan.setParent(genPlanParent);
            genPlan.setName(taskName);

            Udfs udf = refreshUdf();
            udf.setUdfList(generateUdfList(UDF_CDP_CUSTOMER, atCdpCustomer.getId()));
            udf.setSecondUdfList(generateUdfList(UDF_PROJECT_MANAGING, NO_PROJECT_MANAGING));
            udf.setThirdUdfList(generateUdfList(UDF_PROJECT_MEMBERCODEREVIEW, PROJECT_VIEWERS));

            genPlan.refreshUdf(udf);

            projectController.createProject(genPlan);
            ApiAsserts.assertThat(projectController.getResponse())
                    .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        } else {
            genPlan = grTaskDbEntity.mapToGeneralTask();
        }
    }

    private List setValueToList(List list, Udfs.UdfSd udf) {
        List value = Arrays.stream(udfController.getListValuesOf(udf))
                .filter(u -> u.getName().equals(list.getName()) && u.getCode().equals(list.getCode()))
                .findAny()
                .orElse(null);
        if (value == null) {
            udfController.newListValueFor(udf, list);
            return list;
        } else {
            return value;
        }
    }

}

