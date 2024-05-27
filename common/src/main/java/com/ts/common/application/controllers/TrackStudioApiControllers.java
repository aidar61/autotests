package com.ts.common.application.controllers;

import com.ts.common.controllers.BaseController;
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
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.tasks.GeneralTask;
import com.ts.common.entitites.tasks.Task;
import com.ts.common.request.ApiRequest;
import com.ts.common.utils.InitEntities;
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
        var fields = this.getClass().getDeclaredFields();
        for (var field : fields) {
            if (ApiRequest.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                try {
                    var constructor = field.getType().getConstructor(String.class, AuthToken.class);
                    field.set(this, constructor.newInstance(STAND_URL, authToken));
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                         InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
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

    @Step("Пользователь: {0}")
    public void updateToken(User user) {
        AuthToken authToken = InitEntities.generateAuthToken(user);
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

