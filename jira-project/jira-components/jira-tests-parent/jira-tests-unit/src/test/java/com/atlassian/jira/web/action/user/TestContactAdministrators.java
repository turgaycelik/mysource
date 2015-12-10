package com.atlassian.jira.web.action.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraApplicationContext;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mail.CssInliner;
import com.atlassian.jira.mail.TemplateContext;
import com.atlassian.jira.mail.TemplateContextFactory;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.TemplateSource;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.MockUserLocaleStore;
import com.atlassian.jira.user.UserLocaleStore;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.JiraContactHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.web.action.RedirectSanitiser;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.MailQueueItem;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Locale;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestContactAdministrators
{
    @Mock MailQueue mailQueue;
    @Mock JiraContactHelper jiraContactHelper;
    @Mock UserUtil userUtil;
    @Mock TemplateContextFactory templateContextFactory;
    @Mock(answer = Answers.RETURNS_MOCKS) UserPropertyManager userPropertyManager;
    @Mock VelocityTemplatingEngine velocityTemplatingEngine;
    @Mock ApplicationProperties applicationProperties;
    @Mock VelocityTemplatingEngine.RenderRequest renderRequest;
    @Mock CssInliner cssInliner;

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);

        ComponentAccessor.initialiseWorker(new MockComponentWorker()
                        .addMock(TemplateContextFactory.class, templateContextFactory)
                        .addMock(CssInliner.class, cssInliner)
                        .addMock(VelocityTemplatingEngine.class, velocityTemplatingEngine)
                        .addMock(JiraAuthenticationContext.class, mock(JiraAuthenticationContext.class, RETURNS_MOCKS))
                        .addMock(UserPropertyManager.class, userPropertyManager)
                        .addMock(UserLocaleStore.class, new MockUserLocaleStore(Locale.ENGLISH))
                        .addMock(ApplicationProperties.class, applicationProperties)
                        .addMock(JiraApplicationContext.class, mock(JiraApplicationContext.class, RETURNS_MOCKS))
                        .addMock(RedirectSanitiser.class, new MockRedirectSanitiser())
        );
    }

    @After
    public void after()
    {
        ComponentAccessor.initialiseWorker(null);
    }


    @Test
    public void checkSendingEmail() throws Exception {

        when(jiraContactHelper.isAdministratorContactFormEnabled()).thenReturn(true);
        User admin = mock(User.class);
        when(userUtil.getJiraAdministrators()).thenReturn(Lists.newArrayList(admin));
        when(admin.getEmailAddress()).thenReturn("pniewiadomski@atlassian.com");

        TemplateContext templateContext = mock(TemplateContext.class);
        when(templateContextFactory.getTemplateContext(any(Locale.class))).thenReturn(templateContext);

        Map<String, Object> context = MapBuilder.<String, Object>newBuilder().add("testing", "yes").toMutableMap();
        when(templateContext.getTemplateParams()).thenReturn(context);

        //use a mock servlet response
        JiraTestUtil.setupExpectedRedirect("/secure/MyJiraHome.jspa");

        when(velocityTemplatingEngine.render(any(TemplateSource.class))).thenReturn(renderRequest);
        ArgumentCaptor<Map> argumentCaptor = ArgumentCaptor.forClass(Map.class);
        when(renderRequest.applying(argumentCaptor.capture())).thenReturn(renderRequest);

        ContactAdministrators contact = new ContactAdministrators(null, mailQueue, userUtil, userPropertyManager, jiraContactHelper);
        contact.setDetails("");
        contact.setSubject("SomeSubject");
        contact.doExecute();

        verify(mailQueue, times(1)).addItem(any(MailQueueItem.class));
    }

}
