package com.atlassian.jira.issue.fields;

import java.util.Arrays;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.MockIssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.security.IssueSecurityLevelImpl;
import com.atlassian.jira.issue.security.MockIssueSecurityLevelManager;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.project.MockProject;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test SecurityLevelSystemField
 *
 * @since v3.13
 */
public class TestSecurityLevelSystemField
{
    private MockProjectManager mockProjectManager = new MockProjectManager();

    @Before
    public void setUp()
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker());

        MockIssueFactory.setProjectManager(mockProjectManager);
        mockProjectManager.addProject(new MockProject(1, "COW"));
        mockProjectManager.addProject(new MockProject(2, "DOG"));
    }

    @Test
    public void testNeedsMoveWithDefaultTargetSecurityLevel() throws Exception
    {
        MockIssueSecurityLevelManager mockIssueSecurityLevelManager = new MockIssueSecurityLevelManager();
        mockIssueSecurityLevelManager.setDefaultSecurityLevelForProject(2L, 20000L);
        SecurityLevelSystemField securityLevelSystemField = new SecurityLevelSystemField(null, null, null, null,
                mockIssueSecurityLevelManager, null, null, null);

        MutableIssue mockSourceIssue = MockIssueFactory.createIssue(1, "RANDOM-1", 2);
        MutableIssue mockTargetIssue = MockIssueFactory.createIssue(1, "RANDOM-1", 2);

        // Needs move if the target is a required field
        FieldLayoutItem mockTargetFieldLayoutItem = mock(FieldLayoutItem.class);
        when(mockTargetFieldLayoutItem.isRequired()).thenReturn(true);

        MessagedResult messagedResult = securityLevelSystemField.needsMove(Arrays.asList(mockSourceIssue), mockTargetIssue, mockTargetFieldLayoutItem);
        assertTrue(messagedResult.getResult());
        assertNull(messagedResult.getMessage());

        // Source and target projects are the same - Should not need move
        when(mockTargetFieldLayoutItem.isRequired()).thenReturn(false);

        messagedResult = securityLevelSystemField.needsMove(Arrays.asList(mockSourceIssue), mockTargetIssue, mockTargetFieldLayoutItem);
        assertFalse(messagedResult.getResult());
        assertNull(messagedResult.getMessage());

        // Make the Source and Target Projects different, so that we take notice of the default.
        mockSourceIssue.setProjectId(1L);
        messagedResult = securityLevelSystemField.needsMove(Arrays.asList(mockSourceIssue), mockTargetIssue, mockTargetFieldLayoutItem);
        assertTrue(messagedResult.getResult());
        assertNull(messagedResult.getMessage());
    }

    @Test
    public void testNeedsMoveWithoutDefaultTargetSecurityLevel() throws Exception
    {
        MockIssueSecurityLevelManager mockIssueSecurityLevelManager = new MockIssueSecurityLevelManager();

        MutableIssue mockSourceIssue = MockIssueFactory.createIssue(1, "RANDOM-1", 2);
        MutableIssue mockTargetIssue = MockIssueFactory.createIssue(1, "RANDOM-1", 1);

        SecurityLevelSystemField securityLevelSystemField = new SecurityLevelSystemField(null, null, null, null,
                mockIssueSecurityLevelManager, null, null, null);

        // Needs move if is a required field on the target
        FieldLayoutItem mockTargetFieldLayoutItem = mock(FieldLayoutItem.class);
        when(mockTargetFieldLayoutItem.isRequired()).thenReturn(true);

        MessagedResult messagedResult = securityLevelSystemField.needsMove(Arrays.asList(mockSourceIssue), mockTargetIssue, mockTargetFieldLayoutItem);
        assertTrue(messagedResult.getResult());
        assertNull(messagedResult.getMessage());

        // .. but not if the not required field
        when(mockTargetFieldLayoutItem.isRequired()).thenReturn(false);

        messagedResult = securityLevelSystemField.needsMove(Arrays.asList(mockSourceIssue), mockTargetIssue, mockTargetFieldLayoutItem);
        assertFalse(messagedResult.getResult());
        assertNull(messagedResult.getMessage());
    }

    @Test
    public void testNeedsMoveWithSecurityLevelOnOriginalIssue() throws Exception
    {
        MockIssueSecurityLevelManager mockIssueSecurityLevelManager = new MockIssueSecurityLevelManager();
        mockIssueSecurityLevelManager.addIssueSecurityLevel(new IssueSecurityLevelImpl(10001L, "Blue", null, null));

        MockIssueFactory.setIssueSecurityLevelManager(mockIssueSecurityLevelManager);
        MutableIssue mockSourceIssue = MockIssueFactory.createIssue(1, "RANDOM-1", 2);
        mockSourceIssue.setSecurityLevelId(10001L);
        MutableIssue mockTargetIssue = MockIssueFactory.createIssue(1, "RANDOM-1", 1);

        SecurityLevelSystemField securityLevelSystemField = new SecurityLevelSystemField(null, null, null, new MockAuthenticationContext(null),
                mockIssueSecurityLevelManager, null, null, null);

        // Needs move because user does not have this security level
        MessagedResult messagedResult = securityLevelSystemField.needsMove(Arrays.asList(mockSourceIssue), mockTargetIssue, null);
        assertTrue(messagedResult.getResult());
        assertNull(messagedResult.getMessage());
    }

    @Test
    public void testDoesNotNeedMoveWithSecurityLevelOnOriginalIssue() throws Exception
    {
        MockIssueSecurityLevelManager mockIssueSecurityLevelManager = new MockIssueSecurityLevelManager();
        mockIssueSecurityLevelManager.addIssueSecurityLevel(new IssueSecurityLevelImpl(10001L, "Blue", null, null));

        MockIssueFactory.setIssueSecurityLevelManager(mockIssueSecurityLevelManager);
        MutableIssue mockSourceIssue = MockIssueFactory.createIssue(1, "RANDOM-1", 2);
        mockSourceIssue.setSecurityLevelId(10001L);
        MutableIssue mockTargetIssue = MockIssueFactory.createIssue(1, "RANDOM-1", 1);

        // Let the user have access to current security level in new project
        mockIssueSecurityLevelManager.setUsersSecurityLevelsResult(Arrays.asList(mockIssueSecurityLevelManager.getSecurityLevel(10001L)));

        SecurityLevelSystemField securityLevelSystemField = new SecurityLevelSystemField(null, null, null, new MockAuthenticationContext(null),
                mockIssueSecurityLevelManager, null, null, null);

        // Needs move because user does not have this security level
        MessagedResult messagedResult = securityLevelSystemField.needsMove(Arrays.asList(mockSourceIssue), mockTargetIssue, null);
        assertFalse(messagedResult.getResult());
        assertNull(messagedResult.getMessage());
    }
}
