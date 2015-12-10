package com.atlassian.jira.plugin.headernav;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestCreateMenuContextProvider
{
    private final static String CONTEXT_KEY = CreateMenuContextProvider.FIRST_CREATE_MENU_LINK_KEY;
    
    private final SimpleLinkManager simpleLinkManagerMock = mock(SimpleLinkManager.class);
    private final JiraAuthenticationContext authenticationContextMock = mock(JiraAuthenticationContext.class);
    private final CreateMenuContextProvider provider = new CreateMenuContextProvider(simpleLinkManagerMock, authenticationContextMock);
    private final User user = mock(User.class);
    private final JiraHelper helper = new JiraHelper();
    private final SimpleLink createLinkMock = mock(SimpleLink.class);

    @Rule
    public final JiraAuthenticationContextRule authenticationContextRule = new JiraAuthenticationContextRule(authenticationContextMock, user);

    @AnonymousUser
    @Test
    public void testAnonymousUser()
    {
        givenNoVisibleCreateMenuLinksForAnonymous();
        final Map<String, Object> result = provider.getContextMap(null, helper);
        thenContextContainsNull(result);
    }

    @AuthenticatedUser
    @Test
    public void testWithoutProject()
    {
        givenNoCreateMenuLinksVisible();
        final Map<String, Object> result = provider.getContextMap(null, helper);
        thenContextContainsNull(result);
    }

    @AuthenticatedUser
    @Test
    public void testWithProject()
    {
        givenOneCreateMenuLinkVisible();
        final Map<String, Object> result = provider.getContextMap(null, helper);
        thenContextContainsOneLink(result);
    }

    @AuthenticatedUser
    @Test
    public void testMultipleEntries()
    {
        givenMultipleCreateMenuLinksVisible();
        final Map<String, Object> result = provider.getContextMap(null, helper);
        thenContextContainsOneLink(result);
    }

    private void givenNoVisibleCreateMenuLinksForAnonymous()
    {
        when(simpleLinkManagerMock.getLinksForSection(anyString(), eq((User) null), eq(helper))).thenReturn(Collections.<SimpleLink>emptyList());
    }

    private void givenNoCreateMenuLinksVisible()
    {
        when(simpleLinkManagerMock.getLinksForSection(anyString(), eq(user), eq(helper))).thenReturn(Collections.<SimpleLink>emptyList());
    }

    private void givenOneCreateMenuLinkVisible()
    {
        when(simpleLinkManagerMock.getLinksForSection(anyString(), eq(user), eq(helper))).thenReturn(Collections.<SimpleLink>singletonList(createLinkMock));
    }

    private void givenMultipleCreateMenuLinksVisible()
    {
        final SimpleLink secondMock = mock(SimpleLink.class, "secondMock");
        final SimpleLink thirdMock = mock(SimpleLink.class, "thirdMock");
        final SimpleLink fourthMock = mock(SimpleLink.class, "fourthMock");
        when(simpleLinkManagerMock.getLinksForSection(anyString(), eq(user), eq(helper))).thenReturn(Arrays.asList(createLinkMock, secondMock, thirdMock, fourthMock));
    }

    private void thenContextContainsNull(final Map<String, Object> result)
    {
        assertNotNull(result);
        assertThat(result, hasEntry(CONTEXT_KEY, null));
    }

    private void thenContextContainsOneLink(final Map<String, Object> result)
    {
        assertNotNull(result);
        assertThat(result, hasEntry(CONTEXT_KEY, (Object) createLinkMock));
    }
}
