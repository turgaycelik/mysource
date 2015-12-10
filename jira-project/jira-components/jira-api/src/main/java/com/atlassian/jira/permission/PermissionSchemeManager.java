/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.permission;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * This class is used to handle Permission Schemes. Permission Schemes are created, removed and edited through this class
 */
public interface PermissionSchemeManager extends SchemeManager
{
    public String getSchemeEntityName();

    public String getEntityName();

    public String getAssociationType();

    public String getSchemeDesc();

    List<GenericValue> getEntities(GenericValue scheme, String permissionKey) throws GenericEntityException;

    /**
     * @since v6.3
     */
    List<GenericValue> getEntities(@Nonnull GenericValue scheme, @Nonnull ProjectPermissionKey permissionKey) throws GenericEntityException;

    /**
     * Get all Generic Value permission records for a particular scheme and permission Id
     * @param scheme The scheme that the permissions belong to
     * @param permissionId The Id of the permission
     * @param parameter The permission parameter (group name etc)
     * @param type The type of the permission(Group, Current Reporter etc)
     * @return List of (GenericValue) permissions
     * @throws GenericEntityException
     * @deprecated Use {@link #getEntities(GenericValue, ProjectPermissionKey, String, String)}. Since v6.3.
     */
    @Deprecated
    public List<GenericValue> getEntities(GenericValue scheme, Long permissionId, String type, String parameter) throws GenericEntityException;

    /**
     * Get all Generic Value permission records for a particular scheme and permission Id
     * @param scheme The scheme that the permissions belong to
     * @param permissionKey The key of the permission
     * @param parameter The permission parameter (group name etc)
     * @param type The type of the permission(Group, Current Reporter etc)
     * @return List of (GenericValue) permissions
     * @throws GenericEntityException
     * @since v6.3
     */
    public List<GenericValue> getEntities(@Nonnull GenericValue scheme, @Nonnull ProjectPermissionKey permissionKey, @Nonnull String type, @Nonnull String parameter) throws GenericEntityException;

    /**
     * @since v6.3
     */
    List<GenericValue> getEntities(@Nonnull GenericValue scheme, @Nonnull ProjectPermissionKey permissionKey, @Nonnull String parameter) throws GenericEntityException;

    /**
     * @since v6.3
     */
    List<GenericValue> getEntitiesByType(@Nonnull GenericValue scheme, @Nonnull ProjectPermissionKey permissionKey, @Nonnull String type) throws GenericEntityException;

    public void flushSchemeEntities();

    /**
     * This is a method that is meant to quickly get you all the schemes that contain an entity of the
     * specified type and parameter.
     * @param type is the entity type
     * @param parameter is the scheme entries parameter value
     * @return Collection of GenericValues that represents a scheme
     */
    public Collection<GenericValue> getSchemesContainingEntity(String type, String parameter);

    /**
     * @deprecated Use {@link #hasSchemeAuthority(ProjectPermissionKey, GenericValue)}. Since v6.3.
     */
    @Deprecated
    boolean hasSchemeAuthority(Long entityType, GenericValue entity);

    /**
     * Checks anonymous permission of the given permission type for the given entity.
     *
     * @param permissionKey permission key.
     * @param entity     the entity to which permission is being checked.
     * @return true only if the anonymous user is permitted.
     * @since v6.3
     */
    boolean hasSchemeAuthority(@Nonnull ProjectPermissionKey permissionKey, @Nonnull GenericValue entity);

    /**
     * @deprecated Use {@link #hasSchemeAuthority(ProjectPermissionKey, GenericValue, User, boolean)}. Since v6.3.
     */
    @Deprecated
    boolean hasSchemeAuthority(Long entityType, GenericValue entity, User user, boolean issueCreation);

    /**
     * Checks the given user's permission of the given permission type for the given entity.
     *
     * @param permissionKey    permission key.
     * @param entity        the entity to which permission is being checked.
     * @param user          the user.
     * @param issueCreation whether the permission is for creating an issue.
     * @return true only if the user is permitted.
     * @since v6.3
     */
    boolean hasSchemeAuthority(@Nonnull ProjectPermissionKey permissionKey, @Nonnull GenericValue entity, @Nonnull User user, boolean issueCreation);

    /**
     * @deprecated Use {@link #getGroups(ProjectPermissionKey, Project)}. Since v6.3.
     */
    @Deprecated
    Collection<Group> getGroups(Long permissionId, Project project);

    /**
     * @since v6.3
     */
    Collection<Group> getGroups(@Nonnull ProjectPermissionKey permissionKey, @Nonnull Project project);

    /**
     * @deprecated Use {@link #getUsers(ProjectPermissionKey, PermissionContext)}. Since v6.3.
     */
    @Deprecated
    Collection<User> getUsers(Long permissionId, PermissionContext ctx);

    /**
     * @since v6.3
     */
    Collection<User> getUsers(@Nonnull ProjectPermissionKey permissionKey, @Nonnull PermissionContext ctx);
}
