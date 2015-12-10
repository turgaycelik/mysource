package com.atlassian.jira.web.sitemesh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.soy.SoyTemplateRendererProvider;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.model.WebPanel;

import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.same;
import static org.easymock.classextension.EasyMock.createNiceControl;
import static org.easymock.classextension.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.4
 */
public class TestAdminDecoratorHelper
{
    private IMocksControl control;
    private WebInterfaceManager webInterfaceManager;
    private ProjectService projectService;
    private JiraAuthenticationContext authenticationContext;
    private ComponentFactory jiraComponentFactory;
    private SimpleLinkManager simpleLinkManager;
    private MockUser user;
    private SoyTemplateRendererProvider soyTemplateProvider;

    @Before
    public void setUp() throws Exception
    {
        control = createNiceControl();
        webInterfaceManager = control.createMock(WebInterfaceManager.class);
        projectService = control.createMock(ProjectService.class);
        user = new MockUser("bbain");
        authenticationContext = new MockSimpleAuthenticationContext(user, Locale.ENGLISH, new NoopI18nHelper());
        jiraComponentFactory = control.createMock(ComponentFactory.class);
        simpleLinkManager = control.createMock(SimpleLinkManager.class);
        soyTemplateProvider = control.createMock(SoyTemplateRendererProvider.class);
    }

    @Test
    public void testGetHeadersNoProjectKey() throws Exception
    {
        String panel1Content = "panel1";
        String panel2Content = "panel2";

        Map<String, Object> expectedContext = MapBuilder.<String, Object>build("admin.active.section", "some.section", "admin.active.tab", "blah","adminNavigationPrimary",new ArrayList<Map<String,Object>>());

        WebPanel panel1 = control.createMock(WebPanel.class);
        expect(panel1.getHtml(expectedContext)).andStubReturn(panel1Content);
        WebPanel panel2 = control.createMock(WebPanel.class);
        expect(panel2.getHtml(expectedContext)).andStubReturn(panel2Content);

        expect(webInterfaceManager.getDisplayableWebPanels("system.admin.decorator.header", Collections.<String, Object>emptyMap()))
                .andReturn(CollectionBuilder.list(panel1, panel2));

        expect(simpleLinkManager.getSectionsForLocation(eq("system.admin.top"), same(user), isA(JiraHelper.class)))
                .andReturn(Collections.<SimpleLinkSection>emptyList());

        control.replay();

        AdminDecoratorHelper helper = new AdminDecoratorHelper(webInterfaceManager, projectService,
                authenticationContext, simpleLinkManager, soyTemplateProvider);

        helper.setCurrentSection("some.section");
        helper.setCurrentTab("blah");

        assertFalse(helper.hasKey());
        final List<AdminDecoratorHelper.Header> actualHeaders = helper.getHeaders();
        assertEquals(2, actualHeaders.size());
        assertEquals(panel1Content, actualHeaders.get(0).getHtml());
        assertEquals(panel2Content, actualHeaders.get(1).getHtml());

        assertTrue(helper.isHasHeader());
        assertEquals(panel1Content + panel2Content, helper.getHeaderHtml());

        control.verify();
    }

    @Test
    public void testGetHeadersWithoutProject() throws Exception
    {
        String key = "ABC";
        expect(projectService.getProjectByKeyForAction(user, key, ProjectAction.EDIT_PROJECT_CONFIG))
                .andReturn(new ProjectService.GetProjectResult(error("Not Found")));

        expect(webInterfaceManager.getDisplayableWebPanels("system.admin.decorator.header", Collections.<String, Object>emptyMap()))
                .andReturn(Collections.<WebPanel>emptyList());

        expect(simpleLinkManager.getSectionsForLocation(eq("system.admin.top"), same(user), isA(JiraHelper.class)))
                .andReturn(Collections.<SimpleLinkSection>emptyList());

        control.replay();

        AdminDecoratorHelper helper = new AdminDecoratorHelper(webInterfaceManager, projectService,
                authenticationContext, simpleLinkManager, soyTemplateProvider);

        helper.setProject(key);
        assertTrue(helper.hasKey());
        assertEquals(Collections.<AdminDecoratorHelper.Header>emptyList(), helper.getHeaders());
        assertEquals(Collections.<AdminDecoratorHelper.Header>emptyList(), helper.getHeaders());

        assertFalse(helper.isHasHeader());
        assertTrue(isEmpty(helper.getHeaderHtml()));

        control.verify();
    }

    @Test
    public void testGetHeadersProject() throws Exception
    {
        String key = "ABC";
        String panel1Content = "panel1";
        String panel2Content = "panel2";

        Project project = new MockProject(101010L, key);
        Map<String, Object> expectedContext = new HashMap<String,Object>();
        expectedContext.put("project", project);
        expectedContext.put("admin.active.section", null);
        expectedContext.put("admin.active.tab", "blah");
        expectedContext.put("adminNavigationPrimary",new ArrayList<Map<String,Object>>());
        expectedContext.put("adminNavigationSecondary",new ArrayList<Map<String,Object>>());

        expect(projectService.getProjectByKeyForAction(user, key, ProjectAction.EDIT_PROJECT_CONFIG))
                .andReturn(new ProjectService.GetProjectResult(ok(), project));

        WebPanel panel1 = control.createMock(WebPanel.class);
        expect(panel1.getHtml(expectedContext)).andStubReturn(panel1Content);
        WebPanel panel2 = control.createMock(WebPanel.class);
        expect(panel2.getHtml(expectedContext)).andStubReturn(panel2Content);

        expect(webInterfaceManager.getDisplayableWebPanels("atl.jira.proj.config.header", Collections.<String, Object>emptyMap()))
                .andReturn(CollectionBuilder.list(panel1, panel2));

        control.replay();

        AdminDecoratorHelper helper = new AdminDecoratorHelper(webInterfaceManager, projectService,
                authenticationContext, simpleLinkManager, soyTemplateProvider)
        {
            protected String encode(String string)
            {
                return string;
            }
        };

        helper.setProject(key);
        assertTrue(helper.hasKey());

        helper.setCurrentTab("blah");

        List<AdminDecoratorHelper.Header> actualHeaders = helper.getHeaders();

        assertEquals(2, actualHeaders.size());
        assertEquals(panel1Content, actualHeaders.get(0).getHtml());
        assertEquals(panel2Content, actualHeaders.get(1).getHtml());

        assertTrue(helper.isHasHeader());
        assertEquals(panel1Content + panel2Content, helper.getHeaderHtml());

        //The last result should have been cached.
        assertSame(actualHeaders, helper.getHeaders());
        control.verify();
    }

    private ErrorCollection ok()
    {
        return new SimpleErrorCollection();
    }

    private ErrorCollection error(String... msgs)
    {
        SimpleErrorCollection simpleErrorCollection = new SimpleErrorCollection();
        simpleErrorCollection.addErrorMessages(Arrays.asList(msgs));
        return simpleErrorCollection;
    }
}
