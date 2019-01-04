package com.sap.cp.appsec.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PermissionDto {

    @NotBlank
    @NotNull
    public String name;

    @NotNull
    public Character[] permissionCodes;

    @JsonIgnore
    public Map<Character,Permission> getMappedPermissions() {
        Map<Character, Permission> permissions = new HashMap<>();

        for (Character permissionCode : permissionCodes) {
            switch (permissionCode) {
                case 'R':
                    permissions.put(permissionCode, BasePermission.READ);
                    break;
                case 'W':
                    permissions.put(permissionCode, BasePermission.WRITE);
                    break;
                case 'D':
                    permissions.put(permissionCode, BasePermission.DELETE);
                    break;
                case 'A':
                    permissions.put(permissionCode, BasePermission.ADMINISTRATION);
                    break;
                default:
                    break;
            }
        }
        return permissions;
    }

    @JsonIgnore
    public Permission[] getPermissions() {
        Set<Permission> permissions = new HashSet<>();

        for (Character permissionCode : permissionCodes) {
            switch (permissionCode) {
                case 'R':
                    permissions.add(BasePermission.READ);
                    break;
                case 'W':
                    permissions.add(BasePermission.WRITE);
                    break;
                case 'D':
                    permissions.add(BasePermission.DELETE);
                    break;
                case 'A':
                    permissions.add(BasePermission.ADMINISTRATION);
                    break;
                default:
                    break;
            }
        }
        return permissions.toArray(new Permission[0]);
    }

}
