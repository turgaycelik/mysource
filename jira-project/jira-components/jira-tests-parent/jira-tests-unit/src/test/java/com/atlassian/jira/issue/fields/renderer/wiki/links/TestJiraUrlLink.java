package com.atlassian.jira.issue.fields.renderer.wiki.links;

import com.atlassian.renderer.links.GenericLinkParser;
import com.atlassian.renderer.links.UrlLink;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link UrlLink}.
 *
 * @since v3.13
 */
public class TestJiraUrlLink
{
    /**
     * JRA-15812: Make sure that the URL entered by the user is correctly escaped.
     */
    @Test
    public void testEscape()
    {
        final UrlLink link = new UrlLink(createParser("brenden|http://www.atlassian.com/\" onclick=\"alert('hello world')"));
        assertEquals("http://www.atlassian.com/&quot; onclick=&quot;alert(&#39;hello world&#39;)", link.getUrl());

        //make sure that the URL is not double escaped (at least for this simple case).
        final UrlLink link2 = new UrlLink(createParser(link.getUrl()));
        assertEquals("http://www.atlassian.com/&quot; onclick=&quot;alert(&#39;hello world&#39;)", link2.getUrl());
    }

    private static GenericLinkParser createParser(final String parser)
    {
        return new GenericLinkParser(parser);
    }
}
