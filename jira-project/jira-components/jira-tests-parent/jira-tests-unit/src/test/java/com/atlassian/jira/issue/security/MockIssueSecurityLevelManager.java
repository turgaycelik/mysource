package com.atlassian.jira.issue.security;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.Project;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

/**
 * Mock IssueSecurityLevelManager
 *
 * @since v3.13
 */
public class MockIssueSecurityLevelManager implements IssueSecurityLevelManager
{
    private Map<Long, Long> defaultSecurityLevelMap = new HashMap<Long, Long>();
    private Map<Long, IssueSecurityLevel> securityLevelMap = new HashMap<Long, IssueSecurityLevel>();

    private List<IssueSecurityLevel> usersSecurityLevelsResult = Collections.emptyList();

    @Override
    public List<GenericValue> getSchemeIssueSecurityLevels(final Long schemeId)
    {
        return null;
    }

    @Override
    public List<IssueSecurityLevel> getIssueSecurityLevels(long schemeId)
    {
        // TODO: Implement Me!
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public boolean schemeIssueSecurityExists(final Long id)
    {
        return false;
    }

    @Override
    public String getIssueSecurityName(final Long id)
    {
        return null;
    }

    @Override
    public String getIssueSecurityDescription(final Long id)
    {
        return null;
    }

    @Override
    public GenericValue getIssueSecurity(final Long id)
    {
        return null;
    }

    @Override
    public IssueSecurityLevel getSecurityLevel(long id)
    {
        return securityLevelMap.get(id);
    }

    @Override
    public IssueSecurityLevel createIssueSecurityLevel(long schemeId, String name, String description)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<GenericValue> getUsersSecurityLevels(final GenericValue entity, final User user) throws GenericEntityException
    {
        return null;
    }

    @Override
    public List<IssueSecurityLevel> getUsersSecurityLevels(Issue issue, User user)
    {
        return usersSecurityLevelsResult;
    }

    public void setUsersSecurityLevelsResult(List<IssueSecurityLevel> usersSecurityLevels)
    {
        this.usersSecurityLevelsResult = usersSecurityLevels;
    }

    @Override
    public List<IssueSecurityLevel> getUsersSecurityLevels(Project project, User user)
    {
        // TODO: Implement Me!
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Collection<GenericValue> getAllUsersSecurityLevels(final User user) throws GenericEntityException
    {
        return null;
    }

    @Override
    public Collection<IssueSecurityLevel> getAllSecurityLevelsForUser(User user)
    {
        // TODO: Implement Me!
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Collection<GenericValue> getAllSecurityLevels() throws GenericEntityException
    {
        return null;
    }

    @Override
    public Collection<IssueSecurityLevel> getAllIssueSecurityLevels()
    {
        // TODO: Implement Me!
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Collection<GenericValue> getUsersSecurityLevelsByName(final User user, final String securityLevelName)
            throws GenericEntityException
    {
        return null;
    }

    @Override
    public Collection<IssueSecurityLevel> getSecurityLevelsForUserByName(User user, String securityLevelName)
    {
        // TODO: Implement Me!
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Collection<GenericValue> getSecurityLevelsByName(final String securityLevelName)
            throws GenericEntityException
    {
        return null;
    }

    @Override
    public Collection<IssueSecurityLevel> getIssueSecurityLevelsByName(String securityLevelName)
    {
        // TODO: Implement Me!
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Long getSchemeDefaultSecurityLevel(final GenericValue project) throws GenericEntityException
    {
        if (project == null)
        {
            return null;
        }
        return defaultSecurityLevelMap.get(project.getLong("id"));
    }

    @Override
    public Long getDefaultSecurityLevel(Project project)
    {
        if (project == null)
        {
            return null;
        }
        return defaultSecurityLevelMap.get(project.getId());
    }

    public void setDefaultSecurityLevelForProject(final Long projectId, final Long defaultSecurityLevelId) throws GenericEntityException
    {
        defaultSecurityLevelMap.put(projectId, defaultSecurityLevelId);
    }

    @Override
    public GenericValue getIssueSecurityLevel(final Long id) throws GenericEntityException
    {
        IssueSecurityLevel securityLevel = securityLevelMap.get(id);

        if (securityLevel == null)
            return null;
        return new MockGenericValue(Entity.ISSUE_SECURITY_LEVEL.getEntityName(), Entity.ISSUE_SECURITY_LEVEL.fieldMapFrom(securityLevel));
    }

    @Override
    public void deleteSecurityLevel(final Long levelId)
    {
    }

    @Override
    public void clearUsersLevels()
    {
    }

    @Override
    public void clearProjectLevels(final GenericValue project)
    {
    }

    public void addIssueSecurityLevel(IssueSecurityLevelImpl securityLevel)
    {
        securityLevelMap.put(securityLevel.getId(), securityLevel);
    }
}
