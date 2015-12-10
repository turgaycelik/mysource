package com.atlassian.jira.plugin.report;


import java.text.MessageFormat;

import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.module.ModuleFactory;

import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;

public class ReportModuleDescriptorImplTest
{
    @Rule
    public final TestRule initMocks = new InitMockitoMocks(this);
    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;
    @Mock
    private ModuleFactory moduleFactory;
    @Mock
    private Plugin plugin;
    @Mock
    private Project project;
    private final Element element = new DOMElement("plugin");
    private final String moduleKey = "module-key";
    private final String pluginKey = "plugin-key";
    private final long projectId = 123123L;
    private final String projectName = "pr-name";

    @Before
    public void setUp() throws Exception
    {

        element.addAttribute("key", moduleKey);
        Mockito.when(plugin.getKey()).thenReturn(pluginKey);
        Mockito.when(project.getId()).thenReturn(projectId);
        Mockito.when(project.getName()).thenReturn(projectName);
    }

    @Test
    public void shouldIncludeUrlFromParameterInGetUrl() throws Exception
    {
        final ReportModuleDescriptor reportModuleDescriptor = new ReportModuleDescriptorImpl(jiraAuthenticationContext, moduleFactory);
        final String someFancyUrl = "someFancyUrl";
        element.addElement("url").setText(someFancyUrl);
        reportModuleDescriptor.init(plugin, element);

        final String url = reportModuleDescriptor.getUrl(project);
        final String expectedUrl = MessageFormat.format(ReportModuleDescriptorImpl.PARAMS_PATTERN, someFancyUrl, projectId, projectName, pluginKey + ":" + moduleKey);
        assertThat(url, CoreMatchers.equalTo(expectedUrl));
        assertThat(url,CoreMatchers.containsString(String.valueOf(projectId)));
        assertThat(url,CoreMatchers.containsString("project-"+String.valueOf(projectId)));
    }

    @Test
    public void shouldUseDefaultUrlWhenUrlParameterNotProvided() throws Exception
    {
        final ReportModuleDescriptor reportModuleDescriptor = new ReportModuleDescriptorImpl(jiraAuthenticationContext, moduleFactory);

        reportModuleDescriptor.init(plugin, element);

        final String url = reportModuleDescriptor.getUrl(project);

        final String expectedUrl = MessageFormat.format(ReportModuleDescriptorImpl.PARAMS_PATTERN,"/secure/ConfigureReport!default.jspa", projectId, projectName, pluginKey + ":" + moduleKey);
        assertThat(url, CoreMatchers.equalTo(expectedUrl));
        assertThat(url,CoreMatchers.containsString(String.valueOf(projectId)));

    }
}
