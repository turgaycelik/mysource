package com.atlassian.sal.jira.websudo;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.websudo.InternalWebSudoManager;
import com.atlassian.oauth.util.RequestAnnotations;
import junit.framework.TestCase;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpSession;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TestJIRASalWebSudoManager extends TestCase
{
    private JIRASalWebSudoManager webSudoManager;

    @Mock
    private InternalWebSudoManager internalWebSudoManager;

    @Mock
    private ApplicationProperties applicationProperties;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        initMocks(this);
        when(internalWebSudoManager.isEnabled()).thenReturn(true);
        when(internalWebSudoManager.hasValidSession(any(HttpSession.class))).thenReturn(false);
        webSudoManager = new JIRASalWebSudoManager(applicationProperties, internalWebSudoManager);
    }

    public void testWebSudoIsBypassedForOAuthAuthenticatedRequest()
    {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        RequestAnnotations.markAsOAuthRequest(mockRequest);
        assertTrue(webSudoManager.canExecuteRequest(mockRequest));
    }

    public void testWebSudoIsNotBypassedForNonOAuthRequest()
    {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        assertFalse(webSudoManager.canExecuteRequest(mockRequest));
    }
}
