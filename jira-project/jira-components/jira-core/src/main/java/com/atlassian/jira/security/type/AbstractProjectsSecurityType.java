package com.atlassian.jira.security.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.project.Project;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.ofbiz.core.entity.GenericValue;

public abstract class AbstractProjectsSecurityType extends AbstractSecurityType
{
    @Override
    public Query getQuery(User searcher, Project project, String parameter)
    {
        if (project == null)
        {
            return null;
        }

        return new TermQuery(new Term(DocumentConstants.PROJECT_ID, project.getId().toString()));
    }

    @Override
    public Query getQuery(User searcher, Project project, IssueSecurityLevel securityLevel, String parameter)
    {
        // Ignore project for most types.
        return getQuery(securityLevel);
    }

    protected Query getQuery(IssueSecurityLevel securityLevel)
    {
        // We wish to ensure that the search has the value of the field
        return new TermQuery(new Term(DocumentConstants.ISSUE_SECURITY_LEVEL, securityLevel.getId().toString()));
    }
}
