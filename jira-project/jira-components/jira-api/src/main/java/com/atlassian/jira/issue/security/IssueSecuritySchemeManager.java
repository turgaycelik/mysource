package com.atlassian.jira.issue.security;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeManager;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

@PublicApi
public interface IssueSecuritySchemeManager extends SchemeManager
{
    /**
     * Gets a scheme by id from the database.
     * @param id the id of the scheme to get.
     * @return the Scheme
     * @throws DataAccessException if there is trouble retrieving from the database.
     *
     * @deprecated Use {@link #getIssueSecurityLevelScheme(Long)} instead. Since v5.2.
     */
    Scheme getSchemeObject(Long id) throws DataAccessException;

    /**
     * Returns the IssueSecurityLevelScheme for the given ID.
     *
     * @param schemeId Scheme ID
     *
     * @return the IssueSecurityLevelScheme for the given ID.
     */
    IssueSecurityLevelScheme getIssueSecurityLevelScheme(Long schemeId);

    /**
     * Returns the configured permissions for the given Security Level.
     *
     * @param securityLevelId the Security Level
     * @return the configured permissions for the given Security Level.
     *
     * @deprecated Use {@link #getPermissionsBySecurityLevel(Long)} instead. Since v5.2.
     */
    public List getEntitiesBySecurityLevel(Long securityLevelId) throws GenericEntityException;

    /**
     * Returns the configured permissions for the given Security Level.
     *
     * @param securityLevelId the Security Level
     * @return the configured permissions for the given Security Level.
     */
    List<IssueSecurityLevelPermission> getPermissionsBySecurityLevel(Long securityLevelId);

    /**
     * @deprecated Use {@link #getPermissionsBySecurityLevel(Long)} instead. Since v5.2.
     */
    List<GenericValue> getEntities(GenericValue scheme, Long securityLevelId) throws GenericEntityException;

    /**
     * This is a method that is meant to quickly get you all the schemes that contain an entity of the
     * specified type and parameter.
     * @param type is the entity type
     * @param parameter is the scheme entries parameter value
     * @return Collection of GenericValues that represents a scheme
     */
    public Collection<GenericValue> getSchemesContainingEntity(String type, String parameter);

    /**
     * Returns all projects that use the given Issue Security Level Scheme.
     *
     * @param schemeId ID of the Issue Security Level Scheme
     *
     * @return all projects that use the given Issue Security Level Scheme.
     */
    List<Project> getProjectsUsingScheme(long schemeId);

    /**
     * Set the issue security level scheme to be used by the given Project.
     *
     * @param project The Project
     *
     * @param issueSecuritySchemeId The desired new security level scheme to use - null indicates "no issue security levels".
     */
    void setSchemeForProject(Project project, Long issueSecuritySchemeId);
}
