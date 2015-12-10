package com.atlassian.jira.security.type;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.impl.MultiGroupCFType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.MockCustomField;
import com.atlassian.jira.issue.managers.MockCustomFieldManager;
import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.issue.security.IssueSecurityLevelImpl;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.MockGroupManager;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.MockI18nBean;

import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.apache.lucene.search.Query;
import org.junit.Test;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestGroupCF
{
    private static final String CUSTOMFIELDID = "customfield_10000";

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
        assertEquals("Please select a valid group custom field.", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    private void testDoValidation(CustomFieldSearcher customFieldSearcher, String customFieldId, JiraServiceContext jiraServiceContext)
    {
        Map parameters = new HashMap();
        parameters.put("groupCF", customFieldId);

        Mock mockCustomField = new Mock(CustomField.class);
        mockCustomField.expectAndReturn("getCustomFieldSearcher", P.ANY_ARGS, customFieldSearcher);
        mockCustomField.expectAndReturn("getName", P.ANY_ARGS, "TestCustomField");

        Mock mockCustomFieldManager = new Mock(CustomFieldManager.class);
        mockCustomFieldManager.expectAndReturn("getCustomFieldObject", new Constraint[] { P.eq(CUSTOMFIELDID) }, (CustomField) mockCustomField.proxy());

        final GroupCF groupCF = new GroupCF(null, null, (CustomFieldManager) mockCustomFieldManager.proxy(), null);
        groupCF.doValidation(null, parameters, jiraServiceContext);
    }

    @Test
    public void testGetQueryWithNullProject() throws Exception
    {
        GroupCF groupCF = new GroupCF(null, null, null, null);
        Query query = groupCF.getQuery(new MockUser("fred"), null, "developers");

        assertNull(query);
    }

    @Test
    public void testGetQueryWithProjectOnly() throws Exception
    {
        MockGroupManager mockGroupManager = new MockGroupManager();
        mockGroupManager.addUserToGroup(new MockUser("fred"), new MockGroup("jira-users"));
        mockGroupManager.addUserToGroup(new MockUser("fred"), new MockGroup("jira-devs"));

        final MockGenericValue projectGV = new MockGenericValue("Project", FieldMap.build("id", 12L));
        MockProject mockProject = new MockProject(12, "ABC", "Blah", projectGV);
        MockComponentWorker mockComponentWorker = new MockComponentWorker();
        PermissionSchemeManager mockPermissionSchemeManager = Mockito.mock(PermissionSchemeManager.class);
        final MockGenericValue schemeGV = new MockGenericValue("Scheme");
        Mockito.when(mockPermissionSchemeManager.getSchemes(projectGV)).thenReturn(Arrays.<GenericValue>asList(schemeGV));
        Mockito.when(mockPermissionSchemeManager.getEntities(schemeGV, "groupCF", new Long(Permissions.BROWSE))).thenReturn(Arrays.<GenericValue>asList(new MockGenericValue("rubbish")));

        mockComponentWorker.registerMock(PermissionSchemeManager.class, mockPermissionSchemeManager);
        ComponentAccessor.initialiseWorker(mockComponentWorker);

        MockCustomFieldManager mockCustomFieldManager = new MockCustomFieldManager();
        MultiGroupCFType multiGroupCFType = new MultiGroupCFType(null, null, null, null, null, null, null, null);

        mockCustomFieldManager.addCustomField(new MockCustomField("customfield_10000", "", multiGroupCFType));

        GroupCF groupCF = new GroupCF(null, null, mockCustomFieldManager, mockGroupManager);
        Query query = groupCF.getQuery(new MockUser("fred"), mockProject, "customfield_10000");

        assertEquals("(+projid:12 +(customfield_10000:jira-devs customfield_10000:jira-users))", query.toString());
    }

    @Test
    public void testGetQueryWithSecurityLevel() throws Exception
    {
        MockGroupManager mockGroupManager = new MockGroupManager();
        mockGroupManager.addUserToGroup(new MockUser("fred"), new MockGroup("jira-users"));
        mockGroupManager.addUserToGroup(new MockUser("fred"), new MockGroup("jira-devs"));

        final MockGenericValue projectGV = new MockGenericValue("Project", FieldMap.build("id", 12L));
        MockProject mockProject = new MockProject(12, "ABC", "Blah", projectGV);
        MockComponentWorker mockComponentWorker = new MockComponentWorker();
        PermissionSchemeManager mockPermissionSchemeManager = Mockito.mock(PermissionSchemeManager.class);
        final MockGenericValue schemeGV = new MockGenericValue("Scheme");
        Mockito.when(mockPermissionSchemeManager.getSchemes(projectGV)).thenReturn(Arrays.<GenericValue>asList(schemeGV));
        Mockito.when(mockPermissionSchemeManager.getEntities(schemeGV, "groupCF", new Long(Permissions.BROWSE))).thenReturn(Arrays.<GenericValue>asList(new MockGenericValue("rubbish")));

        mockComponentWorker.registerMock(PermissionSchemeManager.class, mockPermissionSchemeManager);
        ComponentAccessor.initialiseWorker(mockComponentWorker);

        MockCustomFieldManager mockCustomFieldManager = new MockCustomFieldManager();
        MultiGroupCFType multiGroupCFType = new MultiGroupCFType(null, null, null, null, null, null, null, null);

        mockCustomFieldManager.addCustomField(new MockCustomField("customfield_10000", "", multiGroupCFType));

        GroupCF groupCF = new GroupCF(null, null, mockCustomFieldManager, mockGroupManager);
        IssueSecurityLevel securityLevel = new IssueSecurityLevelImpl(10100L, "Blue", "", 20L);
        Query query = groupCF.getQuery(new MockUser("fred"), new MockProject(12, "ABC"), securityLevel, "customfield_10000");

        assertEquals("+(+issue_security_level:10100 +(customfield_10000:jira-devs customfield_10000:jira-users))", query.toString());
    }
}
