package com.ts.common.application.controllers;

import com.ts.common.controllers.BaseController;
import com.ts.common.controllers.CreateFromExcelTaskController;
import com.ts.common.controllers.TaskResponseBody;
import com.ts.common.controllers.UserController;
import com.ts.common.controllers.advice.AdviceController;
import com.ts.common.controllers.advice.CodeReviewController;
import com.ts.common.controllers.advice.ConfirmationController;
import com.ts.common.controllers.advice.SanctionController;
import com.ts.common.controllers.bug.BugTaskController;
import com.ts.common.controllers.gap.GapSolutionController;
import com.ts.common.controllers.gap.PotentialGapController;
import com.ts.common.controllers.release.ReleaseController;
import com.ts.common.controllers.release.ReleaseModuleController;
import com.ts.common.controllers.sdbug.*;
import com.ts.common.controllers.sddev.SdDevController;
import com.ts.common.controllers.sdhelp.SdHelpController;
import com.ts.common.controllers.sdquestion.SdQuestionController;
import com.ts.common.controllers.sla.SlaBugController;
import com.ts.common.controllers.sla.SlaFeatureController;
import com.ts.common.controllers.sla.SlaHelpController;
import com.ts.common.controllers.workTask.ContingentTaskController;
import com.ts.common.controllers.workTask.DevTaskController;
import com.ts.common.controllers.workTask.TechTaskController;
import com.ts.common.controllers.workTask.WorkTaskController;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.entitites.tasks.Task;
import com.ts.common.request.ApiRequest;
import com.ts.common.utils.JsonUtils;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;

import static com.ts.common.config.AppConfigProvider.STAND_URL;

@Getter
@Setter
@Slf4j
public class TrackStudioApiControllers {
    private Response response;
    private AuthToken authToken;
    private UserController userController;
    private SlaHelpController slaHelpController;
    private SlaBugController slaBugController;
    private SlaFeatureController slaFeatureController;
    private PotentialGapController potentialGapController;
    private GapSolutionController gapSolutionController;
    private ReleaseModuleController releaseModuleController;
    private AdviceController adviceController;
    private ConfirmationController confirmationController;
    private SanctionController sanctionController;
    private DevTaskController devTaskController;
    private WorkTaskController workTaskController;
    private BaseController baseController;
    private BugTaskController bugTaskController;
    private SdQuestionController sdQuestionController;
    private CreateFromExcelTaskController createFromExcelTaskController;
    private CodeReviewController codeReviewController;
    private TechTaskController techTaskController;
    private ContingentTaskController contingentTaskController;
    private SdBugController sdBugController;
    private SdImproveController sdImproveController;
    private SdDocImproveController sdDocImproveController;
    private SdDocBugController sdDocBugController;
    private SdOptimizationController sdOptimizationController;
    private ReleaseController releaseController;
    private SdDevController sdDevController;
    private SdHelpController sdHelpController;


    public TrackStudioApiControllers(AuthToken authToken) {
        this.userController = new UserController(STAND_URL, authToken);
        this.slaHelpController = new SlaHelpController(STAND_URL, authToken);
        this.slaBugController = new SlaBugController(STAND_URL, authToken);
        this.slaFeatureController = new SlaFeatureController(STAND_URL, authToken);
        this.potentialGapController = new PotentialGapController(STAND_URL, authToken);
        this.baseController = new BaseController(STAND_URL, authToken);
        this.gapSolutionController = new GapSolutionController(STAND_URL, authToken);
        this.releaseModuleController = new ReleaseModuleController(STAND_URL, authToken);
        this.adviceController = new AdviceController(STAND_URL, authToken);
        this.confirmationController = new ConfirmationController(STAND_URL, authToken);
        this.sanctionController = new SanctionController(STAND_URL, authToken);
        this.devTaskController = new DevTaskController(STAND_URL, authToken);
        this.workTaskController = new WorkTaskController(STAND_URL, authToken);
        this.bugTaskController = new BugTaskController(STAND_URL, authToken);
        this.sdQuestionController = new SdQuestionController(STAND_URL, authToken);
        this.createFromExcelTaskController = new CreateFromExcelTaskController(STAND_URL, authToken);
        this.codeReviewController = new CodeReviewController(STAND_URL, authToken);
        this.techTaskController = new TechTaskController(STAND_URL, authToken);
        this.contingentTaskController = new ContingentTaskController(STAND_URL, authToken);
        this.sdBugController = new SdBugController(STAND_URL, authToken);
        this.sdImproveController = new SdImproveController(STAND_URL, authToken);
        this.sdDocImproveController = new SdDocImproveController(STAND_URL, authToken);
        this.sdDocBugController = new SdDocBugController(STAND_URL, authToken);
        this.sdOptimizationController = new SdOptimizationController(STAND_URL, authToken);
        this.releaseController = new ReleaseController(STAND_URL, authToken);
        this.sdDevController = new SdDevController(STAND_URL, authToken);
        this.sdHelpController = new SdHelpController(STAND_URL, authToken);
    }

    public GeneralTask receiveGeneralTask(String slaTaskNumber) {
        this.response = this.baseController.receiveActualTask(slaTaskNumber);
        TaskResponseBody taskResponseBody = JsonUtils.deserialize(this.response, TaskResponseBody.class);
        if (taskResponseBody != null) {
            return new GeneralTask(taskResponseBody);
        }
        return null;
    }

    public Response receiveTask(String slaTaskNumber) {
//        updateToken(InitEntities.generateAuthToken(Users.ROOT));
        return this.response = this.baseController.receiveActualTask(slaTaskNumber);
    }

    public Response receiveParentTaskPayload(String parentTaskNumber, String category) {
        return this.baseController.receiveParentTaskPayload(parentTaskNumber, category);
    }

    public Response receiveFinishDevForm(String taskNumber) {
        return this.baseController.receiveFinishDevForm(taskNumber);
    }

    public Response receiveSubTask(String taskNumber) {
        return this.baseController.receiveActiveSubTask(taskNumber);
    }

    public Task receiveSubTaskByCategory(String taskNumber, String category) {
        var response = baseController.receiveActiveSubTask(taskNumber);
        return response.jsonPath()
                .getList("tasks", com.ts.common.entitites.tasks.Task.class)
                .stream()
                .filter(x -> x.getCategory().getId().equals(category))
                .findFirst()
                .get();
    }

    public Response receiveActiveSubTasks(String taskNumber) {
        return baseController.receiveActiveSubTask(taskNumber);
    }

    public Response receiveAllSubTasks(String taskNumber) {
        return baseController.receiveAllSubTask(taskNumber);
    }

    @Step("Пользователь: {0}")
    public void updateToken(AuthToken authToken) {
        var fields = this.getClass().getDeclaredFields();
        for (var field : fields) {
            if (ApiRequest.class.isAssignableFrom(field.getType())) {
                try {
                    var method = field.getType().getMethod("setAuthToken", AuthToken.class);
                    var fieldValue = field.get(this);
                    method.setAccessible(true);
                    method.invoke(fieldValue, authToken);
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

