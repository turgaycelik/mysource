package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.issuetype.IssueType;

import java.util.Collection;

/**
 * Resolves IssueType objects.
 *
 * @since v4.0
 */
public class IssueTypeResolver extends ConstantsNameResolver<IssueType>
{
    private final ConstantsManager constantsManager;

    public IssueTypeResolver(final ConstantsManager constantsManager)
    {
        super(constantsManager, ConstantsManager.ISSUE_TYPE_CONSTANT_TYPE);
        this.constantsManager = constantsManager;
    }

    public Collection<IssueType> getAll()
    {
        return constantsManager.getAllIssueTypeObjects();
    }

}