package com.atlassian.jira.admin;

import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.web.api.DynamicWebInterfaceManager;
import com.atlassian.plugin.web.api.WebItem;
import com.atlassian.plugin.web.api.WebSection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(ListeningMockitoRunner.class)
public class TestLegacyAdminLinkFactory
{
    private static final String SYSTEM_ADMIN_LOCATION = "system.admin";

    @Mock private DynamicWebInterfaceManager dynamicWebInterfaceManager;
    @Mock private WebItem displayableItem;
    @Mock private WebSection displayableSection;
    @Mock private WebItem undisplayableItem;
    @Mock private WebSection undisplayableSection;

    private LegacyAdminLinkFactory legacyAdminLinkFactory;

    @Before
    public void setUp()
    {
        when(displayableSection.getId()).thenReturn("SAMPLE_ID");
        when(undisplayableSection.getId()).thenReturn("SAMPLE_ID");

        when(dynamicWebInterfaceManager.getDisplayableWebSections(eq(SYSTEM_ADMIN_LOCATION), anyMap())).thenReturn(newArrayList(displayableSection));
        when(dynamicWebInterfaceManager.getWebSections(eq(SYSTEM_ADMIN_LOCATION), anyMap())).thenReturn(newArrayList(displayableSection, undisplayableSection));

        when(dynamicWebInterfaceManager.getDisplayableWebItems(eq(SYSTEM_ADMIN_LOCATION + "/SAMPLE_ID"), anyMap())).thenReturn(newArrayList(displayableItem));
        when(dynamicWebInterfaceManager.getWebItems(eq(SYSTEM_ADMIN_LOCATION + "/SAMPLE_ID"), anyMap())).thenReturn(newArrayList(displayableItem, undisplayableItem));

        legacyAdminLinkFactory = new LegacyAdminLinkFactory(dynamicWebInterfaceManager);
    }

    @Test
    public void getItemsOnlyReturnsDisplayableLinksAndSections()
    {
        Iterable<WebItem> adminLinks = legacyAdminLinkFactory.getItems(MapBuilder.<String, Object>emptyMap());

        assertThat(newArrayList(adminLinks), hasSize(1));
        assertThat(adminLinks, hasItem(displayableItem));
        assertThat(adminLinks, not(hasItem(undisplayableItem)));
    }
}
