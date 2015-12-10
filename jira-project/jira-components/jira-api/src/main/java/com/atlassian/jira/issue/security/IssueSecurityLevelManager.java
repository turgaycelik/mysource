package com.atlassian.jira.issue.security;

import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

@PublicApi
public interface IssueSecurityLevelManager
{
    /**
     * Returns the list of Security Levels for the given Issue Security Level Scheme.
     *
     * @param schemeId ID of the Issue Security Level Scheme.
     * @return the list of Security Levels for the given Issue Security Level Scheme.
     * @deprecated Use {@link #getIssueSecurityLevels(long)} instead. Since v5.0.
     */
    List<GenericValue> getSchemeIssueSecurityLevels(Long schemeId);

    /**
     * Returns the list of Security Levels for the given Issue Security Level Scheme.
     *
     * @param schemeId ID of the Issue Security Level Scheme.
     * @return the list of Security Levels for the given Issue Security Level Scheme.
     *
     * @since v5.0
     */
    List<IssueSecurityLevel> getIssueSecurityLevels(long schemeId);

    /**
     * @param id IssueSecurityLevel ID
     * @deprecated Use {@link #getSecurityLevel(long)} != null instead. Since v5.0.
     * @return true if exists
     */
    @SuppressWarnings ( { "UnusedDeclaration" })
    boolean schemeIssueSecurityExists(Long id);

    String getIssueSecurityName(Long id);

    String getIssueSecurityDescription(Long id);

    /**
     * Returns the IssueSecurityLevel with the given ID.
     * @param id the ID
     * @return the IssueSecurityLevel with the given ID.
     *
     * @deprecated Use {@link #getSecurityLevel(long)} instead. Since v5.0.
     */
    GenericValue getIssueSecurity(Long id);

    /**
     * Returns the IssueSecurityLevel with the given ID.
     * @param id the ID
     * @return the IssueSecurityLevel with the given ID.
     *
     * @since v5.0
     */
    IssueSecurityLevel getSecurityLevel(long id);

    /**
     * Creates an Issue Security Level with the given properties.
     *
     * @param schemeId The Issue security scheme that this level belongs to.
     * @param name The name of the new level
     * @param description an optional description
     *
     * @return the newly created Issue Security Level
     */
    IssueSecurityLevel createIssueSecurityLevel(long schemeId, String name, String description);

    /**
     * Get the different levels of security that can be set for this issue.
     *
     * @param entity This is the issue or the project that the security is being checked for
     * @param user   The user used for the security check
     * @return list containing the security levels, can be null
     * @throws GenericEntityException Exception in the OFBiz persistence layer.
     *
     * @deprecated Use {@link #getUsersSecurityLevels(Issue, User)}
     *               or{@link #getUsersSecurityLevels(Project, User)} instead. Since v5.0.
     */
    List<GenericValue> getUsersSecurityLevels(GenericValue entity, User user) throws GenericEntityException;

    /**
     * Get the different levels of security that can be set for this issue.
     * If you are creating a new Issue, then use {@link #getUsersSecurityLevels(com.atlassian.jira.project.Project, com.atlassian.crowd.embedded.api.User)}.
     *
     * @param issue This is the issue that the security is being checked for
     * @param user   The user used for the security check
     * @return list containing the security levels, can be null
     *
     * @since v5.0
     *
     * @see #getUsersSecurityLevels(com.atlassian.jira.project.Project, com.atlassian.crowd.embedded.api.User)
     */
    List<IssueSecurityLevel> getUsersSecurityLevels(Issue issue, User user);

    /**
     * Get the different levels of security that can be set for an issue created in this project.
     * If you are editing an existing Issue, then use {@link #getUsersSecurityLevels(Issue, User)}.
     *
     * @param project the project that the security is being checked for
     * @param user   The user used for the security check
     * @return list containing the security levels, can be null
     *
     * @since v5.0
     *
     * @see #getUsersSecurityLevels(com.atlassian.jira.issue.Issue, com.atlassian.crowd.embedded.api.User)
     */
    List<IssueSecurityLevel> getUsersSecurityLevels(Project project, User user);

    /**
     * Get the different levels of security that the user can see across all projects.
     *
     * @param user   The user used for the security check
     * @return list containing the security levels, can be null
     * @throws GenericEntityException Exception in the OFBiz persistence layer.
     * @since v4.0
     * @deprecated Use {@link #getAllSecurityLevelsForUser(com.atlassian.crowd.embedded.api.User)} instead. Since v5.0.
     */
    Collection<GenericValue> getAllUsersSecurityLevels(User user) throws GenericEntityException;

    /**
     * Get the different levels of security that the user can see across all projects.
     *
     * @param user   The user used for the security check
     * @return list containing the security levels
     * @since v5.0
     */
    @Nonnull
    Collection<IssueSecurityLevel> getAllSecurityLevelsForUser(final User user);

    /**
     * Get all the different levels of security across all schemes.
     *
     * @return list containing the security levels, can be null
     * @throws GenericEntityException Exception in the OFBiz persistence layer.
     * @since v4.0
     * @deprecated Use {@link #getAllIssueSecurityLevels()} instead. Since v5.0.
     */
    Collection<GenericValue> getAllSecurityLevels() throws GenericEntityException;

    /**
     * Get all the different levels of security across all schemes.
     *
     * @return list containing the security levels, can be null
     * @since v5.0
     */
    Collection<IssueSecurityLevel> getAllIssueSecurityLevels();

    /**
     * Get the different levels of security that a user can see that have the specified name.
     *
     * @param user the user
     * @param securityLevelName the name of the security level.
     * @return a collection of the GenericValues representing each level they can see with the specified name.
     * @throws GenericEntityException Exception in the OFBiz persistence layer.
     * @since v4.0
     *
     * @deprecated Use {@link #getSecurityLevelsForUserByName(com.atlassian.crowd.embedded.api.User, String)} instead. Since v5.0.
     */
    Collection<GenericValue> getUsersSecurityLevelsByName(User user, String securityLevelName) throws GenericEntityException;

    /**
     * Get the different levels of security that a user can see that have the specified name.
     *
     * @param user the user
     * @param securityLevelName the name of the security level.
     * @return a collection of each IssueSecurityLevel they can see with the specified name.
     * @since v5.0
     */
    Collection<IssueSecurityLevel> getSecurityLevelsForUserByName(User user, String securityLevelName);

    /**
     * Get the different levels of security that have the specified name.
     *
     * @param securityLevelName the name of the security level.
     * @return a collection of the GenericValues representing each level with the specified name.
     * @throws GenericEntityException Exception in the OFBiz persistence layer.
     * @since v4.0
     * @deprecated Use {@link #getIssueSecurityLevelsByName(String)} instead. Since v5.0.
     */
    Collection<GenericValue> getSecurityLevelsByName(String securityLevelName) throws GenericEntityException;

    /**
     * Get the different levels of security that have the specified name.
     *
     * @param securityLevelName the name of the security level.
     * @return a collection of the IssueSecurityLevels with the specified name.
     * @since v5.0
     */
    Collection<IssueSecurityLevel> getIssueSecurityLevelsByName(String securityLevelName);

    /**
     * Returns the default Security Level as defined in the Issue Security Level scheme for the given project.
     * @param project the Project
     * @return the default Security Level as defined in the Issue Security Level scheme for the given project. Can be null.
     * @throws GenericEntityException Exception in the OFBiz persistence layer.
     *
     * @deprecated Use {@link #getDefaultSecurityLevel(com.atlassian.jira.project.Project)} instead. Since v5.0.
     */
    Long getSchemeDefaultSecurityLevel(GenericValue project) throws GenericEntityException;

    /**
     * Returns the default Security Level as defined in the Issue Security Level scheme for the given project.
     * @param project the Project
     * @return the default Security Level as defined in the Issue Security Level scheme for the given project. Can be null.
     *
     * @since v5.0
     */
    Long getDefaultSecurityLevel(Project project);

    /**
     * Returns the IssueSecurityLevel with the given ID.
     * @param id the ID
     * @return the IssueSecurityLevel with the given ID.
     * @throws GenericEntityException Exception in the OFBiz persistence layer.
     *
     * @deprecated Use {@link #getSecurityLevel(long)} instead. Since v5.0.
     */
    GenericValue getIssueSecurityLevel(Long id) throws GenericEntityException;

    /**
     * Deletes the given Issue Security Level and any child permissions.
     *
     * @param levelId Issue Security Level ID
     */
    void deleteSecurityLevel(Long levelId);

    @Internal
    void clearUsersLevels();

    @Internal
    void clearProjectLevels(GenericValue project);
}
