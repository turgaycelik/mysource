package com.atlassian.jira.issue.search.handlers;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.ProjectSearcher;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.permission.ProjectClauseValueSanitiser;
import com.atlassian.jira.jql.query.ProjectClauseQueryFactory;
import com.atlassian.jira.jql.resolver.ProjectResolver;
import com.atlassian.jira.jql.validator.ProjectValidator;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;

import org.junit.Test;

/**
 * A test for {@link com.atlassian.jira.issue.search.handlers.ProjectSearchHandlerFactory}.
 *
 * @since v4.0
 */
public class TestProjectSearchHandlerFactory extends AbstractTestSimpleSearchHandlerFactory
{
    @Test
    public void testCreateHandler() throws Exception
    {
        final ProjectResolver projectResolver = new ProjectResolver(mockController.getMock(ProjectManager.class));
        mockController.addObjectInstance(projectResolver);

        _testSystemSearcherHandler(ProjectSearchHandlerFactory.class,
                ProjectClauseQueryFactory.class,
                ProjectValidator.class,
                SystemSearchConstants.forProject(),
                ProjectSearcher.class,
                new ProjectClauseValueSanitiser(
                        mockController.getMock(PermissionManager.class),
                        mockController.getMock(JqlOperandResolver.class),
                        projectResolver));
    }
}
