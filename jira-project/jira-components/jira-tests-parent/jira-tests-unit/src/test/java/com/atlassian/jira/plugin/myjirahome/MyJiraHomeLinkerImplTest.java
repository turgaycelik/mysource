package com.atlassian.jira.plugin.myjirahome;

import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebLink;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@SuppressWarnings("UnusedDeclaration")
@RunWith(JUnit4.class)
public class MyJiraHomeLinkerImplTest
{
    public static final String MY_PLUGIN_MODULE_KEY = "my.plugin.module";

    @Mock private User mockUser;
    @Mock private PluginAccessor mockPluginAccessor;
    @Mock private MyJiraHomePreference mockMyJiraHomePreference;
    @Mock private ProjectService projectService;
    @Mock private FeatureManager featureManager;

    private MyJiraHomeLinkerImpl resolver;

    @Before
    public void setup()
    {
        initMocks(this);
        when(mockMyJiraHomePreference.findHome(null)).thenReturn("");
        when(projectService.getAllProjects((User) null)).thenReturn(new ServiceOutcomeImpl<List<Project>>(new SimpleErrorCollection(), Lists.<Project>newArrayList()));
        when(featureManager.isOnDemand()).thenReturn(true);
        resolver = new MyJiraHomeLinkerImpl(mockPluginAccessor, mockMyJiraHomePreference, projectService, featureManager);
    }

    @Test
    public void testGetHomeAsLinkNoUserLoggedIn()
    {
        expectPluginModuleIsEnabled();

        final String result = resolver.getHomeLink(null);
        assertThat(result, is(MyJiraHomeLinkerImpl.DEFAULT_HOME_OD_ANON));
    }

    @Test
    public void testGetHomeAsLinkNoUserLoggedInBTF()
    {
        when(featureManager.isOnDemand()).thenReturn(false);
        expectPluginModuleIsEnabled();

        final String result = resolver.getHomeLink(null);
        assertThat(result, is(MyJiraHomeLinkerImpl.DEFAULT_HOME_NOT_ANON));
    }

    @Test
    public void testGetHomeAsLinkHomeIsEmpty()
    {
        when(mockMyJiraHomePreference.findHome(mockUser)).thenReturn("");
        
        final String result = resolver.getHomeLink(mockUser);
        assertThat(result, is(MyJiraHomeLinkerImpl.DEFAULT_HOME_NOT_ANON));
    }

    @Test
    public void testGetHomeAsLinkHomePluginModuleUnknown()
    {
        expectPreferenceReturnMyPluginModuleKey();
        doThrow(new IllegalArgumentException("unknown")).when(mockPluginAccessor).isPluginModuleEnabled(MY_PLUGIN_MODULE_KEY);
        
        final String result = resolver.getHomeLink(mockUser);
        assertThat(result, is(MyJiraHomeLinkerImpl.DEFAULT_HOME_NOT_ANON));
    }

    @Test
    public void testGetHomeAsLinkHomePluginModuleNotEnabled()
    {
        expectPreferenceReturnMyPluginModuleKey();
        when(mockPluginAccessor.isPluginModuleEnabled(MY_PLUGIN_MODULE_KEY)).thenReturn(Boolean.FALSE);

        final String result = resolver.getHomeLink(mockUser);
        assertThat(result, is(MyJiraHomeLinkerImpl.DEFAULT_HOME_NOT_ANON));
    }

    @Test
    public void testGetHomeAsLinkHomePluginModuleNotAWebItem()
    {
        final ModuleDescriptor mockWebPanelModuleDescription = mock(WebPanelModuleDescriptor.class);

        expectPluginModuleIsEnabled();
        expectPreferenceReturnMyPluginModuleKey();
        when(mockPluginAccessor.getPluginModule(MY_PLUGIN_MODULE_KEY)).thenReturn(mockWebPanelModuleDescription);

        final String result = resolver.getHomeLink(mockUser);
        assertThat(result, is(MyJiraHomeLinkerImpl.DEFAULT_HOME_NOT_ANON));
    }

    @Test
    public void testGetHomeAsLinkHome()
    {
        final WebItemModuleDescriptor mockWebItemModuleDescriptor = mock(WebItemModuleDescriptor.class);
        final WebLink mockWebLink = mock(WebLink.class);

        expectPluginModuleIsEnabled();
        expectPreferenceReturnMyPluginModuleKey();
        when(mockPluginAccessor.getPluginModule(MY_PLUGIN_MODULE_KEY)).thenReturn((ModuleDescriptor) mockWebItemModuleDescriptor);
        when(mockWebItemModuleDescriptor.getLink()).thenReturn(mockWebLink);
        when(mockWebLink.getRenderedUrl(anyMap())).thenReturn("/my-home");

        final String result = resolver.getHomeLink(mockUser);
        assertThat(result, is("/my-home"));
    }

    private void expectPreferenceReturnMyPluginModuleKey()
    {
        when(mockMyJiraHomePreference.findHome(mockUser)).thenReturn(MY_PLUGIN_MODULE_KEY);
    }

    private void expectPluginModuleIsEnabled()
    {
        when(mockPluginAccessor.isPluginModuleEnabled(MY_PLUGIN_MODULE_KEY)).thenReturn(Boolean.TRUE);
    }
}
