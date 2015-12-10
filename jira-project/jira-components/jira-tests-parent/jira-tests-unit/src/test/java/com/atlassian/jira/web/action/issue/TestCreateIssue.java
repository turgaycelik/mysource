package com.atlassian.jira.web.action.issue;

import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.IssueTypeSystemField;
import com.atlassian.jira.issue.fields.ProjectSystemField;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import webwork.action.Action;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CreateIssue}.
 *
 * @since v6.2
 */
public class TestCreateIssue
{
    public static final long FIRST_PROJECT_ID = 4l;
    public static final long SECOND_PROJECT_ID = 5l;
    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @Mock
    private IssueFactory issueFactory;
    @Mock
    private IssueCreationHelperBean issueCreationHelperBean;

    @AvailableInContainer
    @Mock
    private FieldManager fieldManager;
    @AvailableInContainer
    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;
    @AvailableInContainer
    @Mock
    private ProjectManager projectManager;
    @AvailableInContainer
    @Mock
    private PermissionManager permissionManager;
    @AvailableInContainer
    @Mock
    private UserProjectHistoryManager userProjectHistoryManager;

    @Mock
    private User user;

    private CreateIssue createIssue;

    @Before
    public void setUp()
    {
        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(user);

        createIssue = new CreateIssue(issueFactory, issueCreationHelperBean);
    }

    @Test
    public void getSetProject() throws GenericEntityException
    {
        final GenericValue project = mockProject(FIRST_PROJECT_ID);

        createIssue.setPid(FIRST_PROJECT_ID);
        assertEquals(new Long(FIRST_PROJECT_ID), createIssue.getPid());
        assertEquals(project, createIssue.getProject());
    }

    @Test
    public void getNullProject() throws GenericEntityException
    {
        assertNull(createIssue.getProject());
    }

    /**
     * Create two projects with a security permission for one of them. Check that the user can only see one of them in
     * his allowed projects list.
     */
    @Test
    public void getAllowedProjects() throws Exception
    {
        final GenericValue firstProject = mockProject(FIRST_PROJECT_ID);
        final GenericValue secondProject = mockProject(SECOND_PROJECT_ID);

        when(permissionManager.getProjects(Permissions.CREATE_ISSUE, user)).thenReturn(ImmutableList.of(firstProject));

        assertEquals(1, createIssue.getAllowedProjects().size());
        assertTrue(createIssue.getAllowedProjects().contains(firstProject));
    }

    @Test
    public void doDefaultNoSelectedProject() throws Exception
    {
        assertEquals(Action.INPUT, createIssue.doDefault());
        assertNull(createIssue.getProject());
    }

    @Test
    public void doValidationInvalidProject() throws Exception
    {
        when(issueFactory.getIssue()).thenReturn(new MockIssue());

        when(fieldManager.getField(IssueFieldConstants.PROJECT)).thenReturn(mock(ProjectSystemField.class));
        when(fieldManager.getField(IssueFieldConstants.ISSUE_TYPE)).thenReturn(mock(IssueTypeSystemField.class));

        final Map<String, String> mockedErrors = ImmutableMap.of("pid", "No project selected.", "issuetype", "No issue type selected.");
        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable
            {
                final ErrorCollection errors = (ErrorCollection) invocation.getArguments()[3];
                errors.addErrors(mockedErrors);
                return null;
            }
        }).when(issueCreationHelperBean).validateProject(any(Issue.class), any(OperationContext.class), any(Map.class),
                any(ErrorCollection.class), any(I18nHelper.class));

        String result = createIssue.execute();
        assertEquals(2, createIssue.getErrors().size());
        assertEquals(mockedErrors, createIssue.getErrors());
        assertEquals(Action.INPUT, result);
    }

    private GenericValue mockProject(long projectId)
    {
        final GenericValue project = mock(GenericValue.class);
        when(projectManager.getProject(Mockito.eq(projectId))).thenReturn(project);
        return project;
    }
}