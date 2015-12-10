package com.atlassian.jira.security.type;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.permission.PermissionContextImpl;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.MockI18nBean;

import com.google.common.collect.ImmutableList;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestUserCF
{
    private static final String CUSTOMFIELDID = "customfield_10000";

    @Test
    public void testConvertToValueSetNull() throws Exception
    {
        final UserCF userCF = new UserCF(null, null);
        final Set<User> ret = userCF.convertToUserSet(null);
        assertNotNull(ret);
        assertTrue(ret.isEmpty());
    }

    @Test
    public void testConvertToValueSetObject() throws Exception
    {
        final UserCF userCF = new UserCF(null, null);
        final ApplicationUser applicationUser = new MockApplicationUser("jacqui");
        final Set<User> ret = userCF.convertToUserSet(applicationUser);
        assertNotNull(ret);
        assertEquals(1, ret.size());
        assertTrue(ret.contains(applicationUser.getDirectoryUser()));
    }

    @Test
    public void testConvertToValueSetCollections() throws Exception
    {
        final UserCF userCF = new UserCF(null, null);

        ApplicationUser user1 = new MockApplicationUser("a");
        ApplicationUser user2 = new MockApplicationUser("b");
        List<ApplicationUser> list = ImmutableList.of(user1, user2);
        Set<User> ret = userCF.convertToUserSet(list);
        assertEquals(2, ret.size());
        assertTrue(ret.contains(user1.getDirectoryUser()));
        assertTrue(ret.contains(user2.getDirectoryUser()));
    }

    @Test
    public void testDoValidationWithCustomFieldWithSearcher()
    {
        Mock mockCustomFieldSearcher = new Mock(CustomFieldSearcher.class);
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl((User) null, new SimpleErrorCollection())
        {
            public I18nHelper getI18nBean()
            {
                return new MockI18nBean();
            }
        };
        testDoValidation((CustomFieldSearcher) mockCustomFieldSearcher.proxy(), CUSTOMFIELDID, jiraServiceContext);
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testDoValidationWithCustomFieldWithoutSearcher()
    {
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl((User) null, new SimpleErrorCollection())
        {
            public I18nHelper getI18nBean()
            {
                return new MockI18nBean();
            }
        };
        testDoValidation(null, CUSTOMFIELDID, jiraServiceContext);
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertEquals("Custom field 'TestCustomField' is not indexed for searching - please add a Search Template to this Custom Field.",
                jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void testDoValidationWithNoCustomField()
    {
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl((User) null, new SimpleErrorCollection())
        {
            public I18nHelper getI18nBean()
            {
                return new MockI18nBean();
            }
        };
        testDoValidation(null, null, jiraServiceContext);
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertEquals("Please select a valid user custom field.", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    private void testDoValidation(CustomFieldSearcher customFieldSearcher, String customFieldId, JiraServiceContext jiraServiceContext)
    {
        Map<String,String> parameters = EasyMap.build("userCF", customFieldId);

        Mock mockCustomField = new Mock(CustomField.class);
        mockCustomField.expectAndReturn("getCustomFieldSearcher", P.ANY_ARGS, customFieldSearcher);
        mockCustomField.expectAndReturn("getName", P.ANY_ARGS, "TestCustomField");

        Mock mockCustomFieldManager = new Mock(CustomFieldManager.class);
        mockCustomFieldManager.expectAndReturn("getCustomFieldObject", new Constraint[] { P.eq(CUSTOMFIELDID) }, mockCustomField.proxy());

        final UserCF userCF = new UserCF(null, (CustomFieldManager) mockCustomFieldManager.proxy());
        userCF.doValidation(null, parameters, jiraServiceContext);
    }

    @Test
    public void testGetUsersNoIssueInContext() throws Exception
    {
        final UserCF userCF = new UserCF(null, null);
        final Set<User> users = userCF.getUsers(new PermissionContextImpl(null, null, null), "customField");
        assertTrue(users.isEmpty());
    }

    @Test
    public void testGetUsersIssueInContext() throws Exception
    {
        final ApplicationUser applicationUser = new MockApplicationUser("maggie");
        final CustomField mockCustomField = Mockito.mock(CustomField.class);

        final MockIssue mockIssue = new MockIssue();
        final FieldManager fieldManager = Mockito.mock(FieldManager.class);

        Mockito.when(fieldManager.getCustomField("customField")).thenReturn(mockCustomField);

        final UserCF userCF = new UserCF(null, null)
        {
            FieldManager getFieldManager()
            {
                return fieldManager;
            }

            Object getValuesFromIssue(final CustomField field, final Issue issue)
            {
                assertEquals(mockIssue, issue);
                return applicationUser;
            }
        };

        final Set<User> users = userCF.getUsers(new PermissionContextImpl(mockIssue, null, null), "customField");
        assertEquals(1, users.size());
        assertEquals(applicationUser.getDirectoryUser(), users.iterator().next());
    }
}
