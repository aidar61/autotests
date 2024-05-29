package com.ts.common.generators;

import com.ts.common.asserts.ApiAsserts;
import com.ts.common.config.AppConfigProvider;
import com.ts.common.config.AppUserConfig;
import com.ts.common.controllers.user.UserController;
import com.ts.common.entitites.commonEntities.Role;
import com.ts.common.entitites.commonEntities.User;
import com.ts.common.utils.InitEntities;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ts.common.application.controllers.TrackStudioHttpStatusCodes.HTTP_OK;
import static com.ts.common.config.AppConfigProvider.getUserConfig;
import static com.ts.common.entitites.commonEntities.Role.RoleConstants.*;
import static com.ts.common.entitites.commonEntities.Task.Constants.COLVIR;
import static com.ts.common.entitites.commonEntities.Task.Constants.SERVICE_DESK;
import static com.ts.common.entitites.commonEntities.User.Constants.CLI_ROOT;
import static com.ts.common.entitites.commonEntities.User.Constants.COMPANY_100_100;
import static com.ts.common.utils.InitEntities.*;

public class UserGenerator {
    private final Map<Role.RoleConstants, List<User>> userMap;
    private User user;
    private final User cliRoot;
    private final User company100100;
    private List<User> organizations;
    private List<User> clients;
    private List<User> deps;
    private List<User> workers;
    private final UserController userController;

    private UserGenerator(UserController userController) {
        this.userController = userController;
        userMap = new HashMap<>();
        cliRoot = userController.getUserBy(CLI_ROOT.getLogin());
        company100100 = userController.getUserBy(COMPANY_100_100.getLogin());
    }

    public static UserGenerator create(UserController userController) {
        return new UserGenerator(userController);
    }

    public Map<Role.RoleConstants, List<User>> generateUsers() {
        createUserRoleOrganization(getUserConfig().roleOrganization());
        userMap.put(ROLE_ORGANIZATION, organizations);

        User organizationParent = userController.getUserBy(userMap, ROLE_ORGANIZATION, getUserConfig().roleOrganization());
        User clientParent = generateUser(organizationParent.getId()
                , organizationParent.getLogin()
                , organizationParent.getName());
        createUserRoleClient(getUserConfig().roleClient(), clientParent);
        userMap.put(ROLE_CLIENT, clients);

        createUserRoleDep(getUserConfig().roleDep());
        userMap.put(ROLE_DEP, deps);

        User depParent = userController.getUserBy(userMap, ROLE_DEP, getUserConfig().roleDep());
        User workerParent = generateUser(depParent.getId()
                , depParent.getLogin()
                , depParent.getName());
        createUserRoleWorker(getUserConfig().projectManager(), workerParent);
        createUserRoleWorker(getUserConfig().projectParticipant(), workerParent);
        createUserRoleWorker(getUserConfig().managerClient(), workerParent);
        createUserRoleWorker(getUserConfig().managerSupplier(), workerParent);
        createUserRoleWorker(getUserConfig().analytic(), workerParent);
        createUserRoleWorker(getUserConfig().managerAccount(), workerParent);
        createUserRoleWorker(getUserConfig().managerFeature(), workerParent);
        createUserRoleWorker(getUserConfig().managerRequest(), workerParent);
        createUserRoleWorker(getUserConfig().managerEmp(), workerParent);
        userMap.put(ROLE_WORKER, workers);
        return userMap;
    }

    private void createUserRoleOrganization(String username) {
        if (organizations == null || organizations.isEmpty()) {
            organizations = new ArrayList<>();
        }
        if (isUserNotExist(username)) {
            user = getUser(username, ROLE_ORGANIZATION);
            user.defaultUser();
            user.setDefaultTask(generateTask(SERVICE_DESK));
            user.setParent(generateUser(cliRoot));
            userController.createUser(user);
            ApiAsserts.assertThat(userController.getResponse())
                    .isCorrectResponseCode(HTTP_OK);
        }
        organizations.add(user);
    }

    private void createUserRoleClient(String username, User parent) {
        if (clients == null || clients.isEmpty()) {
            clients = new ArrayList<>();
        }
        if (isUserNotExist(username)) {
            user = getUser(username, ROLE_CLIENT);
            user.defaultUser();
            user.setDefaultTask(generateTask(COLVIR));
            user.setParent(parent);
            userController.createUser(user);
            ApiAsserts.assertThat(userController.getResponse())
                    .isCorrectResponseCode(HTTP_OK);
        }
        clients.add(user);
    }

    private void createUserRoleDep(String username) {
        if (deps == null || deps.isEmpty()) {
            deps = new ArrayList<>();
        }
        if (isUserNotExist(username)) {
            user = getUser(username, ROLE_DEP);
            user.defaultUser();
            user.setCompany("Colvir");
            user.setDefaultTask(generateTask(COLVIR));
            user.setParent(InitEntities.generateUser(company100100));
            userController.createUser(user);
            ApiAsserts.assertThat(userController.getResponse())
                    .isCorrectResponseCode(HTTP_OK);
        }
        deps.add(user);
    }

    private void createUserRoleWorker(String username, User parent) {
        if (workers == null || workers.isEmpty()) {
            workers = new ArrayList<>();
        }
        if (isUserNotExist(username)) {
            user = getUser(username, ROLE_WORKER);
            user.defaultUser();
            user.setDefaultTask(generateTask(COLVIR));
            user.setParent(parent);
            userController.createUser(user);
            ApiAsserts.assertThat(userController.getResponse())
                    .isCorrectResponseCode(HTTP_OK);
        }
        workers.add(user);
    }

    private boolean isUserNotExist(String username) {
        user = userController.getUserBy(username);
        return user.getId() == null;
    }
}
