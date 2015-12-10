package com.atlassian.jira.scheme.mapper;

import com.atlassian.jira.scheme.Scheme;

import java.util.List;
import java.util.Set;

/**
 * This object will allow you to specify a {@link java.util.Set} of regular expressions that should map {@link
 * com.atlassian.crowd.embedded.api.Group} names to a {@link com.atlassian.jira.security.roles.ProjectRole}. You can then use this
 * object to transform a Set of {@link com.atlassian.jira.scheme.Scheme}'s {@link com.atlassian.jira.scheme.SchemeEntity}'s
 * from the groups that have been mapped from to the role they have been mapped to.
 */
public interface SchemeGroupsToRolesTransformer
{
    /**
     * This will take a list of {@link Scheme}'s and transform their associated {@link
     * com.atlassian.jira.scheme.SchemeEntity}'s of type 'group' to the project roles that are mapped via the passed in
     * {@link GroupToRoleMapping}'s.
     *
     * @param schemes                       the List of {@link com.atlassian.jira.scheme.Scheme}'s that should be transformed. NOTE: there will be
     *                                      one {@link com.atlassian.jira.scheme.mapper.SchemeTransformResult} per scheme passed in.
     * @param groupToRoleMappings           defines the group {@link com.atlassian.jira.scheme.SchemeEntity}'s that will
     *                                      be replaced by the defined {@link com.atlassian.jira.security.roles.ProjectRole}
     *                                      SchemeEntity.
     * @return a collection of {@link SchemeTransformResult}'s. An empty collection if the passed in schemes are null or
     *         empty.
     */
    public SchemeTransformResults doTransform(List schemes, Set groupToRoleMappings);

    /**
     * This method, given a {@link SchemeTransformResult} will 'unpack' all the users in the groups that are mapped to
     * each {@link com.atlassian.jira.security.roles.ProjectRole} and then add the users to that ProjectRole for the
     * {@link com.atlassian.jira.project.Project}'s that are associated with this SchemeTransformResults original. It
     * will also save the resulting transformed scheme, renaming the original scheme to 'Backup of ...'.
     * {@link Scheme}.
     * @param schemeTransformResults - contains the results of the GroupToRoles transform as created by the doTransform
     * method of this class.
     */
    public void persistTransformationResults(SchemeTransformResults schemeTransformResults);
}
