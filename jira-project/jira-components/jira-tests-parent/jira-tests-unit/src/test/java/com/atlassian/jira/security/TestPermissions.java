package com.atlassian.jira.security;

import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.util.I18nHelper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import static com.atlassian.jira.security.Permissions.ADMINISTER;
import static com.atlassian.jira.security.Permissions.ASSIGNABLE_USER;
import static com.atlassian.jira.security.Permissions.ASSIGN_ISSUE;
import static com.atlassian.jira.security.Permissions.ATTACHMENT_DELETE_ALL;
import static com.atlassian.jira.security.Permissions.ATTACHMENT_DELETE_OWN;
import static com.atlassian.jira.security.Permissions.BROWSE;
import static com.atlassian.jira.security.Permissions.CLOSE_ISSUE;
import static com.atlassian.jira.security.Permissions.COMMENT_DELETE_ALL;
import static com.atlassian.jira.security.Permissions.COMMENT_DELETE_OWN;
import static com.atlassian.jira.security.Permissions.COMMENT_EDIT_ALL;
import static com.atlassian.jira.security.Permissions.COMMENT_EDIT_OWN;
import static com.atlassian.jira.security.Permissions.COMMENT_ISSUE;
import static com.atlassian.jira.security.Permissions.CREATE_ATTACHMENT;
import static com.atlassian.jira.security.Permissions.CREATE_ISSUE;
import static com.atlassian.jira.security.Permissions.DELETE_ISSUE;
import static com.atlassian.jira.security.Permissions.EDIT_ISSUE;
import static com.atlassian.jira.security.Permissions.LINK_ISSUE;
import static com.atlassian.jira.security.Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS;
import static com.atlassian.jira.security.Permissions.MOVE_ISSUE;
import static com.atlassian.jira.security.Permissions.PROJECT_ADMIN;
import static com.atlassian.jira.security.Permissions.RESOLVE_ISSUE;
import static com.atlassian.jira.security.Permissions.SCHEDULE_ISSUE;
import static com.atlassian.jira.security.Permissions.SYSTEM_ADMIN;
import static com.atlassian.jira.security.Permissions.USE;
import static com.atlassian.jira.security.Permissions.WORKLOG_DELETE_ALL;
import static com.atlassian.jira.security.Permissions.WORKLOG_DELETE_OWN;
import static com.atlassian.jira.security.Permissions.WORKLOG_EDIT_ALL;
import static com.atlassian.jira.security.Permissions.WORKLOG_EDIT_OWN;
import static com.atlassian.jira.security.Permissions.WORK_ISSUE;
import static com.atlassian.jira.security.Permissions.getDescription;
import static com.atlassian.jira.security.Permissions.getShortName;
import static com.atlassian.jira.security.Permissions.getType;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class TestPermissions
{
    private static final String TRANSLATION = "someI18NTranslation";

    @Rule public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @Mock @AvailableInContainer private I18nHelper i18nHelper;
    @Mock @AvailableInContainer private JiraAuthenticationContext jiraAuthenticationContext;
    @Mock @AvailableInContainer private GlobalPermissionManager globalPermissionManager;

    @Before
    public void setup()
    {
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);
    }

    @Test
    public void testGetType()
    {
        assertEquals(SYSTEM_ADMIN, getType("sysadmin"));
        assertEquals(ADMINISTER, getType("admin"));
        assertEquals(USE, getType("use"));
        assertEquals(BROWSE, getType("browse"));
        assertEquals(CREATE_ISSUE, getType("create"));
        assertEquals(CREATE_ATTACHMENT, getType("attach"));
        assertEquals(EDIT_ISSUE, getType("edit"));
        assertEquals(EDIT_ISSUE, getType("update"));
        assertEquals(SCHEDULE_ISSUE, getType("scheduleissue"));
        assertEquals(ASSIGNABLE_USER, getType("assignable"));
        assertEquals(ASSIGN_ISSUE, getType("assign"));
        assertEquals(RESOLVE_ISSUE, getType("resolve"));
        assertEquals(COMMENT_ISSUE, getType("comment"));
        assertEquals(CLOSE_ISSUE, getType("close"));
        assertEquals(WORK_ISSUE, getType("work"));
        assertEquals(WORKLOG_DELETE_ALL, getType("worklogdeleteall"));
        assertEquals(WORKLOG_DELETE_OWN, getType("worklogdeleteown"));
        assertEquals(WORKLOG_EDIT_ALL, getType("worklogeditall"));
        assertEquals(WORKLOG_EDIT_OWN, getType("worklogeditown"));
        assertEquals(LINK_ISSUE, getType("link"));
        assertEquals(DELETE_ISSUE, getType("delete"));
        assertEquals(PROJECT_ADMIN, getType("project"));
        assertEquals(MOVE_ISSUE, getType("move"));
        assertEquals(COMMENT_EDIT_ALL, getType("commenteditall"));
        assertEquals(COMMENT_EDIT_OWN, getType("commenteditown"));
        assertEquals(COMMENT_DELETE_OWN, getType("commentdeleteown"));
        assertEquals(COMMENT_DELETE_ALL, getType("commentdeleteall"));
        assertEquals(ATTACHMENT_DELETE_ALL, getType("attachdeleteall"));
        assertEquals(ATTACHMENT_DELETE_OWN, getType("attachdeleteown"));
        assertEquals(MANAGE_GROUP_FILTER_SUBSCRIPTIONS, getType("groupsubscriptions"));
        assertEquals(-1, getType("foobar"));
    }

    @Test
    public void testGetShortName()
    {
        assertEquals("commenteditall", getShortName(COMMENT_EDIT_ALL));
        assertEquals("commenteditown", getShortName(COMMENT_EDIT_OWN));
        assertEquals("worklogeditown", getShortName(WORKLOG_EDIT_OWN));
        assertEquals("worklogeditall", getShortName(WORKLOG_EDIT_ALL));
        assertEquals("worklogdeleteall", getShortName(WORKLOG_DELETE_ALL));
        assertEquals("worklogdeleteown", getShortName(WORKLOG_DELETE_OWN));
        assertEquals("sysadmin", getShortName(SYSTEM_ADMIN));
    }

    @Test
    public void testGetDescription()
    {
        assertMessageCode(COMMENT_EDIT_ALL, "admin.permissions.descriptions.COMMENT_EDIT_ALL");
        assertMessageCode(COMMENT_EDIT_OWN, "admin.permissions.descriptions.COMMENT_EDIT_OWN");
        assertMessageCode(ATTACHMENT_DELETE_OWN, "admin.permissions.descriptions.ATTACHMENT_DELETE_OWN");
        assertMessageCode(ATTACHMENT_DELETE_ALL, "admin.permissions.descriptions.ATTACHMENT_DELETE_ALL");
        assertMessageCode(COMMENT_DELETE_ALL, "admin.permissions.descriptions.COMMENT_DELETE_ALL");
        assertMessageCode(COMMENT_DELETE_OWN, "admin.permissions.descriptions.COMMENT_DELETE_OWN");
        assertMessageCode(WORKLOG_DELETE_ALL, "admin.permissions.descriptions.WORKLOG_DELETE_ALL");
        assertMessageCode(WORKLOG_DELETE_OWN, "admin.permissions.descriptions.WORKLOG_DELETE_OWN");
        assertMessageCode(WORKLOG_EDIT_ALL, "admin.permissions.descriptions.WORKLOG_EDIT_ALL");
        assertMessageCode(WORKLOG_EDIT_OWN, "admin.permissions.descriptions.WORKLOG_EDIT_OWN");
        assertMessageCode(SYSTEM_ADMIN, "admin.permissions.descriptions.SYS_ADMIN");
        assertMessageCode(ADMINISTER, "admin.permissions.descriptions.ADMINISTER");
    }

    private void assertMessageCode(final int permission, final String key)
    {
        // Set up
        when(i18nHelper.getText(key)).thenReturn(TRANSLATION);

        final String description = getDescription(permission);

        // Check
        assertEquals(TRANSLATION, description);
    }
}
