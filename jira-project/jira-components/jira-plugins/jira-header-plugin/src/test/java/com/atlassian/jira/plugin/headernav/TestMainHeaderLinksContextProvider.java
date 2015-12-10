package com.atlassian.jira.plugin.headernav;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.MockSimpleLink;
import com.atlassian.jira.plugin.webfragment.model.MockSimpleLinkSection;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.collect.ImmutableList;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestMainHeaderLinksContextProvider
{
    private static final String LOCATION = MainHeaderLinksContextProvider.SYSTEM_TOP_NAVIGATION_BAR;
    private static final String CONTEXT_TOPLEVEL_ITEMS_KEY = MainHeaderLinksContextProvider.TOPLEVEL_ITEMS_KEY;
    private static final String CONTEXT_MORE_ITEMS_KEY = MainHeaderLinksContextProvider.MORE_ITEMS_KEY;
    private static final String CONTEXT_LAZY_HEADER_LINKS_KEY = MainHeaderLinksContextProvider.LAZY_HEADER_LINKS_KEY;
    private static final String CONTEXT_DROPDOWN_SECTIONS_KEY = MainHeaderLinksContextProvider.DROPDOWN_SECTIONS_KEY;
    private static final String CONTEXT_DROPDOWN_LINKS_KEY = MainHeaderLinksContextProvider.DROPDOWN_LINKS_KEY;

    private final SimpleLinkManager simpleLinkManagerMock = mock(SimpleLinkManager.class);
    private final JiraAuthenticationContext authenticationContextMock = mock(JiraAuthenticationContext.class);
    private final User userMock = mock(User.class);
    private final JiraHelper helper = new JiraHelper();

    private final SimpleLink nonLazyLinkMock = mock(SimpleLink.class, "nonLazyLink");
    private final SimpleLinkSection subSectionMock = mock(SimpleLinkSection.class);
    private final SimpleLink lazyLinkMock = mock(SimpleLink.class, "lazyLink");
    private final SimpleLink linkMock = mock(SimpleLink.class);

    private final MainHeaderLinksContextProvider provider = new MainHeaderLinksContextProvider(simpleLinkManagerMock, authenticationContextMock);

    @Rule
    public final JiraAuthenticationContextRule authenticationContextRule = new JiraAuthenticationContextRule(authenticationContextMock, userMock);

    @Before
    public void setUp()
    {
        when(nonLazyLinkMock.getId()).thenReturn("non-lazy-main-link");
        when(subSectionMock.getId()).thenReturn("sub-section");
        when(linkMock.getId()).thenReturn("sub-section-link");

        when(lazyLinkMock.getId()).thenReturn("lazy-main-link");

        when(simpleLinkManagerMock.shouldSectionBeLazy(nonLazyLinkMock.getId())).thenReturn(Boolean.FALSE);
        when(simpleLinkManagerMock.getSectionsForLocation(nonLazyLinkMock.getId(), userMock, helper)).thenReturn(Collections.singletonList(subSectionMock));
        when(simpleLinkManagerMock.getLinksForSection(createCompoundKey(nonLazyLinkMock, subSectionMock), userMock, helper)).thenReturn(Collections.singletonList(linkMock));
        
        when(simpleLinkManagerMock.shouldLocationBeLazy(lazyLinkMock.getId(), userMock, helper)).thenReturn(Boolean.TRUE);
        provider.init(new HashMap<String, String>());
    }

    @AnonymousUser
    @Test
    public void testAnonymous()
    {
        final Map<String, Object> result = provider.getContextMap(createContext());
        assertNotNull(result);
        assertThat(result.get(CONTEXT_TOPLEVEL_ITEMS_KEY), is(ContextProviderMatcher.emptyCollection()));
        assertThat(result.get(CONTEXT_MORE_ITEMS_KEY), is(ContextProviderMatcher.emptyCollection()));
        assertThat(result.get(CONTEXT_LAZY_HEADER_LINKS_KEY), is(ContextProviderMatcher.emptyCollection()));
        assertThat(result.get(CONTEXT_DROPDOWN_SECTIONS_KEY), is(ContextProviderMatcher.emptyMap()));
        assertThat(result.get(CONTEXT_DROPDOWN_LINKS_KEY), is(ContextProviderMatcher.emptyMap()));
    }

    @AuthenticatedUser
    @Test
    public void testOnlyLazyToplevelLinks()
    {
        givenToplevelItems("lazy-main-link");
        givenSomeSections(lazyLinkMock);
        final Map<String, Object> result = provider.getContextMap(createContext());
        assertNotNull(result);
        assertThat(result.get(CONTEXT_TOPLEVEL_ITEMS_KEY), is(ContextProviderMatcher.aListWithValues(lazyLinkMock)));
        assertThat(result.get(CONTEXT_MORE_ITEMS_KEY), is(ContextProviderMatcher.emptyCollection()));
        assertThat(result.get(CONTEXT_LAZY_HEADER_LINKS_KEY), is(ContextProviderMatcher.aListWithValues(lazyLinkMock.getId())));
        assertThat(result.get(CONTEXT_DROPDOWN_SECTIONS_KEY), is(ContextProviderMatcher.emptyMap()));
        assertThat(result.get(CONTEXT_DROPDOWN_LINKS_KEY), is(ContextProviderMatcher.emptyMap()));
    }

    @AuthenticatedUser
    @Test
    public void testOnlyNonLazyToplevelLinks()
    {
        givenToplevelItems("non-lazy-main-link");
        givenSomeSections(nonLazyLinkMock);
        final Map<String, Object> result = provider.getContextMap(createContext());
        assertNotNull(result);
        assertThat(result.get(CONTEXT_TOPLEVEL_ITEMS_KEY), is(ContextProviderMatcher.aListWithValues(nonLazyLinkMock)));
        assertThat(result.get(CONTEXT_MORE_ITEMS_KEY), is(ContextProviderMatcher.emptyCollection()));
        assertThat(result.get(CONTEXT_LAZY_HEADER_LINKS_KEY), is(ContextProviderMatcher.emptyCollection()));
        assertThat(result.get(CONTEXT_DROPDOWN_SECTIONS_KEY), is(ContextProviderMatcher.aMapWithEntrySet(nonLazyLinkMock.getId(), Collections.singletonList(subSectionMock))));
        assertThat(result.get(CONTEXT_DROPDOWN_LINKS_KEY), is(ContextProviderMatcher.aMapWithEntrySet(createCompoundKey(nonLazyLinkMock, subSectionMock), Collections.singletonList(linkMock))));
    }

    @AuthenticatedUser
    @Test
    public void testBothSortsOfToplevelLinks()
    {
        givenToplevelItems("lazy-main-link", "non-lazy-main-link");
        givenSomeSections(nonLazyLinkMock, lazyLinkMock);
        final Map<String, Object> result = provider.getContextMap(createContext());
        assertNotNull(result);
        assertThat(result.get(CONTEXT_TOPLEVEL_ITEMS_KEY), is(ContextProviderMatcher.aListWithValues(nonLazyLinkMock, lazyLinkMock)));
        assertThat(result.get(CONTEXT_MORE_ITEMS_KEY), is(ContextProviderMatcher.emptyCollection()));
        assertThat(result.get(CONTEXT_LAZY_HEADER_LINKS_KEY), is(ContextProviderMatcher.aListWithValues(lazyLinkMock.getId())));
        assertThat(result.get(CONTEXT_DROPDOWN_SECTIONS_KEY), is(ContextProviderMatcher.aMapWithEntrySet(nonLazyLinkMock.getId(), Collections.singletonList(subSectionMock))));
        assertThat(result.get(CONTEXT_DROPDOWN_LINKS_KEY), is(ContextProviderMatcher.aMapWithEntrySet(createCompoundKey(nonLazyLinkMock, subSectionMock), Collections.singletonList(linkMock))));
    }

    /**
     *
     * https://jira.atlassian.com/browse/JRA-36859
     *
     */
    @AuthenticatedUser
    @Test
    public void testDuplicateIdInvalidBehaviour()
    {

        final String duplicatedId = "duplicated-id";
        final String anotherId = "another-id";

        //2 links have exact the same id
        MockSimpleLink duplicatedLink1 = new MockSimpleLink(duplicatedId);
        MockSimpleLink duplicatedLink2 = new MockSimpleLink(duplicatedId);
        MockSimpleLink anotherLink = new MockSimpleLink(anotherId);
        givenSomeSections(duplicatedLink1, anotherLink, duplicatedLink2);

        //and we assume that for given id there are some sections
        //but unfortunately they are indexed by id so there is no way to distinguish those two links
        final MockSimpleLinkSection section1 = new MockSimpleLinkSection("dupl-sect1");
        final MockSimpleLinkSection section2 = new MockSimpleLinkSection("dupl-sect2");
        when(simpleLinkManagerMock.getSectionsForLocation(duplicatedId, userMock, helper)).thenReturn(ImmutableList.<SimpleLinkSection>of(section1, section2));

        final Map<String, Object> result = provider.getContextMap(createContext());
        result.get(CONTEXT_TOPLEVEL_ITEMS_KEY);

        //top level list contains all 3 items
        assertThat((List<SimpleLink>) result.get(CONTEXT_TOPLEVEL_ITEMS_KEY), Matchers.<SimpleLink>containsInAnyOrder(duplicatedLink1, duplicatedLink2, anotherLink));

        //... but the sections map contains only two elements
        final Map<String, List> dropdownSections = (Map<String, List>) result.get(CONTEXT_DROPDOWN_SECTIONS_KEY);
        assertEquals(2, dropdownSections.size());
        assertThat(dropdownSections, Matchers.<String, List>hasEntry(anotherId, Collections.emptyList()));
        assertThat(dropdownSections, Matchers.<String, List>hasEntry(duplicatedId, ImmutableList.of(section1, section2)));
    }

    private String createCompoundKey(@Nonnull final SimpleLink firstKey, @Nonnull final SimpleLinkSection secondKey)
    {
        return firstKey.getId() + "/" + secondKey.getId();
    }
    
    private void givenSomeSections(@Nonnull final SimpleLink... sections)
    {
        when(simpleLinkManagerMock.getLinksForSection(LOCATION, userMock, helper)).thenReturn(Arrays.asList(sections));
    }

    private void givenToplevelItems(@Nonnull String... items)
    {
        Map<String, String> params = new HashMap<String, String>();
        for (String item : items)
        {
            params.put("toplevel-item-" + item, item);
        }
        provider.init(params);
    }

    private Map<String, Object> createContext()
    {
        return MapBuilder.<String, Object>build("helper", helper);
    }
}
