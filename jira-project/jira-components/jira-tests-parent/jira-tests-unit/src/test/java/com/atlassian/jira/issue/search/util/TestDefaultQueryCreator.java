package com.atlassian.jira.issue.search.util;

import java.util.Collections;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.MockConstantsManager;
import com.atlassian.jira.mock.MockPermissionManager;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

public class TestDefaultQueryCreator
{
    @Rule
    public final RuleChain mockContainer = MockitoMocksInContainer.forTest(this);
    @org.mockito.Mock
    @AvailableInContainer
    private PluginAccessor pluginAccessor;
    @org.mockito.Mock
    @AvailableInContainer
    private PluginEventManager pluginEventManager;
    private static final String DEFAULT_SEARCH_STRING = "IssueNavigator.jspa?reset=true&mode=show";

    @Test
    public void testNullQuery()
    {
        QueryCreator queryCreator = new DefaultQueryCreator(getNullProjectManager(), getNullConstantsManager(), getNullVersionManager(), getNullPermissionsManager(), getAuthenticationContext(), getNullProjectComponentManager(), getDateTimeFormatterFactory(), getTimeZoneManager(), getApplicationProperties());
        Assert.assertEquals("IssueNavigator.jspa?mode=show", queryCreator.createQuery(null));  //JRA-6084
    }

    @Test
    public void testStandardQuery()
    {
        QueryCreator queryCreator = new DefaultQueryCreator(getNullProjectManager(), getNullConstantsManager(), getNullVersionManager(), getNullPermissionsManager(), getAuthenticationContext(), getNullProjectComponentManager(), getDateTimeFormatterFactory(), getTimeZoneManager(), getApplicationProperties());

         assertThat( queryCreator.createQuery(""), containsString(DEFAULT_SEARCH_STRING));
         assertThat( queryCreator.createQuery("abc"), containsString(DEFAULT_SEARCH_STRING + "&text=abc"));
         assertThat( queryCreator.createQuery("abc&def"), containsString(DEFAULT_SEARCH_STRING + "&text=abc%26def"));
    }

    @Test
    public void testInternationalQuery()
    {
        QueryCreator queryCreator = new DefaultQueryCreator(getNullProjectManager(), getNullConstantsManager(), getNullVersionManager(), getNullPermissionsManager(), getAuthenticationContext(), getNullProjectComponentManager(), getDateTimeFormatterFactory(), getTimeZoneManager(), getApplicationProperties());

         assertThat( queryCreator.createQuery(""), containsString(DEFAULT_SEARCH_STRING));
         assertThat( queryCreator.createQuery("h" + '\ufffd' + "r"), containsString(DEFAULT_SEARCH_STRING + "&text=h%EF%BF%BDr"));
    }

    @Test
    public void testProjectKeyQuery()
    {
        ProjectManager projectManager = getProjectByKeyProjectManager();

        QueryCreator queryCreator = new DefaultQueryCreator(projectManager, getNullConstantsManager(), getNullVersionManager(), getNullPermissionsManager(), getAuthenticationContext(), getNullProjectComponentManager(), getDateTimeFormatterFactory(), getTimeZoneManager(), getApplicationProperties());

         assertThat( queryCreator.createQuery(""), containsString(DEFAULT_SEARCH_STRING));
         assertThat( queryCreator.createQuery("PRJ"), containsString(DEFAULT_SEARCH_STRING + "&pid=77"));
         assertThat( queryCreator.createQuery("PRJ abc"), containsString(DEFAULT_SEARCH_STRING + "&text=abc&pid=77"));
    }

    @Test
    public void testProjectNameQuery()
    {
        ProjectManager projectManager = new MockProjectManager()
        {
            public Project getProjectObjByKey(String key)
            {
                return null;
            }

            @Override
            public Project getProjectObjByName(String name)
            {
                if ("project-name".equals(name))
                    return new MockProject(new MockGenericValue("Project", EasyMap.build("id", new Long(77))));
                else
                    return null;
            }
        };

        QueryCreator queryCreator = new DefaultQueryCreator(projectManager, getNullConstantsManager(), getNullVersionManager(), getNullPermissionsManager(), getAuthenticationContext(), getNullProjectComponentManager(), getDateTimeFormatterFactory(), getTimeZoneManager(), getApplicationProperties());

         assertThat( queryCreator.createQuery(""), containsString(DEFAULT_SEARCH_STRING));
         assertThat( queryCreator.createQuery("project-name abc"), containsString(DEFAULT_SEARCH_STRING + "&text=abc&pid=77"));
    }

    @Test
    public void testStatusQuery()
    {
        MockConstantsManager mockConstantsManager = new MockConstantsManager();
        mockConstantsManager.addStatus(new MockGenericValue("Status", EasyMap.build("id", new Long(77), "name", "open")));

        QueryCreator queryCreator = new DefaultQueryCreator(getNullProjectManager(), mockConstantsManager, getNullVersionManager(), getNullPermissionsManager(), getAuthenticationContext(), getNullProjectComponentManager(), getDateTimeFormatterFactory(), getTimeZoneManager(), getApplicationProperties());

         assertThat( queryCreator.createQuery("open"), containsString(DEFAULT_SEARCH_STRING + "&status=77"));
         assertThat( queryCreator.createQuery("open abc"), containsString(DEFAULT_SEARCH_STRING + "&text=abc&status=77"));
         assertThat( queryCreator.createQuery("Open abc"), containsString(DEFAULT_SEARCH_STRING + "&text=abc&status=77")); // case sensitivity
    }

    @Test
    public void testTypeQuery()
    {
        MockConstantsManager mockConstantsManager = new MockConstantsManager();
        mockConstantsManager.addIssueType(new MockGenericValue("IssueType", EasyMap.build("id", new Long(89), "name", "bug")));

        QueryCreator queryCreator = new DefaultQueryCreator(getNullProjectManager(), mockConstantsManager, getNullVersionManager(), getNullPermissionsManager(), getAuthenticationContext(), getNullProjectComponentManager(), getDateTimeFormatterFactory(), getTimeZoneManager(), getApplicationProperties());

         assertThat( queryCreator.createQuery("bug"), containsString(DEFAULT_SEARCH_STRING + "&" + DocumentConstants.ISSUE_TYPE + "=89"));
         assertThat( queryCreator.createQuery("bug abc"), containsString(DEFAULT_SEARCH_STRING + "&text=abc&" + DocumentConstants.ISSUE_TYPE + "=89"));
         assertThat( queryCreator.createQuery("Bugs abc"), containsString(DEFAULT_SEARCH_STRING + "&text=abc&" + DocumentConstants.ISSUE_TYPE + "=89")); // plurals
    }

    @Test
    public void testMyIssues()
    {
        QueryCreator queryCreator = new DefaultQueryCreator(getNullProjectManager(), getNullConstantsManager(), getNullVersionManager(), getNullPermissionsManager(), getAuthenticationContext(), getNullProjectComponentManager(), getDateTimeFormatterFactory(), getTimeZoneManager(), getApplicationProperties());
         assertThat( queryCreator.createQuery("my"), containsString(DEFAULT_SEARCH_STRING + "&assigneeSelect=issue_current_user"));
         assertThat( queryCreator.createQuery("my abc"), containsString(DEFAULT_SEARCH_STRING + "&text=abc&assigneeSelect=issue_current_user"));
    }

    @Test
    public void testProjectAndStatusAndTypeQuery()
    {
        ProjectManager projectManager = getProjectByKeyProjectManager();

        MockConstantsManager mockConstantsManager = new MockConstantsManager();
        mockConstantsManager.addStatus(new MockGenericValue("Status", EasyMap.build("id", new Long(77), "name", "open")));
        mockConstantsManager.addIssueType(new MockGenericValue("IssueType", EasyMap.build("id", new Long(89), "name", "bug")));

        QueryCreator queryCreator = new DefaultQueryCreator(projectManager, mockConstantsManager, getNullVersionManager(), getNullPermissionsManager(), getAuthenticationContext(), getNullProjectComponentManager(), getDateTimeFormatterFactory(), getTimeZoneManager(), getApplicationProperties());

        String query = queryCreator.createQuery("PRJ open bug abc");
        assertThat(query, startsWith(DEFAULT_SEARCH_STRING));
        // The query's parameters order depends on the JDK that is used. So test their presence as order does not really matter.
        assertThat(query, containsString("text=abc"));
        assertThat(query, containsString(DocumentConstants.ISSUE_TYPE + "=89"));
        assertThat(query, containsString("pid=77"));
        assertThat(query, containsString("status=77"));
    }


    private ProjectManager getProjectByKeyProjectManager()
    {
        ProjectManager projectManager = new MockProjectManager()
        {

            public Project getProjectObjByKey(String key)
            {
                if ("PRJ".equals(key))
                    return new MockProject(77L);
                else
                    return null;
            }

            public Project getProjectObjByName(String name)
            {
                return null;
            }

        };
        return projectManager;
    }

    private DateTimeFormatterFactory getDateTimeFormatterFactory()
    {
        Mock dateTimeFormatterFactory = new Mock(DateTimeFormatterFactory.class);
        return (DateTimeFormatterFactory) dateTimeFormatterFactory.proxy();
    }
    
    private TimeZoneManager getTimeZoneManager()
    {
        Mock timeZoneManager = new Mock(TimeZoneManager.class);
        return (TimeZoneManager) timeZoneManager.proxy();
    }

    private ConstantsManager getNullConstantsManager()
    {
        return new MockConstantsManager();
    }

    private VersionManager getNullVersionManager()
    {
        Mock versionManagerMock = new Mock(VersionManager.class);
        versionManagerMock.expectAndReturn("getVersions", P.ANY_ARGS, Collections.EMPTY_LIST);
        return (VersionManager) versionManagerMock.proxy();
    }

    private ProjectManager getNullProjectManager()
    {
        Mock projectManagerMock = new Mock(ProjectManager.class);
        projectManagerMock.expectAndReturn("getProjectByKey", P.ANY_ARGS, null);
        projectManagerMock.expectAndReturn("getProjectByName", P.ANY_ARGS, null);

        return (ProjectManager) projectManagerMock.proxy();
    }

    private ProjectComponentManager getNullProjectComponentManager()
    {
        Mock projectComponentManagerMock = new Mock(ProjectComponentManager.class);
        projectComponentManagerMock.expectAndReturn("findAllForProject", P.ANY_ARGS, null);

        return (ProjectComponentManager) projectComponentManagerMock.proxy();
    }

    private ApplicationProperties getApplicationProperties()
    {
        MockApplicationProperties applicationProperties = new MockApplicationProperties();
        applicationProperties.setEncoding("UTF-8");
        return applicationProperties;
    }

    private PermissionManager getNullPermissionsManager()
    {
        PermissionManager permissionManager = new MockPermissionManager()
        {
            public boolean hasPermission(int permissionsId, Project entity, ApplicationUser u)
            {
                return true;
            }
        };
        return permissionManager;
    }

    private JiraAuthenticationContext getAuthenticationContext()
    {
        return new MockSimpleAuthenticationContext(null) {
            @Override
            public OutlookDate getOutlookDate()
            {
                return null;
            }
        };
    }
}
