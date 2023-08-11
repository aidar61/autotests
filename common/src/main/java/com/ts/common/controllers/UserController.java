package com.ts.common.controllers;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.UserRole;
import com.ts.common.enums.Operations;
import com.ts.common.enums.Parents;
import com.ts.common.enums.TaskType;
import com.ts.common.enums.Users;
import com.ts.common.request.ApiRequest;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.RandomUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import static com.ts.common.application.controllers.TrackStudioEndPoints.*;
import static com.ts.common.config.AppConfigProvider.STAND_URL;

@Slf4j
public class UserController extends ApiRequest {
    public UserController(String url, AuthToken authToken) {
        super(url, HEADERS_BASE_CONTROLLER, authToken);
    }

    private List<UserRole> receiveAllUserForProject(Parents parents) {
        HashMap<String, String> queryParam = new LinkedHashMap<>() {{
            put(TO_TASK, parents.tuskNumber);
        }};
        super.response = super.get(getEndpoint(REST, ACL, EFFECTIVE, formatParameters(queryParam)));
        return Arrays.asList(extractObject(UserRole[].class));
    }

    private List<UserRole> receiveUserClients(Parents parents) {
        List<UserRole> userRoles = receiveAllUserForProject(parents);
        return userRoles.stream().filter(f -> f.getAssignedRole().getName().equals("Клиент")).collect(Collectors.toList());
    }

    public UserRole receiveRandomClient(Parents parents) {
        List<UserRole> userRoles = receiveUserClients(parents);
        return userRoles.get(RandomUtils.generateRandomNumberBetween(0, userRoles.size() - 1));
    }

    private List<UserRole> receiveUserEmployees(Parents parents) {
        List<UserRole> userRoles = receiveAllUserForProject(parents);
        return userRoles.stream().filter(f -> f.getAssignedRole().getName().equals("Сотрудник")).collect(Collectors.toList());
    }

    public UserRole receiveRandomEmployees(Parents parents) {
        List<UserRole> userRoles = receiveUserEmployees(parents);
        return userRoles.get(RandomUtils.generateRandomNumberBetween(0, userRoles.size() - 1));
    }


    public User receiveRandomUser(String taskNumber, Udfs.UdfSd udfSd, Operations field, TaskType taskType) {
        LinkedHashMap<String, String> params = new LinkedHashMap<>() {{
            put("active", "true");
            put("operationId", InitEntities.generateOperationIDHelper(taskType, field));
        }};
        List<User> collect = Arrays.stream(super.get(getEndpoint(UDF_VAL, udfSd.udfId, TASK, taskNumber, USER, LIST, formatParameters(params))).as(User[].class)).collect(Collectors.toList());
        return collect.get(RandomUtils.generateRandomNumberBetween(0, collect.size() - 1));
    }

    private List<UserRole> receiveUsersByTaskNumber(String taskNumber) {
        HashMap<String, String> queryParam = new LinkedHashMap<>() {{
            put(TO_TASK, taskNumber);
        }};
        super.response = super.get(getEndpoint(REST, ACL, EFFECTIVE, formatParameters(queryParam)));
        System.out.println("@@@@@@@@@@@@@@ " + response.asPrettyString());
        return Arrays.asList(extractObject(UserRole[].class));
    }

    public UserRole receiveUserByRole(List<UserRole> userRoles, String role, String login) {
        return userRoles.stream().filter(f -> f.getAssignedRole().getName().equals(role) && !f.getForUser().getLogin().equals(login)).findFirst().get();
    }

    public List<UserRole> receiveUserByTask(String taskNumber) {
        return receiveUsersByTaskNumber(taskNumber);
    }

    public Map<String, UserRole> receiveUsersByRoles(String taskNumber, String... roles) {
        LinkedHashMap<String, UserRole> usersByRole = new LinkedHashMap<>();
        var userRoles = receiveUsersByTaskNumber(taskNumber);
        for (String role : roles) {
            UserRole users = userRoles.stream().filter(f -> f.getAssignedRole().getName().equals(role)).findFirst().get();
            usersByRole.put(role, users);
        }
        return usersByRole;
    }

    public static void main(String[] args) {
        UserController userController = new UserController(STAND_URL, InitEntities.generateAuthToken(Users.ROOT));
        UserRole userRole = userController.receiveRandomClient(Parents.MTB);
        UserRole userRole1 = userController.receiveRandomEmployees(Parents.MTB);
        System.out.println(userRole);
        System.out.println(userRole1);
    }

}
