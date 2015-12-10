package com.atlassian.jira.bc.scheme.mapper;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.scheme.mapper.SchemeTransformResults;
import com.atlassian.jira.util.ErrorCollection;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Service to do validation that passes through to a {@link com.atlassian.jira.scheme.mapper.SchemeGroupsToRolesTransformer}.
 */
public interface SchemeGroupsToRoleTransformerService
{
    public SchemeTransformResults doTransform(User currentUser, List schemes, Set regularExpressionRoleMappings, ErrorCollection errorCollection);

    public void persistTransformationResults(User currentUser, SchemeTransformResults schemeTransformResults, ErrorCollection errorCollection);

    /**
     * Accepts a Collection of group names and returns a sub-Collection containing the groups that have been granted the
     * global Permissions.USE permission.
     *
     * @param groups a Collection of group names (not null, may be empty)
     * @return a Collection of group names that is a subset of the argument collection, containing only those groups
     * with the USE permission
     */
    public Collection /*<String>*/ getGroupsWithGlobalUsePermission(Collection groups);

    /**
     * Accepts a Collection of group names and returns a sub-Collection containing the groups that have <strong>NOT
     * </strong> been granted the global Permissions.USE permission.
     *
     * @param groups a Collection of group names (not null, may be empty)
     * @return a Collection of group names that is a subset of the argument collection, containing only those groups
     * <strong>without</strong> the USE permission 
     */
    public Collection /*<String>*/ getGroupsWithoutGlobalUsePermission(Collection groups);

    /**
     * Determines whether supplied group name matches a group that's been granted the global Permissions.USE permission
     *
     * @param groupName a group name
     * @return true if the group specified by the name has been granted the USE permission, false otherwise
     */
    boolean isGroupGrantedGlobalUsePermission(String groupName);

    /**
     * Determines whether one or more of the supplied group names matches a group that's been granted the global
     * Permissions.USE permission
     *
     * @param groups a Collection of group names (not null, may be empty)
     * @return true if the Collection contains the name of a group that has the USE permission, false otherwise
     */
    boolean isAnyGroupGrantedGlobalUsePermission(Collection groups);
}
