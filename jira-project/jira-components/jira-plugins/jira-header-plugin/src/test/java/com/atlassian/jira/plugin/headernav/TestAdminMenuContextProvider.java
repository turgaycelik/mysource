package com.atlassian.jira.plugin.headernav;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static com.atlassian.jira.plugin.headernav.AdminMenuContextProvider.CONTEXT_SECTIONS_KEY;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestAdminMenuContextProvider
{
    private final SimpleLinkManager simpleLinkManagerMock = mock(SimpleLinkManager.class);
    private final JiraAuthenticationContext authenticationContextMock = mock(JiraAuthenticationContext.class);
    private final User userMock = mock(User.class);
    private final JiraHelper helper = new JiraHelper();

    private final SimpleLinkSection sectionMock = mock(SimpleLinkSection.class);
    private final SimpleLinkSection subSectionMock = mock(SimpleLinkSection.class);
    private final SimpleLink sectionsLinkMock = mock(SimpleLink.class);
    private final SimpleLink linkMock = mock(SimpleLink.class);
    private final SimpleLink adminSummaryMock = mock(SimpleLink.class);

    private final AdminMenuContextProvider provider = new AdminMenuContextProvider(simpleLinkManagerMock, authenticationContextMock);

    @Rule
    public final JiraAuthenticationContextRule authenticationContextRule = new JiraAuthenticationContextRule(authenticationContextMock, userMock);

    @Before
    public void setUp()
    {
        when(sectionMock.getId()).thenReturn("root-section");
        when(subSectionMock.getId()).thenReturn("sub-section");
        when(linkMock.getId()).thenReturn("link");
        when(adminSummaryMock.getId()).thenReturn("view_projects");
    }

    @AnonymousUser
    @Test
    public void testNoLinksVisibleAsAnonymousUser()
    {
        final Map<String,Object> result = whenContextProviderIsInvoked();
        assertNotNull(result);
        assertThat(result.get(CONTEXT_SECTIONS_KEY), is(ContextProviderMatcher.emptyMap()));
    }

    @AuthenticatedUser
    @Test
    public void testAuthenticatedUser()
    {
        givenOneNonEmptyAdminSection();
        givenOneAdminLinkForTheFirstAdminSection();
        givenOneAdditionalAdminLink();
        final Map<String, Object> result = whenContextProviderIsInvoked();
        assertNotNull(result);
        assertThat((Map<? super SimpleLinkSection, ?>) result.get(CONTEXT_SECTIONS_KEY), hasKey(sectionMock));
    }

    private void givenOneNonEmptyAdminSection()
    {
        when(simpleLinkManagerMock.getNotEmptySectionsForLocation(AdminMenuContextProvider.ADMIN_TOP_NAVIGATION_BAR_LOCATION, userMock, helper)).thenReturn(Collections.singletonList(sectionMock));
    }

    private void givenOneAdminLinkForTheFirstAdminSection()
    {
        when(simpleLinkManagerMock.getNotEmptySectionsForLocation(sectionMock.getId(), userMock, helper)).thenReturn(Collections.singletonList(subSectionMock));
        when(simpleLinkManagerMock.getLinksForSection(subSectionMock.getId(), userMock, helper)).thenReturn(Collections.singletonList(sectionsLinkMock));
    }

    private void givenOneAdditionalAdminLink()
    {
        when(simpleLinkManagerMock.getLinksForSection(AdminMenuContextProvider.ADMIN_TOP_NAVIGATION_BAR_LOCATION, userMock, helper)).thenReturn(Arrays.asList(adminSummaryMock, linkMock));
    }

    private Map<String, Object> whenContextProviderIsInvoked()
    {
        return provider.getContextMap(Collections.<String, Object>singletonMap("helper", helper));
    }
}
