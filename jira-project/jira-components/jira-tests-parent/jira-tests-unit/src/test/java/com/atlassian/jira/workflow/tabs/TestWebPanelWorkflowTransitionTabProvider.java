package com.atlassian.jira.workflow.tabs;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.matchers.IterableMatchers;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.plugin.webfragment.EmptyWebPanel;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.workflow.MockJiraWorkflow;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebLabel;
import com.atlassian.plugin.web.model.WebParam;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestWebPanelWorkflowTransitionTabProvider
{
    private final MockJiraWorkflow workflow = new MockJiraWorkflow();
    private WorkflowTransitionTabProvider provider;

    @Mock
    private WebInterfaceManager webInterfaceManager;

    @Mock
    JiraAuthenticationContext jiraAuthenticationContext;

    private final ActionDescriptor actionDescriptor = DescriptorFactory.getFactory().createActionDescriptor();

    @Before
    public void setUp() throws Exception
    {
        new MockComponentWorker().init().addMock(JiraAuthenticationContext.class, jiraAuthenticationContext);
        this.provider = new WebPanelWorkflowTransitionTabProvider(webInterfaceManager);
        when(jiraAuthenticationContext.getLoggedInUser()).thenReturn(new MockUser("joe"));
    }

    @Test
    public void testGetTabsThrowsLinkageError() throws Exception
    {
        // having
        when(webInterfaceManager.getDisplayableWebPanelDescriptors(anyString(), anyMapOf(String.class, Object.class)))
                .thenThrow(new LinkageError("Should be handled by WorkflowTransitionTabProvider"));

        // when
        final Iterable<WorkflowTransitionTabProvider.WorkflowTransitionTab> tabs = provider.getTabs(actionDescriptor, workflow);

        // then
        assertThat(tabs, Matchers.<WorkflowTransitionTabProvider.WorkflowTransitionTab>emptyIterable());
    }

    @Test
    public void testGetTabsThrowsExceptionWhenRenderingLabel() throws Exception
    {
        // having
        WebPanelModuleDescriptor badDescriptor = mock(WebPanelModuleDescriptor.class);
        when(badDescriptor.getWebLabel()).thenThrow(new RuntimeException());
        WebPanelModuleDescriptor goodDescriptor = mockDescriptor("good");
        when(webInterfaceManager.getDisplayableWebPanelDescriptors(anyString(), anyMapOf(String.class, Object.class))).thenReturn(ImmutableList.of(badDescriptor, goodDescriptor));

        // when
        final Iterable<WorkflowTransitionTabProvider.WorkflowTransitionTab> tabs = provider.getTabs(actionDescriptor, workflow);

        // then
        assertThat(tabs, IterableMatchers.iterableWithSize(1, WorkflowTransitionTabProvider.WorkflowTransitionTab.class));
        assertThat(Iterables.getOnlyElement(tabs).getLabel(), equalTo("good"));
    }

    @Test
    public void testContextWithNullsPassed() throws Exception
    {
        // having
        final WebPanelModuleDescriptor nullContext = mockDescriptor("nullContext");
        when(webInterfaceManager.getDisplayableWebPanelDescriptors(anyString(), anyMapOf(String.class, Object.class)))
                .thenReturn(ImmutableList.of(nullContext));

        // when
        final Iterable<WorkflowTransitionTabProvider.WorkflowTransitionTab> tabs = provider.getTabs(null, null);

        // then
        assertThat(tabs, IterableMatchers.iterableWithSize(1, WorkflowTransitionTabProvider.WorkflowTransitionTab.class));
    }

    @Test
    public void testGetTabContent() throws Exception
    {
        // having
        final String key = "foo";
        final String tabContent = "html";
        final WebPanelModuleDescriptor descriptor = mockDescriptor(key);

        when(webInterfaceManager.getDisplayableWebPanelDescriptors(anyString(), anyMapOf(String.class, Object.class)))
                .thenReturn(ImmutableList.of(descriptor));
        when(descriptor.getModule()).thenReturn(new EmptyWebPanel() {
            @Override
            public String getHtml(final Map<String, Object> context)
            {
                return tabContent;
            }
        });

        // when
        final String resultContent = provider.getTabContentHtml(key, actionDescriptor, workflow);

        // then
        assertThat(resultContent, equalTo(tabContent));
    }

    @Test
    public void testGetTabGetModuleThrows() throws Exception
    {
        // having
        final String key = "baz";
        final WebPanelModuleDescriptor descriptor = mockDescriptor(key);

        when(webInterfaceManager.getDisplayableWebPanelDescriptors(anyString(), anyMapOf(String.class, Object.class)))
                .thenReturn(ImmutableList.of(descriptor));
        when(descriptor.getModule())
                .thenThrow(new NoClassDefFoundError("Should be handled by WorkflowTransitionTabProvider"));

        // when
        final String resultContent = provider.getTabContentHtml(key, actionDescriptor, workflow);

        // then
        assertThat(resultContent, nullValue());
    }

    @Test
    public void testGetTabRenderingThrows() throws Exception
    {
        // having
        final String key = "zzz";
        final WebPanelModuleDescriptor descriptor = mockDescriptor(key);

        when(webInterfaceManager.getDisplayableWebPanelDescriptors(anyString(), anyMapOf(String.class, Object.class)))
                .thenReturn(ImmutableList.of(descriptor));
        when(descriptor.getModule()).thenReturn(new EmptyWebPanel() {
            @Override
            public String getHtml(final Map<String, Object> context)
            {
                throw new RuntimeException("Bad renderer");
            }
        });

        // when
        final String resultContent = provider.getTabContentHtml(key, actionDescriptor, workflow);

        // then
        assertThat(resultContent, nullValue());
    }

    @Test
    public void testGetTabEnumeratingModulesThrows() throws Exception
    {
        // having
        when(webInterfaceManager.getDisplayableWebPanelDescriptors(anyString(), anyMapOf(String.class, Object.class)))
                .thenThrow(new LinkageError("Should be handled by WorkflowTransitionTabProvider"));

        // when
        final String resultContent = provider.getTabContentHtml("", actionDescriptor, workflow);

        // then
        assertThat(resultContent, nullValue());
    }

    private WebPanelModuleDescriptor mockDescriptor(String key)
    {
        WebPanelModuleDescriptor desc = mock(WebPanelModuleDescriptor.class);
        final WebLabel webLabel = mock(WebLabel.class);
        when(desc.getWebLabel()).thenReturn(webLabel);
        when(webLabel.getDisplayableLabel(any(HttpServletRequest.class), anyMapOf(String.class, Object.class))).thenReturn(key);
        final WebParam webParam = mock(WebParam.class);
        when(desc.getWebParams()).thenReturn(webParam);
        when(desc.getKey()).thenReturn(key);
        return desc;
    }
}
