package com.atlassian.jira.plugin.headernav;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.api.DynamicWebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUserProfileContextProvider
{
    private static final String LOCATION = UserProfileContextProvider.SYSTEM_USER_OPTIONS;
    private static final String CONTEXT_SECTION_KEYS = UserProfileContextProvider.SECTIONS_KEY;
    private static final String CONTEXT_SECTION_ITEMS_KEY = UserProfileContextProvider.SECTION_ITEMS_KEY;

    private final JiraAuthenticationContext authenticationContextMock = mock(JiraAuthenticationContext.class);
    private final User userMock = mock(User.class);
    private final DynamicWebInterfaceManager webInterfaceManagerMock = mock(DynamicWebInterfaceManager.class);
    private final JiraHelper helper = new JiraHelper();

    private final WebSectionModuleDescriptor webSectionMock = mock(WebSectionModuleDescriptor.class);
    private final WebSectionModuleDescriptor helpWebSectionMock = mock(WebSectionModuleDescriptor.class);
    private final WebItemModuleDescriptor webItemMock = mock(WebItemModuleDescriptor.class);

    private final UserProfileContextProvider provider = new UserProfileContextProvider(authenticationContextMock);
    
    @Rule
    public JiraAuthenticationContextRule authenticationContextRule = new JiraAuthenticationContextRule(authenticationContextMock, userMock);

    @Before
    public void setUp()
    {
        when(webSectionMock.getCompleteKey()).thenReturn("complete.section.key");
        when(webSectionMock.getKey()).thenReturn("section.key");
        when(helpWebSectionMock.getKey()).thenReturn(UserProfileContextProvider.HELP_WEB_SECTION_KEY);
    }
    
    @AnonymousUser
    @Test
    public void testAnonymousUser()
    {
        final Map<String, Object> result = provider.getContextMap(createContext());
        assertNotNull(result);
        assertThat(result.get(CONTEXT_SECTION_KEYS), is(ContextProviderMatcher.emptyCollection()));
        assertThat(result.get(CONTEXT_SECTION_ITEMS_KEY), is(ContextProviderMatcher.emptyMap()));
    }

    @AuthenticatedUser
    @Test
    public void testAuthenticatedUser()
    {
        givenWebSections(webSectionMock, helpWebSectionMock);
        givenOneItemOfTheSection();
        final Map<String, Object> result = provider.getContextMap(createContext());
        assertNotNull(result);
        assertThat(result.get(CONTEXT_SECTION_KEYS), is(ContextProviderMatcher.aListWithValues(webSectionMock)));
        assertThat(result.get(CONTEXT_SECTION_ITEMS_KEY), is(ContextProviderMatcher.aMapWithEntrySet(webSectionMock.getCompleteKey(), Collections.singletonList(webItemMock))));
    }

    private void givenOneItemOfTheSection()
    {
        when(webInterfaceManagerMock.getDisplayableItems(eq(LOCATION + "/" + webSectionMock.getKey()), anyMap())).thenReturn(Collections.singletonList(webItemMock));
    }

    private void givenWebSections(@Nonnull final WebSectionModuleDescriptor... sections)
    {
        when(webInterfaceManagerMock.getDisplayableSections(eq(LOCATION), anyMap())).thenReturn(Arrays.asList(sections));
    }

    private Map<String, Object> createContext()
    {
        return MapBuilder.<String, Object>build("helper", helper, "webInterfaceManager", new JiraWebInterfaceManager(webInterfaceManagerMock));
    }
}
