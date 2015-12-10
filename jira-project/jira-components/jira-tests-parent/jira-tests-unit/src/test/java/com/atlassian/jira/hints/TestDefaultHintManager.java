package com.atlassian.jira.hints;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.plugin.web.api.DynamicWebInterfaceManager;
import com.atlassian.plugin.web.api.WebItem;
import com.atlassian.plugin.web.api.model.WebFragmentBuilder;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Test case for {@link DefaultHintManager}.
 *
 * @since v4.2
 */
@RunWith (ListeningMockitoRunner.class)
public class TestDefaultHintManager
{
    private DefaultHintManager hintManager;

    @Mock
    private DynamicWebInterfaceManager webInterfaceManager;

    private JiraHelper jiraHelper;

    @Before
    public void setUp() throws Exception
    {
        jiraHelper = new JiraHelper();
        hintManager = new DefaultHintManager(webInterfaceManager);
    }

    @Test
    public void testGetAll()
    {
        initWebInterfaceManager("one", "two", "three");

        final List<Hint> result = hintManager.getAllHints(null, jiraHelper);
        assertEquals(Arrays.asList("one", "two", "three"), toStringList(result));
    }

    @Test
    public void testGetRandom()
    {
        initWebInterfaceManager("one", "two", "three", "four", "five");

        final Hint result = hintManager.getRandomHint(null, jiraHelper);
        assertTrue(Arrays.asList("one", "two", "three", "four", "five").contains(result.getText()));
    }

    @Test
    public void testGetWithNullWebItems()
    {
        initWebInterfaceManager("one", "two", null, "four", "five");

        final List<Hint> result = hintManager.getAllHints(null, jiraHelper);
        assertEquals(Arrays.asList("one", "two", "", "four", "five"), toStringList(result));
    }

    @Test
    public void testGetWithNoWebItems()
    {
        final List<Hint> result = hintManager.getAllHints(null, jiraHelper);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testHintsWithTooltips()
    {
        when(webInterfaceManager.getDisplayableWebItems(eq("jira.hints/all"), anyMap())).thenReturn(Lists.newArrayList(
                new WebFragmentBuilder("notnull", 0).label("one").title("tooltipone").webItem("").url("").build()
        ));
        final Hint result = hintManager.getRandomHint(null, jiraHelper);
        assertEquals("one", result.getText());
        assertEquals("tooltipone", result.getTooltip());
    }

    private void initWebInterfaceManager(String... hints)
    {
        final List<WebItem> webItems = Lists.newArrayList();
        for (String hint : hints)
        {
            webItems.add(new WebFragmentBuilder("notnull", 0).label(hint).title(hint == null ? "stuff" : hint).webItem("").url("").build());
        }
        when(webInterfaceManager.getDisplayableWebItems(eq("jira.hints/all"), anyMap())).thenReturn(webItems);
    }

    private List<String> toStringList(List<Hint> hints)
    {
        return CollectionUtil.transform(hints, new Function<Hint, String>()
        {
            public String get(final Hint input)
            {
                return input.getText();
            }
        });
    }
}
