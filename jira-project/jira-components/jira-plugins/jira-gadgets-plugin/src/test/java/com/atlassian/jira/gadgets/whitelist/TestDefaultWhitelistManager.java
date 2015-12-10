package com.atlassian.jira.gadgets.whitelist;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.gadgets.event.AddGadgetEvent;
import com.atlassian.plugins.whitelist.ImmutableWhitelistRule;
import com.atlassian.plugins.whitelist.OutboundWhitelist;
import com.atlassian.plugins.whitelist.WhitelistRule;
import com.atlassian.plugins.whitelist.WhitelistService;
import com.atlassian.plugins.whitelist.WhitelistType;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestDefaultWhitelistManager
{
    @Mock private EventPublisher mockEventPublisher;
    @Mock private WhitelistService whitelistService;
    @Mock private OutboundWhitelist whitelist;
    @Captor private ArgumentCaptor<WhitelistRule> argument;

    @Test
    public void testGetRules()
    {
        givenWhitelistEnabled();
        givenSomeWhitelistRules();

        final List<String> rules = createManager().getRules();
        assertThat(rules, containsInAnyOrder("http://www.atlassian.com/*", "http://www.google.com", "=http://www.twitter.com"));
    }

    @Test
    public void testGetRulesDisabled()
    {
        givenWhitelistDisabled();

        assertTrue("Expected list to be empty.", createManager().getRules().isEmpty());
    }

    @Test
    public void testUpdateRules()
    {
        givenWhitelistEnabled();
        givenSomeWhitelistRules();

        final List<String> rules = createManager().updateRules(Arrays.asList("http://www.atlassian.com/*", "http://www.google.com", "=http://www.twitter.com"), false);
        assertThat(rules, containsInAnyOrder("http://www.atlassian.com/*", "http://www.google.com", "=http://www.twitter.com"));
    }

    @Test
    public void testAddGadgetEvent()
    {
        givenWhitelistEnabled();
        givenSomeWhitelistRules();
        when(whitelist.isAllowed(Matchers.<URI>any())).thenReturn(false);

        createManager().onAddGadget(new AddGadgetEvent(URI.create("http://extranet.atlassian.com/gadgets/jira/somegadget.xml")));

        verify(whitelistService).add(argument.capture());
        assertThat(argument.getValue().getExpression(), is("http://extranet.atlassian.com/*"));
        assertThat(argument.getValue().getType(), is(WhitelistType.WILDCARD_EXPRESSION));
    }

    @Test
    public void updateIsNotRemovingApplicationLinks()
    {
        givenWhitelistEnabled();
        givenWhitelistRuleOfTypeApplicationLink();

        createManager().updateRules(Lists.newArrayList("http://www.atlassian.com/*"), false);

        verify(whitelistService, never()).remove(anyInt());
    }

    private void givenWhitelistEnabled()
    {
        when(whitelistService.isWhitelistEnabled()).thenReturn(true);
    }

    private void givenWhitelistDisabled()
    {
        when(whitelistService.isWhitelistEnabled()).thenReturn(false);
    }

    private void givenSomeWhitelistRules()
    {
        doReturn(Arrays.asList(
                ImmutableWhitelistRule.builder().id(1).expression("http://www.atlassian.com/*").type(WhitelistType.WILDCARD_EXPRESSION).build(),
                ImmutableWhitelistRule.builder().id(2).expression("http://www.google.com").type(WhitelistType.WILDCARD_EXPRESSION).build(),
                ImmutableWhitelistRule.builder().id(3).expression("http://www.twitter.com").type(WhitelistType.EXACT_URL).build()
        )).when(whitelistService).getAll();
    }

    private void givenWhitelistRuleOfTypeApplicationLink()
    {
        doReturn(Collections.singleton(
                ImmutableWhitelistRule.builder().id(1).expression("some-application-link-id").type(WhitelistType.APPLICATION_LINK).build()
        )).when(whitelistService).getAll();
    }

    private DefaultWhitelistManager createManager()
    {
        return new DefaultWhitelistManager(mockEventPublisher, whitelistService, whitelist);
    }
}
