package com.atlassian.jira.jql.permission;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.local.MockControllerTestCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestTimeTrackingPermissionChecker extends MockControllerTestCase
{

    private ApplicationProperties applicationProperties;
    private FieldClausePermissionChecker.Factory fieldClausePermissionCheckerFactory;
    private FieldClausePermissionChecker fieldClausePermissionChecker;

    @Before
    public void setUp() throws Exception
    {
        applicationProperties = mockController.getMock(ApplicationProperties.class);

        fieldClausePermissionChecker = mockController.getMock(FieldClausePermissionChecker.class);

        fieldClausePermissionCheckerFactory = mockController.getMock(FieldClausePermissionChecker.Factory.class);
        fieldClausePermissionCheckerFactory.createPermissionChecker(IssueFieldConstants.TIMETRACKING);
        mockController.setReturnValue(fieldClausePermissionChecker);
    }

    @Test
    public void testHasPermissionToUseClauseTimeTrackingDisabled() throws Exception
    {
        applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
        mockController.setReturnValue(false);
        mockController.replay();

        final TimeTrackingPermissionChecker checker = new TimeTrackingPermissionChecker(fieldClausePermissionCheckerFactory, applicationProperties);
        assertFalse(checker.hasPermissionToUseClause(null));
        mockController.verify();
    }

    @Test
    public void testHasPermissionToUseClauseTimeTrackingHidden() throws Exception
    {
        applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
        mockController.setReturnValue(true);

        fieldClausePermissionChecker.hasPermissionToUseClause(null);
        mockController.setReturnValue(false);
        mockController.replay();

        final TimeTrackingPermissionChecker checker = new TimeTrackingPermissionChecker(fieldClausePermissionCheckerFactory, applicationProperties);
        assertFalse(checker.hasPermissionToUseClause(null));
        mockController.verify();
    }

    @Test
    public void testHasPermissionToUseClauseHappyPath() throws Exception
    {
        applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
        mockController.setReturnValue(true);

        fieldClausePermissionChecker.hasPermissionToUseClause(null);
        mockController.setReturnValue(true);
        mockController.replay();

        final TimeTrackingPermissionChecker checker = new TimeTrackingPermissionChecker(fieldClausePermissionCheckerFactory, applicationProperties);
        assertTrue(checker.hasPermissionToUseClause(null));
        mockController.verify();
    }
}
