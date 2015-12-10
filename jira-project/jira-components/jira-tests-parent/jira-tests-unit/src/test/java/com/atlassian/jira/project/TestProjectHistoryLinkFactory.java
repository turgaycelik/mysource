package com.atlassian.jira.project;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.web.api.WebItem;
import com.atlassian.plugin.web.api.model.WebFragmentBuilder;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.plugin.webfragment.EqWebItem.eqWebItem;
import static com.google.common.collect.Iterables.isEmpty;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith (ListeningMockitoRunner.class)
public class TestProjectHistoryLinkFactory
{
    @Mock
    private VelocityRequestContext requestContext;
    @Mock
    private VelocityRequestContextFactory requestContextFactory;
    @Mock
    private UserProjectHistoryManager historyManager;
    @Mock
    private I18nHelper.BeanFactory i18nFactory;
    private User user;

    private ProjectHistoryLinkFactory linkFactory;

    @Before
    public void setUp() throws Exception
    {
        user = new MockUser("admin");
        linkFactory = new ProjectHistoryLinkFactory(requestContextFactory, historyManager, i18nFactory);
    }

    @After
    public void tearDown() throws Exception
    {
        requestContext = null;
        requestContextFactory = null;
        linkFactory = null;
        user = null;
        i18nFactory = null;
        historyManager = null;

    }

    @Test
    public void testNullUserEmptyHistory()
    {
        when(historyManager.getProjectHistoryWithPermissionChecks(ProjectAction.VIEW_ISSUES, null)).thenReturn(Lists.<Project>newArrayList());

        assertTrue(isEmpty(linkFactory.getItems(Maps.<String, Object>newHashMap())));
    }

    @Test
    public void testEmptyHistory()
    {
        when(historyManager.getProjectHistoryWithPermissionChecks(ProjectAction.VIEW_ISSUES, user)).thenReturn(Lists.<Project>newArrayList());

        assertTrue(isEmpty(linkFactory.getItems(MapBuilder.<String, Object>build("user", user))));
    }

    @Test
    public void testOneHistoryNullUserNullCurrent()
    {
        final GenericValue gv = new MockGenericValue("Project", MapBuilder.build("avatar", 1L));
        final MockProject project = new MockProject(1L, "ONE", "Project One", gv);
        final Avatar avatar = Mockito.mock(Avatar.class);
        when(avatar.getId()).thenReturn(11L);
        project.setAvatar(avatar);

        when(historyManager.getProjectHistoryWithPermissionChecks(ProjectAction.VIEW_ISSUES, null)).thenReturn(Lists.newArrayList((Project) project));

        when(requestContextFactory.getJiraVelocityRequestContext()).thenReturn(requestContext);
        when(requestContext.getBaseUrl()).thenReturn("");

        when(historyManager.getCurrentProject(Permissions.BROWSE, null)).thenReturn(null);

        when(i18nFactory.getInstance((User) null)).thenReturn(new MockI18nHelper());

        final Iterable<WebItem> actualItems = linkFactory.getItems(Maps.<String, Object>newHashMap());

        final WebItem link = new WebFragmentBuilder(20).
                id("proj_lnk_1").
                label("Project One (ONE)").
                title("tooltip.browseproject.specified [Project One]").
                addParam("iconUrl", "/secure/projectavatar?pid=1&avatarId=11&size=small").
                webItem("browse_link/project_history_main").
                url("/browse/ONE").build();
        assertThat(actualItems, hasItems(eqWebItem(link)));
    }

    @Test
    public void testOneHistoryNullCurrent()
    {
        final GenericValue gv = new MockGenericValue("Project", MapBuilder.build("avatar", 1L));
        final MockProject project = new MockProject(1L, "ONE", "Project One", gv);
        final Avatar avatar = Mockito.mock(Avatar.class);
        when(avatar.getId()).thenReturn(11L);
        project.setAvatar(avatar);

        when(historyManager.getProjectHistoryWithPermissionChecks(ProjectAction.VIEW_ISSUES, user)).thenReturn(Lists.newArrayList((Project) project));

        when(requestContextFactory.getJiraVelocityRequestContext()).thenReturn(requestContext);
        when(requestContext.getBaseUrl()).thenReturn("/jira");

        when(historyManager.getCurrentProject(Permissions.BROWSE, user)).thenReturn(null);

        when(i18nFactory.getInstance(user)).thenReturn(new MockI18nHelper());

        final Iterable<WebItem> actualItems = linkFactory.getItems(MapBuilder.<String, Object>build("user", user));

        final WebItem link = new WebFragmentBuilder(20).
                id("proj_lnk_1").
                label("Project One (ONE)").
                title("tooltip.browseproject.specified [Project One]").
                addParam("iconUrl", "/jira/secure/projectavatar?pid=1&avatarId=11&size=small").
                webItem("browse_link/project_history_main").
                url("/jira/browse/ONE").build();
        assertThat(actualItems, hasItems(eqWebItem(link)));
    }

    @Test
    public void testOneHistoryDiffCurrent()
    {
        final GenericValue gv = new MockGenericValue("Project", MapBuilder.build("avatar", 1L));
        final MockProject project = new MockProject(1L, "ONE", "Project One", gv);
        final Project curProject = new MockProject(22L);
        final Avatar avatar = Mockito.mock(Avatar.class);
        when(avatar.getId()).thenReturn(11L);
        project.setAvatar(avatar);

        when(historyManager.getProjectHistoryWithPermissionChecks(ProjectAction.VIEW_ISSUES, user)).thenReturn(Lists.newArrayList((Project) project));

        when(requestContextFactory.getJiraVelocityRequestContext()).thenReturn(requestContext);
        when(requestContext.getBaseUrl()).thenReturn("/jira");

        when(historyManager.getCurrentProject(Permissions.BROWSE, user)).thenReturn(curProject);

        when(i18nFactory.getInstance(user)).thenReturn(new MockI18nHelper());

        final Iterable<WebItem> actualItems = linkFactory.getItems(MapBuilder.<String, Object>build("user", user));

        final WebItem link = new WebFragmentBuilder(20).
                id("proj_lnk_1").
                label("Project One (ONE)").
                title("tooltip.browseproject.specified [Project One]").
                addParam("iconUrl", "/jira/secure/projectavatar?pid=1&avatarId=11&size=small").
                webItem("browse_link/project_history_main").
                url("/jira/browse/ONE").build();
        assertThat(actualItems, hasItems(eqWebItem(link)));
    }

    @Test
    public void testOneHistorySameCurrent()
    {
        final GenericValue gv = new MockGenericValue("Project", MapBuilder.build("avatar", 1L));
        final MockProject project = new MockProject(1L, "ONE", "Project One", gv);
        final Avatar avatar = Mockito.mock(Avatar.class);
        when(avatar.getId()).thenReturn(11L);
        project.setAvatar(avatar);

        when(historyManager.getProjectHistoryWithPermissionChecks(ProjectAction.VIEW_ISSUES, user)).thenReturn(Lists.newArrayList((Project) project));

        when(requestContextFactory.getJiraVelocityRequestContext()).thenReturn(requestContext);
        when(requestContext.getBaseUrl()).thenReturn("/jira");

        when(historyManager.getCurrentProject(Permissions.BROWSE, user)).thenReturn(project);

        when(i18nFactory.getInstance(user)).thenReturn(new MockI18nHelper());

        final Iterable<WebItem> actualItems = linkFactory.getItems(MapBuilder.<String, Object>build("user", user));
        assertTrue(Iterables.isEmpty(actualItems));
    }
}
