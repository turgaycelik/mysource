package com.atlassian.jira.scheme;

import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.web.action.RedirectSanitiser;
import com.atlassian.jira.web.action.admin.permission.CopyScheme;

import com.mockobjects.servlet.MockHttpServletResponse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestCopySchemeAction
{
    @Rule
    public final RuleChain mockitoMocks = MockitoMocksInContainer.forTest(this);

    @Mock
    @AvailableInContainer
    private PermissionSchemeManager schemeManager;

    @AvailableInContainer
    private RedirectSanitiser redirectSanitiser = new MockRedirectSanitiser();

    @Mock
    private Scheme scheme;

    @Test
    public void testCopyPermissionScheme() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewPermissionSchemes.jspa");
        when(schemeManager.getSchemeObject(1L)).thenReturn(scheme);

        CopyScheme copyScheme = new CopyScheme();
        copyScheme.setSchemeId(1L);

        copyScheme.execute();
        verify(schemeManager).copyScheme(scheme);

        response.verify();
    }
}
