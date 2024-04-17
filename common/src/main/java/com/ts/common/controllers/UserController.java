package com.ts.common.controllers;

import com.ts.common.application.controllers.AuthToken;
import com.ts.common.entitites.commonEntities.Role;
import com.ts.common.entitites.commonEntities.Udfs;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.entitites.commonEntities.UserRole;
import com.ts.common.enums.Operations;
import com.ts.common.enums.Parents;
import com.ts.common.enums.TaskType;
import com.ts.common.request.ApiRequest;
import com.ts.common.utils.InitEntities;
import com.ts.common.utils.JsonUtils;
import com.ts.common.utils.RandomUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import static com.ts.common.application.controllers.TrackStudioEndPoints.*;

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
        super.setAuthToken(new AuthToken("root", "password"));
        this.response = super.get(getEndpoint(REST, ACL, EFFECTIVE, formatParameters(queryParam)));
        return Arrays.asList(Objects.requireNonNull(JsonUtils.deserialize(this.response, UserRole[].class)));
    }

    public List<User> receiveUsersByTaskOperation(Udfs.UdfSd udfType, String taskNumber, String operationId) {
        HashMap<String, String> queryParam = new LinkedHashMap<>() {{
            put("active", "true");
            put("operationId", operationId);
        }};
        this.response = super.get(getEndpoint(REST, UDF_VAL, udfType.udfId, TASK, taskNumber, USER, LIST, formatParameters(queryParam)));
        return Arrays.asList(Objects.requireNonNull(JsonUtils.deserialize(this.response, User[].class)));
    }

    public UserRole receiveUserByRole(List<UserRole> userRoles, String role, String login) {
        Collections.shuffle(userRoles);
        if (role.equals("Клиент")) {
            return userRoles.stream().filter(f -> f.getForUser().getLogin().contains("@") && f.getAssignedRole().getName().equals(role) && !f.getForUser().getLogin().equals(login) && f.getForUser().getActive()).findFirst().get();
        }
        return userRoles.stream().filter(f -> f.getAssignedRole().getName().equals(role) && !f.getForUser().getLogin().contains(login) && f.getForUser().getActive()).findFirst().get();
    }

    public UserRole receiveUserByRole(List<UserRole> userRoles, Role.Constants role, String exceptLogin) {
        Collections.shuffle(userRoles);
        if (role.getRole().equals("Клиент")) {
            return userRoles.stream().filter(f -> f.getForUser().getLogin().contains("@")
                    && f.getAssignedRole().getName().equals(role.getRole())
                    && !f.getForUser().getLogin().equals(exceptLogin) && f.getForUser().getActive()).findFirst().get();
        }
        return userRoles.stream().filter(f ->
                f.getAssignedRole().getName().equals(role.getRole())
                        && f.getForUser().getActive()
                        && !f.getForUser().getLogin().contains(exceptLogin)
                        && !f.getForUser().getLogin().equals("ovoronov")).findFirst().get();
    }

    public UserRole receiveUserByRole(List<UserRole> userRoles, String exceptLogin, Role.Constants... role) {
        Collections.shuffle(userRoles);

        if (Arrays.stream(role).anyMatch(r -> r.getRole().equals("Клиент"))) {
            return userRoles.stream().filter(f -> f.getForUser().getLogin().contains("@")
                    && f.getAssignedRole().getName().equals(role[0].getRole())
                    && f.getAssignedRole().getName().equals(role[1].getRole())
                    && !f.getForUser().getLogin().equals(exceptLogin) && f.getForUser().getActive()).findFirst().get();
        }
        return userRoles.stream().filter(f ->
                f.getAssignedRole().getName().equals(role[0].getRole())
                        && f.getAssignedRole().getName().equals(role[1].getRole())
                        && !f.getForUser().getLogin().contains(exceptLogin) && f.getForUser().getActive()).findFirst().get();
    }

    public UserRole receiveUserByRoles(List<UserRole> userRoles, String exceptLogin, Role.Constants... role) {
        return Arrays.stream(role).map(r -> {
            return userRoles.stream().filter(userRole ->
                    userRole.getAssignedRole().getName().equals(r.getRole())
                            && !userRole.getForUser().getLogin().contains(exceptLogin)).findFirst().get();
        }).findAny().get();
    }

    public UserRole receiveUserByRole(List<UserRole> userRoles, String role, String login, boolean singleRole) {
        Collections.shuffle(userRoles);
        if (role.equals("Клиент")) {
            userRoles = userRoles.stream().filter(f -> f.getForUser().getLogin().contains("@") && f.getAssignedRole().getName().equals(role) && !f.getForUser().getLogin().contains(login) && f.getForUser().getActive()).collect(Collectors.toList());
        }
        if (singleRole) {
            var filteredUsers = userRoles.stream().filter(x -> x.getAssignedRole().getName().equals(role) && !x.getForUser().getLogin().contains(login)).collect(Collectors.toList());
            for (var filteredUser : filteredUsers) {
                var userCount = userRoles.stream().filter(x -> x.getForUser().getLogin().equals(filteredUser.getForUser().getLogin()) && !x.getForUser().getLogin().contains(login)).count();
                if (userCount < 2) {
                    return filteredUser;
                }
            }
        }

        return userRoles.stream().filter(f -> f.getAssignedRole().getName().equals(role) && !f.getForUser().getLogin().contains(login) && f.getForUser().getActive()).findAny().get();
    }

    public UserRole receiveUserByRole(List<UserRole> userRoles, String login) {
        Collections.shuffle(userRoles);
        return userRoles.stream().filter(f -> !f.getForUser().getLogin().equals(login) && f.getForUser().getActive()).findAny().get();
    }

    public List<UserRole> receiveUserByTask(String taskNumber) {
        return receiveUsersByTaskNumber(taskNumber);
    }

    public User receiveUserByLogin(List<UserRole> userRoles, String login) {
        return userRoles.stream().filter(u -> u.getForUser().getLogin().equals(login)).findFirst().get().getForUser();
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

//    public static void main(String[] args) {
//        UserController userController = new UserController(STAND_URL, InitEntities.generateAuthToken(Users.ROOT));
//        UserRole userRole = userController.receiveRandomClient(Parents.MTB);
//        UserRole userRole1 = userController.receiveRandomEmployees(Parents.MTB);
//        System.out.println(userRole);
//        System.out.println(userRole1);
//    }

}
