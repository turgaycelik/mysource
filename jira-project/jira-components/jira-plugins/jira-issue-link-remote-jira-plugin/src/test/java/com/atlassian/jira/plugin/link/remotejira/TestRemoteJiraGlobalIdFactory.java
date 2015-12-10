package com.atlassian.jira.plugin.link.remotejira;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link RemoteJiraGlobalIdFactoryImpl}.
 *
 * @since v5.0
 */
public class TestRemoteJiraGlobalIdFactory
{
    private final ApplicationLink applicationLink = mock(ApplicationLink.class);
    private final ApplicationId applicationId = mock(ApplicationId.class);
    private final ApplicationLinkService applicationLinkService = mock(ApplicationLinkService.class);
    private final RemoteJiraGlobalIdFactoryImpl remoteJiraGlobalIdFactory = new RemoteJiraGlobalIdFactoryImpl(applicationLinkService);

    @Before
    public void setUp()
    {
        when(applicationLinkService.getApplicationLinks(JiraApplicationType.class)).thenReturn(Arrays.asList(applicationLink));
        when(applicationLink.getId()).thenReturn(applicationId);
        when(applicationId.get()).thenReturn("8835b6b9-5676-3de4-ad59-bbe987416662");
    }

    @Test
    public void testSuccess()
    {
        final RemoteJiraGlobalId globalId = new RemoteJiraGlobalId(applicationLink, 10000L);
        final String encoded = RemoteJiraGlobalIdFactoryImpl.encode(globalId);

        final RemoteJiraGlobalId decoded = remoteJiraGlobalIdFactory.decode(encoded);
        assertEquals(globalId.getApplicationLink().getId().get(), decoded.getApplicationLink().getId().get());
        assertEquals(globalId.getRemoteIssueId(), decoded.getRemoteIssueId());
    }
}
