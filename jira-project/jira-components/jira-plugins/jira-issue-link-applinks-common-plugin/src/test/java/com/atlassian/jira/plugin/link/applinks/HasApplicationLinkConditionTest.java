package com.atlassian.jira.plugin.link.applinks;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.plugin.PluginParseException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @since v5.0
 */
@RunWith (MockitoJUnitRunner.class)
public class HasApplicationLinkConditionTest
{
    @Mock
    private ApplicationLinkService applicationLinkService;

    @Mock
    private ApplicationLink applicationLink;

    private HasApplicationLinkCondition condition;

    @Before
    public void setUp() throws Exception
    {
        condition = new HasApplicationLinkCondition(applicationLinkService);
    }

    @Test
    public void testInit() throws Exception
    {
        condition.init(ImmutableMap.of("applicationType", JiraApplicationType.class.getName()));
    }

    @Test(expected = PluginParseException.class)
    public void testInitWithoutApplicationTypeParam() throws Exception
    {
        condition.init(ImmutableMap.<String, String>of());
    }

    @Test(expected = PluginParseException.class)
    public void testInitWithNonClassApplicationTypeParam() throws Exception
    {
        condition.init(ImmutableMap.of("applicationType", "NoClass"));
    }

    @Test(expected = PluginParseException.class)
    public void testInitWithNonApplicationTypeApplicationTypeParam() throws Exception
    {
        condition.init(ImmutableMap.of("applicationType", String.class.getName()));
    }

    @Test
    public void testShoudDisplayWhenApplinksAreAvailable() throws Exception
    {
        condition.init(ImmutableMap.of("applicationType", JiraApplicationType.class.getName()));

        when(applicationLinkService.getApplicationLinks(JiraApplicationType.class)).thenReturn(ImmutableList.of(applicationLink));

        assertTrue(condition.shouldDisplay(null));
    }

    @Test
    public void testShoudNotDisplayWhenApplinksAreNotAvailable() throws Exception
    {
        condition.init(ImmutableMap.of("applicationType", JiraApplicationType.class.getName()));

        when(applicationLinkService.getApplicationLinks(JiraApplicationType.class)).thenReturn(ImmutableList.<ApplicationLink>of());

        assertFalse(condition.shouldDisplay(null));
    }
}
