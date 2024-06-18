package com.ts.common.asserts;

import com.ts.common.entitites.BaseEntity;
import com.ts.common.entitites.commonEntities.Role;
import com.ts.common.entitites.helpers.Permissions;
import com.ts.common.entitites.helpers.Transition;
import com.ts.common.enums.Permission;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

@Slf4j
public class SettingsAssert extends EntityAssert {

    public SettingsAssert(BaseEntity[] entity) {
        super(entity);
    }

    public static SettingsAssert assertThat(BaseEntity[] entity) {
        return new SettingsAssert(entity);
    }

    public SettingsAssert isCorrectTransitionStatusName(String name) {
        Transition[] transitions = (Transition[]) this.entities;

        Assertions.assertThat(transitions)
                .withFailMessage("Transitions is not have status by name %s", name.trim())
                .anyMatch(t -> t.receiveTaskStatus().equals(name.trim()));
        log.info("Transition status is correct, Expected: {}", name.trim());
        return this;
    }

    public SettingsAssert isHavePermission(Role.RoleConstants role, Permission... permissionsToCheck) {
        Permissions[] permissions = (Permissions[]) this.entities;
        List<String> roleIds = Arrays.stream(permissions)
                .map(Permissions::getRole)
                .map(Role::getId)
                .collect(Collectors.toList());

        boolean roleExists = roleIds.contains(role.getId());
        assertTrue(roleExists, "Role with id " + role + " is not found ");

        if (permissionsToCheck.length > 3) {
            fail("Expected permission size can't be greater than 3");
        }

        Permissions matchingPermission = Arrays.stream(permissions)
                .filter(p -> p.getRole().getId().equals(role.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Role with id " + role + " has no permissions"));

        String[] actualPermissions = matchingPermission.getPermissions();
        String[] expectedPermissions = Arrays.stream(permissionsToCheck)
                .map(Permission::getPermission)
                .toArray(String[]::new);

        Assertions.assertThat(actualPermissions)
                .withFailMessage("Permissions of role are not correct Expected %s, Actual %s"
                        , Arrays.toString(expectedPermissions)
                        , Arrays.toString(actualPermissions))
                .containsExactlyInAnyOrder(expectedPermissions);

        log.info("Permissions {} of role {} are correct", permissionsToCheck, role);
        log.info("Actual {} Expected {}", actualPermissions, expectedPermissions);
        return this;
    }

}
