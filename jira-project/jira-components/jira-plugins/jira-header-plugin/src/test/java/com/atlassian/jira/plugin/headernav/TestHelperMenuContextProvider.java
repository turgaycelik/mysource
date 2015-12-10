package com.atlassian.jira.plugin.headernav;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.MapBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestHelperMenuContextProvider
{
    private static final String LOCATION = HelpMenuContextProvider.SYSTEM_USER_OPTIONS;
    private static final String HELP_SECTION_ID = HelpMenuContextProvider.HELP_SECTION_ID;
    private static final String CONTEXT_SECTIONS_KEY = HelpMenuContextProvider.SECTIONS_KEY;
    private static final String CONTEXT_SECTION_LINKS_KEY = HelpMenuContextProvider.SECTION_LINKS_KEY;
    private static final String CONTEXT_HELP_LINK_KEY = HelpMenuContextProvider.HELP_LINK_KEY;

    private final SimpleLinkManager simpleLinkManagerMock = mock(SimpleLinkManager.class);
    private final JiraAuthenticationContext authenticationContextMock = mock(JiraAuthenticationContext.class);
    private final User userMock = mock(User.class);
    private final JiraHelper helper = new JiraHelper();

    private final SimpleLinkSection helpSectionMock = mock(SimpleLinkSection.class);
    private final SimpleLinkSection anotherSectionMock = mock(SimpleLinkSection.class);
    private final SimpleLink linkMock = mock(SimpleLink.class);

    @Rule
    public final JiraAuthenticationContextRule authenticationContextRule = new JiraAuthenticationContextRule(authenticationContextMock, userMock);

    private final HelpMenuContextProvider provider = new HelpMenuContextProvider(simpleLinkManagerMock, authenticationContextMock);

    @Before
    public void setUp()
    {
        when(helpSectionMock.getId()).thenReturn(HELP_SECTION_ID);
        when(anotherSectionMock.getId()).thenReturn("another-section");
    }
    
    @AnonymousUser
    @Test
    public void testAnonymousUser()
    {

        final Map<String, Object> result = provider.getContextMap(createContext());
        assertNotNull(result);
        assertNull(result.get("user"));
        assertThat(result.get(CONTEXT_SECTIONS_KEY), is(ContextProviderMatcher.emptyCollection()));
        assertThat(result.get(CONTEXT_SECTION_LINKS_KEY), is(ContextProviderMatcher.emptyMap()));
        assertNull(result.get(CONTEXT_HELP_LINK_KEY));
    }

    @AuthenticatedUser
    @Test
    public void testAuthenticatedUser()
    {
        givenTwoMenuSectionsForLocation();
        givenOneHelpMenuEntryForTheHelpMenuSection();
        final Map<String, Object> result = provider.getContextMap(createContext());
        assertNotNull(result);
        assertThat(result.get("user"), is((Object) userMock));
        assertThat(result.get(CONTEXT_SECTIONS_KEY), is(ContextProviderMatcher.aListWithValues(helpSectionMock)));
        assertThat(result.get(CONTEXT_SECTION_LINKS_KEY), is(ContextProviderMatcher.aMapWithEntrySet(helpSectionMock.getId(), Collections.singletonList(linkMock))));
        assertThat(result.get(CONTEXT_HELP_LINK_KEY), is((Object) linkMock));
    }

    private void givenTwoMenuSectionsForLocation()
    {
        when(simpleLinkManagerMock.getNotEmptySectionsForLocation(LOCATION, userMock, helper)).thenReturn(Arrays.asList(anotherSectionMock, helpSectionMock));
    }

    private void givenOneHelpMenuEntryForTheHelpMenuSection()
    {
        when(simpleLinkManagerMock.getLinksForSection(LOCATION + "/" + helpSectionMock.getId(), userMock, helper)).thenReturn(Collections.singletonList(linkMock));
    }

    private Map<String, Object> createContext()
    {
        return MapBuilder.<String, Object>build("helper", helper);
    }
        
}
