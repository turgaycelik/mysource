package com.atlassian.jira.issue.fields.renderer.wiki;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.components.RendererComponent;
import com.atlassian.renderer.v2.components.TokenRendererComponent;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests TokenRendererAwareRendererComparator
 *
 * @since v5.0
 */
public class TestTokenRendererAwareRendererComparator
{
    @Test
    public void testTokenRendererAwareRendererComparator()
    {
        TokenRendererAwareRendererComparator comp = TokenRendererAwareRendererComparator.COMPARATOR;

        RendererComponent renderer = new MockRendererComponent();
        RendererComponent tokenRenderer = new TokenRendererComponent(null);

        assertTrue(comp.compare(renderer, renderer) == 0);
        assertTrue(comp.compare(renderer, tokenRenderer) < 0);
        assertTrue(comp.compare(tokenRenderer, renderer) > 0);
        assertTrue(comp.compare(tokenRenderer, tokenRenderer) == 0);
    }

    private static class MockRendererComponent implements RendererComponent
    {
        @Override
        public boolean shouldRender(RenderMode renderMode)
        {
            return false;
        }

        @Override
        public String render(String wiki, RenderContext context)
        {
            return null;
        }
    }
}
