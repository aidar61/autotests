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
import com.ts.common.utils.RandomUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

import static com.ts.common.entitites.commonEntities.List.Constants.*;
import static com.ts.common.entitites.commonEntities.Status.Priority.PRIORITY;
import static com.ts.common.entitites.commonEntities.Udfs.UdfSd.*;
import static com.ts.common.enums.TaskType.*;
import static com.ts.common.utils.InitEntities.*;
import static com.ts.common.utils.RandomUtils.generateComment;

@Slf4j
public class TaskGenerator {
    @Getter
    private GeneralTask at_genplan;
    @Getter
    private GeneralTask at_grouptasks;
    @Getter
    private GeneralTask at_sdprojectgroup;
    @Getter
    private GeneralTask at_sdproject;
    @Getter
    private GeneralTask at_regfolder;
    @Getter
    private GeneralTask at_regproject;
    @Getter
    private GeneralTask at_service;
    @Getter
    private GeneralTask at_bdku_installation;
    @Getter
    private GeneralTask at_sdpatchfolder;
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

    public void generateTasks(
            String tasks_home,
            String sd_tasks_home,
            String reg_project_home,
            String installation_home
    ) {
        atCdpCustomer = generateList("AT_CDP_CUSTOMER", "000");
        atCdpCustomer = setValueToList(atCdpCustomer, UDF_CDP_CUSTOMER);

        AT_GROUPTASKS(tasks_home);
        AT_GENPLAN();

        AT_SDPROJECTGROUP(sd_tasks_home);
        AT_SDPROJECT();

        AT_REGFOLDER(reg_project_home);
        AT_REGPROJECT();

        setGENPLANtoSDPROJECT();
        setREGPROJECTtoGENPLAN();

        AT_SERVICE();

        AT_BDKU_INSTALLATION(installation_home);
        AT_SDPATCHFOLDER();
    }

    private void logExist(String name, GrTaskDbEntity taskDbEntity) {
        log.info("{} is exist, take from database next object {}", name, taskDbEntity.toString());
    }

    private void setREGPROJECTtoGENPLAN() {
        if (at_genplan == null) {
            throw new RuntimeException("Call first AT_GENPLAN");
        }
        GeneralTask edit_at_genplan = at_genplan;
        Parent genPlanParent = InitEntities.generateParent(at_grouptasks.getId(), at_grouptasks.getNumber());

        edit_at_genplan.setId(null);
        edit_at_genplan.setCategory(generateCategory(GENPLAN));
        edit_at_genplan.setParent(genPlanParent);
        edit_at_genplan.setNumber(at_genplan.getNumber());
        edit_at_genplan.setName(at_genplan.getName());
        edit_at_genplan.setDescription(generateComment());

        Udfs udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_PROJECT_MANAGING, NO_PROJECT_MANAGING));
        udf.setUdfTask(generateUdfTask(UDF_REGPROJECT, at_regproject.to()));
        udf.setSecondUdfList(generateUdfList(UDF_CDP_CUSTOMER, atCdpCustomer.getId()));
        udf.setThirdUdfList(generateUdfList(UDF_PROJECT_MEMBERCODEREVIEW, PROJECT_VIEWERS));

        edit_at_genplan.refreshUdf(udf);

        projectController.editProject(edit_at_genplan);
        ApiAsserts.assertThat(projectController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);

    }

    private void setGENPLANtoSDPROJECT() {
        if (at_sdproject == null) {
            throw new RuntimeException("Call first AT_SDPROJECT");
        }

        GeneralTask edit_ad_sdproject = at_sdproject;

        Parent projectTaskParent = InitEntities.generateParent(at_sdprojectgroup.getId(), at_sdprojectgroup.getNumber());

        edit_ad_sdproject.setId(null);
        edit_ad_sdproject.setCategory(generateCategory(SDPROJECT));
        edit_ad_sdproject.setParent(projectTaskParent);
        edit_ad_sdproject.setNumber(at_sdproject.getNumber());
        edit_ad_sdproject.setName(at_sdproject.getName());
        edit_ad_sdproject.setDescription(generateComment());

        Udfs udf = refreshUdf();
        udf.setUdfList(generateUdfList(UDF_CDP_CUSTOMER, atCdpCustomer.getId()));
        udf.setSecondUdfList(generateUdfList(UDF_SDPROJECT_SUPPORTTYPE, STANDARD));
        udf.setUdfTask(generateUdfTask(UDF_SD_INTERNPROJECT, at_genplan.to()));
        udf.setUdfInteger(generateUdfInteger(UDF_SD_COST1CAT, 100));
        udf.setSecondUdfInteger(generateUdfInteger(UDF_SD_COST2CAT, 200));
        udf.setThirdUdfInteger(generateUdfInteger(UDF_SD_COST3CAT, 300));

        edit_ad_sdproject.refreshUdf(udf);

        projectController.editProject(edit_ad_sdproject);
        ApiAsserts.assertThat(projectController.getResponse())
                .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);

    }

    private void AT_SDPATCHFOLDER() {
        String task_name = "AT_SDPATCHFOLDER";
        GrTaskDbEntity grTaskDbEntity = (GrTaskDbEntity) grTaskTable.receiveByTaskName(task_name);
        if (grTaskDbEntity == null) {
            Parent sdPatchFolderParent = InitEntities.generateParent(at_bdku_installation.getId(), at_bdku_installation.getNumber());
            at_sdpatchfolder = getGeneralTask(SDPATCHFOLDER, Operations.CAT);
            at_sdpatchfolder.setParent(sdPatchFolderParent);
            at_sdpatchfolder.setName(task_name);
            at_sdpatchfolder.setPriority(generatePriority(2));

            Udfs udf = refreshUdf();
            udf.setUdfTask(generateUdfTask(UDF_REGPROJECT, at_regproject.to()));

            at_sdpatchfolder.refreshUdf(udf);

            projectController.createProject(at_sdpatchfolder);
            ApiAsserts.assertThat(projectController.getResponse())
                    .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        } else {
            at_sdpatchfolder = grTaskDbEntity.mapToGeneralTask();
            logExist(task_name, grTaskDbEntity);
        }
    }

    private void AT_BDKU_INSTALLATION(String installation_home) {
        String task_name = "AT_BDKU_INSTALLATION";
        GrTaskDbEntity grTaskDbEntity = (GrTaskDbEntity) grTaskTable.receiveByTaskName(task_name);
        if (grTaskDbEntity == null) {
            GrTaskDbEntity installationHome = (GrTaskDbEntity) grTaskTable.receiveByTaskNumber(installation_home);
            Parent bdkuInstallationParent = installationHome.mapToParent();
            at_bdku_installation = getGeneralTask(BDKU_INSTALLATION, Operations.CAT);
            at_bdku_installation.setParent(bdkuInstallationParent);
            at_bdku_installation.setName(task_name);

            Udfs udf = refreshUdf();
            udf.setUdfList(generateUdfList(UDF_CDP_CUSTOMER, atCdpCustomer.getId()));
            udf.setUdfTask(generateUdfTask(UDF_INSTALLATION_SDPROJECT, at_sdproject.to()));
            udf.setSecondUdfTask(generateUdfTask(UDF_REGPROJECT, at_regproject.to()));

            at_bdku_installation.refreshUdf(udf);
            projectController.createProject(at_bdku_installation);

            ApiAsserts.assertThat(projectController.getResponse())
                    .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        } else {
            at_bdku_installation = grTaskDbEntity.mapToGeneralTask();
            logExist(task_name, grTaskDbEntity);
        }
    }


    private void AT_REGPROJECT() {
        String task_name = "AT_REGPROJECT";
        GrTaskDbEntity grTaskDbEntity = (GrTaskDbEntity) grTaskTable.receiveByTaskName(task_name);
        if (grTaskDbEntity == null) {
            Parent regProjectParent = InitEntities.generateParent(at_regfolder.getId(), at_regfolder.getName());
            at_regproject = getGeneralTask(REGPROJECT, Operations.CAT);
            at_regproject.setParent(regProjectParent);
            at_regproject.setName(task_name);

            Udfs udf = refreshUdf();
            udf.setUdfMultiList(generateUdfMultiList(UDF_REGISTRY_LOB, NULL_1));
            udf.setUdfList(generateUdfList(UDF_CDP_CUSTOMER, atCdpCustomer.getId()));
            udf.setUdfTask(generateUdfTask(UDF_REGISTRY_FOLDER, at_genplan.to()));
            udf.setSecondUdfTask(generateUdfTask(UDF_REGISTRY_SUPPORT, at_sdprojectgroup.to()));
            udf.setSecondUdfList(generateUdfList(UDF_MIS_TPRJ, NULL_2));

            at_regproject.refreshUdf(udf);
            projectController.createProject(at_regproject);

            ApiAsserts.assertThat(projectController.getResponse())
                    .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        } else {
            at_regproject = grTaskDbEntity.mapToGeneralTask();
            logExist(task_name, grTaskDbEntity);
        }
    }

    private void AT_REGFOLDER(String project) {
        String task_name = "AT_REGFOLDER";
        GrTaskDbEntity grTaskDbEntity = (GrTaskDbEntity) grTaskTable.receiveByTaskName(task_name);
        if (grTaskDbEntity == null) {
            GrTaskDbEntity projectTask = (GrTaskDbEntity) grTaskTable.receiveByTaskNumber(project);
            Parent projectParent = projectTask.mapToParent();
            at_regfolder = getGeneralTask(REGFOLDER, Operations.CAT);
            at_regfolder.setName(task_name);

            Udfs udf = refreshUdf();
            udf.setUdfList(generateUdfList(UDF_CDP_CUSTOMER, atCdpCustomer.getId()));

            at_regfolder.setParent(projectParent);
            at_regfolder.refreshUdf(udf);

            projectController.createProject(at_regfolder);
            ApiAsserts.assertThat(projectController.getResponse())
                    .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        } else {
            at_regfolder = grTaskDbEntity.mapToGeneralTask();
            logExist(task_name, grTaskDbEntity);
        }
    }

    private void AT_SDPROJECTGROUP(String project) {
        String task_name = "AT_SDPROJECTGROUP";
        GrTaskDbEntity grTaskDbEntity = (GrTaskDbEntity) grTaskTable.receiveByTaskName(task_name);
        if (grTaskDbEntity == null) {
            GrTaskDbEntity projectTask = (GrTaskDbEntity) grTaskTable.receiveByTaskNumber(project);
            Parent projectParent = projectTask.mapToParent();
            at_sdprojectgroup = getGeneralTask(SDPROJECTGROUP, Operations.CAT);

            at_sdprojectgroup.setName(task_name);
            at_sdprojectgroup.setPriority(generatePriority(2));
            at_sdprojectgroup.setParent(projectParent);

            Udfs udf = refreshUdf();
            udf.setUdfList(generateUdfList(UDF_CDP_CUSTOMER, atCdpCustomer.getId()));
            udf.setUdfInteger(generateUdfInteger(UDF_SD_COST1CAT, 100));
            udf.setSecondUdfInteger(generateUdfInteger(UDF_SD_COST2CAT, 200));
            udf.setThirdUdfInteger(generateUdfInteger(UDF_SD_COST3CAT, 300));

            at_sdprojectgroup.refreshUdf(udf);
            projectController.createProject(at_sdprojectgroup);
            ApiAsserts.assertThat(projectController.getResponse())
                    .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        } else {
            at_sdprojectgroup = grTaskDbEntity.mapToGeneralTask();
            logExist(task_name, grTaskDbEntity);
        }
    }

    private void AT_SDPROJECT() {
        String task_name = "AT_SDPROJECT";
        GrTaskDbEntity grTaskDbEntity = (GrTaskDbEntity) grTaskTable.receiveByTaskName(task_name);
        if (grTaskDbEntity == null) {
            at_sdproject = getGeneralTask(SDPROJECT, Operations.CAT);
            Parent projectTaskParent = InitEntities.generateParent(at_sdprojectgroup.getId(), at_sdprojectgroup.getName());

            at_sdproject.setParent(projectTaskParent);
            at_sdproject.setName(task_name);

            Udfs udf = refreshUdf();
            udf.setUdfList(generateUdfList(UDF_CDP_CUSTOMER, atCdpCustomer.getId()));
            udf.setSecondUdfList(generateUdfList(UDF_SDPROJECT_SUPPORTTYPE, STANDARD));
            udf.setUdfInteger(generateUdfInteger(UDF_SD_COST1CAT, 100));
            udf.setSecondUdfInteger(generateUdfInteger(UDF_SD_COST2CAT, 200));
            udf.setThirdUdfInteger(generateUdfInteger(UDF_SD_COST3CAT, 300));

            at_sdproject.refreshUdf(udf);

            projectController.createProject(at_sdproject);
            ApiAsserts.assertThat(projectController.getResponse())
                    .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        } else {
            at_sdproject = grTaskDbEntity.mapToGeneralTask();
            logExist(task_name, grTaskDbEntity);
        }
    }

    private void AT_GROUPTASKS(String project) {
        String task_name = "AT_GROUPTASKS";
        GrTaskDbEntity grTaskDbEntity = (GrTaskDbEntity) grTaskTable.receiveByTaskName(task_name);
        if (grTaskDbEntity == null) {
            GrTaskDbEntity projectTask = (GrTaskDbEntity) grTaskTable.receiveByTaskNumber(project);
            Parent groupTaskParent = projectTask.mapToParent();
            at_grouptasks = getGeneralTask(GROUP_TASKS, Operations.CAT);

            Udfs udf = refreshUdf();
            udf.setUdfList(generateUdfList(UDF_CDP_CUSTOMER, atCdpCustomer.getId()));
            at_grouptasks.refreshUdf(udf);

            at_grouptasks.setName(task_name);
            at_grouptasks.setParent(groupTaskParent);
            at_grouptasks.setPriority(generatePriority(PRIORITY));

            projectController.createProject(at_grouptasks);
            ApiAsserts.assertThat(projectController.getResponse())
                    .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        } else {
            at_grouptasks = grTaskDbEntity.mapToGeneralTask();
            logExist(task_name, grTaskDbEntity);
        }
    }

    private void AT_GENPLAN() {
        String task_name = "AT_GENPLAN";
        GrTaskDbEntity grTaskDbEntity = (GrTaskDbEntity) grTaskTable.receiveByTaskName(task_name);
        if (grTaskDbEntity == null) {
            at_genplan = getGeneralTask(GENPLAN, Operations.CAT);
            Parent genPlanParent = InitEntities.generateParent(at_grouptasks.getId(), at_grouptasks.getNumber());

            at_genplan.setParent(genPlanParent);
            at_genplan.setName(task_name);

            Udfs udf = refreshUdf();
            udf.setUdfList(generateUdfList(UDF_CDP_CUSTOMER, atCdpCustomer.getId()));
            udf.setSecondUdfList(generateUdfList(UDF_PROJECT_MANAGING, NO_PROJECT_MANAGING));
            udf.setThirdUdfList(generateUdfList(UDF_PROJECT_MEMBERCODEREVIEW, PROJECT_VIEWERS));

            at_genplan.refreshUdf(udf);

            projectController.createProject(at_genplan);
            ApiAsserts.assertThat(projectController.getResponse())
                    .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        } else {
            at_genplan = grTaskDbEntity.mapToGeneralTask();
            logExist(task_name, grTaskDbEntity);
        }
    }

    private void AT_SERVICE() {
        String task_name = "AT_SERVICE";
        GrTaskDbEntity grTaskDbEntity = (GrTaskDbEntity) grTaskTable.receiveByTaskName(task_name);
        if (grTaskDbEntity == null) {
            at_service = getGeneralTask(SERVICE, Operations.CAT);
            Parent serviceParent = generateParent(at_genplan.getId(), at_genplan.getNumber());

            at_service.setParent(serviceParent);
            at_service.setName(task_name);

            Udfs udf = refreshUdf();
            udf.setUdfList(generateUdfList(UDF_MIS_SERVICE, VIEW_PROCESS_05));

            at_service.refreshUdf(udf);

            projectController.createProject(at_service);
            ApiAsserts.assertThat(projectController.getResponse())
                    .isCorrectResponseCode(TrackStudioHttpStatusCodes.HTTP_OK);
        } else {
            at_service = grTaskDbEntity.mapToGeneralTask();
            logExist(task_name, grTaskDbEntity);
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

