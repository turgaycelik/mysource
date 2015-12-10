package com.atlassian.jira.plugin.issuetabpanel;

import java.util.List;
import java.util.Locale;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NoopI18nFactory;
import com.atlassian.jira.util.NoopI18nHelper;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class IssueTabPanelInvokerImplTest
{
    private MockUser admin;
    private I18nHelper.BeanFactory i18nFactory;
    private JiraAuthenticationContext jiraAuthenticationContext;
    private Issue issue;
    private ShowPanelRequest showPanelRequest;
    private GetActionsRequest getActionsRequest;

    @Before
    public void setUp() throws Exception
    {
        admin = new MockUser("admin");
        i18nFactory = new NoopI18nFactory();
        jiraAuthenticationContext = new MockSimpleAuthenticationContext(admin, new Locale("pt"));
        issue = new MockIssue(1);
        showPanelRequest = new ShowPanelRequest(issue, admin);
        getActionsRequest = new GetActionsRequest(issue, admin, false, false, null);
    }

    @Test
    public void showPanelShouldHandleBothOldAndNewStyleIssueTabPanels() throws Exception
    {
        IssueTabPanel oldStylePanel = mock(IssueTabPanel.class);
        invoker().invokeShowPanel(showPanelRequest, descriptorFor(oldStylePanel));
        verify(oldStylePanel).showPanel(issue, admin);
        verifyNoMoreInteractions(oldStylePanel);

        IssueTabPanel2 newStylePanel = mock(IssueTabPanel2.class);
        invoker().invokeShowPanel(showPanelRequest, descriptorFor(newStylePanel));
        verify(newStylePanel).showPanel(showPanelRequest);
        verifyNoMoreInteractions(newStylePanel);
    }

    @Test
    public void getActionsShouldHandleBothOldAndNewStyleIssueTabPanels() throws Exception
    {
        IssueTabPanel oldStylePanel = mock(IssueTabPanel.class);
        invoker().invokeGetActions(getActionsRequest, descriptorFor(oldStylePanel));
        verify(oldStylePanel).getActions(issue, admin);
        verifyNoMoreInteractions(oldStylePanel);

        IssueTabPanel2 newStylePanel = mock(IssueTabPanel2.class);
        invoker().invokeGetActions(getActionsRequest, descriptorFor(newStylePanel));
        verify(newStylePanel).getActions(getActionsRequest);
        verifyNoMoreInteractions(newStylePanel);
    }

    @Test
    public void showPanelShouldReturnTrueWhenPanelThrowsAnException() throws Exception
    {
        boolean reply = invoker().invokeShowPanel(new ShowPanelRequest(issue, admin), descriptorFor(new BrokenIssueTabPanel()));
        assertThat(reply, equalTo(true));

        boolean reply2 = invoker().invokeShowPanel(new ShowPanelRequest(issue, admin), descriptorFor(new BrokenIssueTabPanel2()));
        assertThat(reply2, equalTo(true));
    }

    @Test
    public void getActionsShouldRenderAMessageWhenPanelThrowsAnException() throws Exception
    {
        List<IssueAction> issueActions = invoker().invokeGetActions(getActionsRequest, descriptorFor(new BrokenIssueTabPanel()));
        assertThat(issueActions.get(0).getHtml(), equalTo(NoopI18nHelper.makeTranslation("modulewebcomponent.exception", "completeKey-" + BrokenIssueTabPanel.class.getName())));

        List<IssueAction> issueActions2 = invoker().invokeGetActions(getActionsRequest, descriptorFor(new BrokenIssueTabPanel2()));
        assertThat(issueActions2.get(0).getHtml(), equalTo(NoopI18nHelper.makeTranslation("modulewebcomponent.exception", "completeKey-" + BrokenIssueTabPanel2.class.getName())));
    }

    @Test
    public void showPanelShouldReturnTrueWhenDescriptorThrowsAnException() throws Exception
    {
        IssueTabPanelModuleDescriptor descriptor = mock(IssueTabPanelModuleDescriptor.class);
        when(descriptor.getCompleteKey()).thenReturn("keyOfBadDescriptor");
        when(descriptor.getModule()).thenThrow(new RuntimeException("bet you didn't see this coming..."));

        boolean reply = invoker().invokeShowPanel(showPanelRequest, descriptor);
        assertThat(reply, equalTo(true));
    }

    @Test
    public void getActionsShouldRenderAMessageWhenDescriptorThrowsAnException() throws Exception
    {
        IssueTabPanelModuleDescriptor descriptor = mock(IssueTabPanelModuleDescriptor.class);
        when(descriptor.getCompleteKey()).thenReturn("keyOfBadDescriptor");
        when(descriptor.getModule()).thenThrow(new RuntimeException("bet you didn't see this coming..."));

        List<IssueAction> issueActions = invoker().invokeGetActions(getActionsRequest, descriptor);
        assertThat(issueActions.get(0).getHtml(), equalTo(NoopI18nHelper.makeTranslation("modulewebcomponent.exception", "keyOfBadDescriptor")));
    }

    private IssueTabPanelInvokerImpl invoker()
    {
        return new IssueTabPanelInvokerImpl(i18nFactory, jiraAuthenticationContext);
    }

    private IssueTabPanelModuleDescriptor descriptorFor(IssueTabPanel tabPanel)
    {
        IssueTabPanelModuleDescriptor descriptor = mock(IssueTabPanelModuleDescriptor.class);
        when(descriptor.getModule()).thenReturn(IssueTabPanel3Adaptor.createFrom(tabPanel));
        when(descriptor.getKey()).thenReturn("key-" + tabPanel.getClass().getName());
        when(descriptor.getCompleteKey()).thenReturn("completeKey-" + tabPanel.getClass().getName());

        return descriptor;
    }

    static class BrokenIssueTabPanel extends AbstractIssueTabPanel
    {
        @Override
        public List<IssueAction> getActions(Issue issue, User remoteUser)
        {
            throw new RuntimeException();
        }

        @Override
        public boolean showPanel(Issue issue, User remoteUser)
        {
            throw new RuntimeException();
        }
    }

    static class BrokenIssueTabPanel2 extends AbstractIssueTabPanel2
    {
        @Override
        public ShowPanelReply showPanel(ShowPanelRequest request)
        {
            throw new RuntimeException();
        }

        @Override
        public GetActionsReply getActions(GetActionsRequest request)
        {
            throw new RuntimeException();
        }
    }
}
