package com.atlassian.jira.web.action.admin.subtasks;

import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockAvatar;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import webwork.action.Action;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.when;


public class TestEditSubTaskIssueTypes
{
    @Rule
    public MockitoContainer mockitoMocksInContainer = MockitoMocksInContainer.rule(this);

    EditSubTaskIssueTypes editSubTaskIssueTypesAction;

    @Mock
    private ConstantsManager mockConstantMaanger;

    @Mock
    private SubTaskManager mockSubTaskManager;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext authenticationContext;

    @AvailableInContainer (instantiateMe = true)
    private MockI18nHelper mockI18nHelper;

    @AvailableInContainer (instantiateMe = true)
    private MockRedirectSanitiser redirectSanitiser;

    private final String id = "1";
    private final String iconurl = "test iconurl";
    private final String name = "test name";
    private final Long sequence = new Long(0);
    private final String description = "test description";

    @Before
    public void setUp() throws Exception
    {
        when(authenticationContext.getI18nHelper()).thenReturn(mockI18nHelper);
        editSubTaskIssueTypesAction = new EditSubTaskIssueTypes(mockSubTaskManager, mockConstantMaanger);
    }

    @AfterClass
    public static void cleanupRequestAndResponse() throws Exception
    {
        JiraTestUtil.resetRequestAndResponse();
    }

    @Test
    public void testDoDefaultSubTasksDisabled() throws Exception
    {
        when(mockSubTaskManager.isSubTasksEnabled()).thenReturn(false);
        final String result = editSubTaskIssueTypesAction.doDefault();

        assertEquals(Action.ERROR, result);
        assertEquals(ImmutableList.of("admin.errors.subtasks.disabled"), editSubTaskIssueTypesAction.getErrorMessages());
    }

    @Test
    public void testDoDefaultNoId() throws Exception
    {
        when(mockSubTaskManager.isSubTasksEnabled()).thenReturn(true);
        final String result = editSubTaskIssueTypesAction.doDefault();

        assertEquals(Action.ERROR, result);
        assertEquals(ImmutableList.of("admin.errors.no.id.set"), editSubTaskIssueTypesAction.getErrorMessages());

    }

    @Test
    public void testDoDefault() throws Exception
    {
        final MockIssueType issueType = new MockIssueType(id, name, true);
        issueType.setSequence(sequence);
        issueType.setIconUrl(iconurl);
        issueType.setDescription(description);

        when(mockSubTaskManager.isSubTasksEnabled()).thenReturn(true);
        when(mockSubTaskManager.getSubTaskIssueType(id)).thenReturn(issueType);

        editSubTaskIssueTypesAction.setId(id);
        final String result = editSubTaskIssueTypesAction.doDefault();

        assertEquals(Action.INPUT, result);
        assertEquals(id, editSubTaskIssueTypesAction.getId());
        assertEquals(name, editSubTaskIssueTypesAction.getName());
        assertEquals(sequence, editSubTaskIssueTypesAction.getSequence());
        assertEquals(description, editSubTaskIssueTypesAction.getDescription());
        assertEquals(iconurl, editSubTaskIssueTypesAction.getIconurl());
    }

    @Test
    public void testDoValidationSubTasksDisabled() throws Exception
    {
        when(mockSubTaskManager.isSubTasksEnabled()).thenReturn(false);
        final String result = editSubTaskIssueTypesAction.execute();

        assertEquals(Action.INPUT, result);
        assertEquals(ImmutableList.of("admin.errors.subtasks.are.disabled"), editSubTaskIssueTypesAction.getErrorMessages());
    }

    @Test
    public void testDoValidationNoId() throws Exception
    {
        when(mockSubTaskManager.isSubTasksEnabled()).thenReturn(true);
        final String result = editSubTaskIssueTypesAction.execute();

        assertEquals(Action.INPUT, result);
        assertEquals(ImmutableList.of("admin.errors.no.id.set"), editSubTaskIssueTypesAction.getErrorMessages());
    }

    @Test
    public void testDoValidationNoNameNoIconUrl() throws Exception
    {
        when(mockSubTaskManager.isSubTasksEnabled()).thenReturn(true);

        editSubTaskIssueTypesAction.setId(id);
        final String result = editSubTaskIssueTypesAction.execute();

        assertEquals(Action.INPUT, result);

        final Map errorMessages = editSubTaskIssueTypesAction.getErrors();

        assertNotNull(errorMessages);
        assertEquals(
                ImmutableMap.of(
                        "name", "admin.errors.specify.a.name.for.this.new.sub.task.issue.type",
                        "iconurl", "admin.errors.issuetypes.must.specify.url"
                ),
                editSubTaskIssueTypesAction.getErrors()
        );
    }

    @Test
    public void testDoValidationDuplicateName() throws Exception
    {
        final String anotherId = "2";
        final String anotherName = "another name";
        final IssueType issueType = new MockIssueType(anotherId, anotherName, true, new MockAvatar(3, null, null, null, null, false));

        when(mockSubTaskManager.isSubTasksEnabled()).thenReturn(true);
        when(mockConstantMaanger.getIssueConstantByName("IssueType", anotherName)).thenReturn(issueType);

        editSubTaskIssueTypesAction.setId(id);
        editSubTaskIssueTypesAction.setName(anotherName);
        editSubTaskIssueTypesAction.setIconurl(iconurl);

        final String result = editSubTaskIssueTypesAction.execute();

        assertEquals(Action.INPUT, result);

        final Map errorMessages = editSubTaskIssueTypesAction.getErrors();

        assertNotNull(errorMessages);
        assertEquals(ImmutableMap.of("name", "admin.errors.issue.type.with.this.name.already.exists"), editSubTaskIssueTypesAction.getErrors());
    }

    @Test
    public void testDoValidationSameName() throws Exception
    {
        final IssueType issueType = new MockIssueType(id, name);
        MockHttpServletResponse mockHttpServletResponse = JiraTestUtil.setupExpectedRedirect("ManageSubTasks.jspa");

        when(mockSubTaskManager.isSubTasksEnabled()).thenReturn(true);
        when(mockConstantMaanger.getIssueConstantByName("IssueType", name)).thenReturn(issueType);

        editSubTaskIssueTypesAction.setId(id);
        editSubTaskIssueTypesAction.setName(name);
        editSubTaskIssueTypesAction.setSequence(sequence);
        editSubTaskIssueTypesAction.setDescription(description);
        editSubTaskIssueTypesAction.setIconurl(iconurl);

        final String result = editSubTaskIssueTypesAction.execute();

        assertEquals(Action.NONE, result);

        mockHttpServletResponse.verify();
    }
}
