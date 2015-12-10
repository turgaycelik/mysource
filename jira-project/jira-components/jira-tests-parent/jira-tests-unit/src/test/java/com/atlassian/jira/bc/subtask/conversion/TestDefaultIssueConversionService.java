package com.atlassian.jira.bc.subtask.conversion;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.event.issue.IssueEventBundle;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.IssueTypeImpl;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.WorkflowManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the DefaultIssueConversionService
 */
@RunWith (MockitoJUnitRunner.class)
public class TestDefaultIssueConversionService
{
    private DefaultIssueConversionService service;
    private MockIssue mockIssue;

    @Mock
    private CustomField customField;
    @Mock
    private FieldManager fieldManager;
    @Mock
    private IssueEventManager issueEventManager;
    @Mock
    private IssueEventBundleFactory issueEventBundleFactory;

    private static final Long ID_0 = new Long(0);

    @Before
    public void setUp() throws Exception
    {
        mockIssue = new MockIssue(ID_0);
        mockIssue.setIssueTypeObject(new IssueTypeImpl(new MockGenericValue("issueType", EasyMap.build("id", ID_0)), null, null, null, null));

        when(fieldManager.isCustomField(any(Field.class))).thenReturn(Boolean.TRUE);

        service = new MockDefaultIssueConversionService(null, null, null, null, null, fieldManager, issueEventManager, issueEventBundleFactory);
    }

    @Test
    public void testIsShouldCheckFieldValueNonCustomField()
    {
        when(fieldManager.isCustomField(any(Field.class))).thenReturn(Boolean.FALSE);
        when(customField.isInScope(any(Project.class), anyListOf(String.class))).thenReturn(Boolean.TRUE);

        assertTrue(service.isShouldCheckFieldValue(mockIssue, customField));
    }

    @Test
    public void testIsShouldCheckFieldValueTrue()
    {
        when(customField.isInScope(any(Project.class), anyListOf(String.class))).thenReturn(Boolean.TRUE);

        assertTrue(service.isShouldCheckFieldValue(mockIssue, customField));
    }

    @Test
    public void testIsShouldCheckFieldValueFalse()
    {
        when(customField.isInScope(any(Project.class), anyListOf(String.class))).thenReturn(Boolean.FALSE);

        assertFalse(service.isShouldCheckFieldValue(mockIssue, customField));
    }
    
    @Test
    public void testDispatchEventsDispatchesTheIssueEventCorrectly()
    {
        Issue issue = mock(Issue.class);
        User user = mock(User.class);
        GenericValue changeLog = new MockGenericValue("ChangeLog");

        service.dispatchEvents(issue, serviceContextWith(user), changeLog, issueChangeHolderWithSubtasksUpdated());

        verify(issueEventManager).dispatchRedundantEvent(EventType.ISSUE_UPDATED_ID, issue, user, changeLog, true, true);
    }
    
    @Test
    public void testDispatchEventsDispatchesIssueEventBundleCorrectly()
    {
        Issue issue = mock(Issue.class);
        ApplicationUser user = mock(ApplicationUser.class);
        GenericValue changeLog = new MockGenericValue("ChangeLog");

        IssueEventBundle issueEventBundle = mock(IssueEventBundle.class);
        when(issueEventBundleFactory.createIssueUpdateEventBundle(any(Issue.class), any(GenericValue.class), any(IssueUpdateBean.class), any(ApplicationUser.class))).thenReturn(issueEventBundle);

        service.dispatchEvents(issue, serviceContextWith(user), changeLog, issueChangeHolderWithSubtasksUpdated());

        verify(issueEventManager).dispatchEvent(issueEventBundle);
    }

    private JiraServiceContext serviceContextWith(final ApplicationUser user)
    {
        JiraServiceContext context = mock(JiraServiceContext.class);
        when(context.getLoggedInApplicationUser()).thenReturn(user);
        return context;
    }

    private JiraServiceContext serviceContextWith(User user)
    {
        JiraServiceContext context = mock(JiraServiceContext.class);
        when(context.getLoggedInUser()).thenReturn(user);
        return context;
    }

    private IssueChangeHolder issueChangeHolderWithSubtasksUpdated()
    {
        IssueChangeHolder changeHolder = mock(IssueChangeHolder.class);
        when(changeHolder.isSubtasksUpdated()).thenReturn(true);
        return changeHolder;
    }

    /**
     * We only want to test methods from the abstract class here.  We therefore need this Mock implementation, such that
     * we can instantiate the abstract class.
     */
    private class MockDefaultIssueConversionService extends DefaultIssueConversionService
    {
        public MockDefaultIssueConversionService(
                PermissionManager permissionManager,
                WorkflowManager workflowManager,
                FieldLayoutManager fieldLayoutManager,
                IssueTypeSchemeManager issueTypeSchemeManager,
                JiraAuthenticationContext jiraAuthenticationContext,
                FieldManager fieldManager,
                IssueEventManager issueEventManager,
                IssueEventBundleFactory issueEventBundleFactory
        ) {
            super(permissionManager, workflowManager, fieldLayoutManager, issueTypeSchemeManager, jiraAuthenticationContext, fieldManager, issueEventManager, issueEventBundleFactory);
        }

        protected boolean canIssueSecurityFieldIgnore()
        {
            return false;
        }

        public boolean canConvertIssue(JiraServiceContext context, Issue issue)
        {
            return false;
        }

        public void validateTargetIssueType(JiraServiceContext context, Issue issue, IssueType issueType, final String fieldNameIssueTypeId)
        {

        }

        public void preStoreUpdates(JiraServiceContext context, IssueChangeHolder changeHolder, Issue currentIssue, MutableIssue targetIssue)
        {

        }
    }
}
