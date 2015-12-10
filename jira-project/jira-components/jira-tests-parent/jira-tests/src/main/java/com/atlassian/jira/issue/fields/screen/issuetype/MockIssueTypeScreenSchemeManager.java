package com.atlassian.jira.issue.fields.screen.issuetype;

import java.util.Collection;
import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.project.Project;

import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

import org.ofbiz.core.entity.GenericValue;

/**
 * A simple implementation of {@link IssueTypeScreenSchemeManager} for tests.
 *
 * @since v6.2
 */
public class MockIssueTypeScreenSchemeManager implements IssueTypeScreenSchemeManager
{
    private Map<Long, IssueTypeScreenScheme> projectToScheme = Maps.newHashMap();
    private Map<Long, IssueTypeScreenScheme> idToScheme = Maps.newHashMap();

    @Override
    public Collection<IssueTypeScreenScheme> getIssueTypeScreenSchemes()
    {
        return idToScheme.values();
    }

    @Override
    public IssueTypeScreenScheme getIssueTypeScreenScheme(final Long id)
    {
        return idToScheme.get(id);
    }

    @Override
    public IssueTypeScreenScheme getIssueTypeScreenScheme(final GenericValue project)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public IssueTypeScreenScheme getIssueTypeScreenScheme(final Project project)
    {
        final IssueTypeScreenScheme issueTypeScreenScheme = projectToScheme.get(project.getId());
        if (issueTypeScreenScheme == null)
        {
            return getDefaultScheme();
        }
        else
        {
            return issueTypeScreenScheme;
        }
    }

    @Override
    public FieldScreenScheme getFieldScreenScheme(final Issue issue)
    {
        final IssueTypeScreenScheme issueTypeScreenScheme = getIssueTypeScreenScheme(issue.getProjectObject());
        IssueTypeScreenSchemeEntity entity = issueTypeScreenScheme.getEntity(issue.getIssueTypeId());
        if (entity == null)
        {
            entity = issueTypeScreenScheme.getEntity(null);
        }

        return entity.getFieldScreenScheme();
    }

    @Override
    public Collection getIssueTypeScreenSchemeEntities(final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        return issueTypeScreenScheme.getEntities();
    }

    public MockIssueTypeScreenScheme createIssueTypeScreenScheme(Project... projects)
    {
        final MockIssueTypeScreenScheme scheme = new MockIssueTypeScreenScheme().id(getNextSchemeId());
        createIssueTypeScreenScheme(scheme);
        for (Project project : projects)
        {
            addSchemeAssociation(project, scheme);
        }
        return scheme;
    }

    public MockIssueTypeScreenScheme createDefault()
    {
        final MockIssueTypeScreenScheme scheme = new MockIssueTypeScreenScheme()
                .id(IssueTypeScreenScheme.DEFAULT_SCHEME_ID);

        createIssueTypeScreenScheme(scheme);
        return scheme;
    }

    private long getNextSchemeId()
    {
        if (projectToScheme.isEmpty())
        {
            return IssueTypeScreenScheme.DEFAULT_SCHEME_ID + 1;
        }
        else
        {
            final long max = Ordering.natural().max(projectToScheme.keySet());
            if (max <= IssueTypeScreenScheme.DEFAULT_SCHEME_ID)
            {
                return IssueTypeScreenScheme.DEFAULT_SCHEME_ID + 1;
            }
            else
            {
                return max;
            }
        }
    }

    @Override
    public void createIssueTypeScreenScheme(final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        idToScheme.put(issueTypeScreenScheme.getId(), issueTypeScreenScheme);
    }

    @Override
    public void updateIssueTypeScreenScheme(final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        idToScheme.put(issueTypeScreenScheme.getId(), issueTypeScreenScheme);
    }

    @Override
    public void removeIssueTypeSchemeEntities(final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void removeIssueTypeScreenScheme(final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        idToScheme.remove(issueTypeScreenScheme.getId());
    }

    @Override
    public void createIssueTypeScreenSchemeEntity(final IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void updateIssueTypeScreenSchemeEntity(final IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void removeIssueTypeScreenSchemeEntity(final IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Collection getIssueTypeScreenSchemes(final FieldScreenScheme fieldScreenScheme)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void addSchemeAssociation(final GenericValue project, final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void addSchemeAssociation(final Project project, final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        projectToScheme.put(project.getId(), issueTypeScreenScheme);
    }

    @Override
    public void removeSchemeAssociation(final GenericValue project, final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void removeSchemeAssociation(final Project project, final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        projectToScheme.remove(project.getId());
    }

    @Override
    public Collection<GenericValue> getProjects(final IssueTypeScreenScheme issueTypeScreenScheme)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void associateWithDefaultScheme(final GenericValue project)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void associateWithDefaultScheme(final Project project)
    {
        addSchemeAssociation(project, getDefaultScheme());
    }

    @Override
    public IssueTypeScreenScheme getDefaultScheme()
    {
        return getIssueTypeScreenScheme(IssueTypeScreenScheme.DEFAULT_SCHEME_ID);
    }

    @Override
    public void refresh()
    {
    }
}
