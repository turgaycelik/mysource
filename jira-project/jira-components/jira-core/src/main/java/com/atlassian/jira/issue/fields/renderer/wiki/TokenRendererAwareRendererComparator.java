package com.atlassian.jira.issue.fields.renderer.wiki;

import com.atlassian.renderer.v2.components.RendererComponent;
import com.atlassian.renderer.v2.components.TokenRendererComponent;

import java.util.Comparator;

/**
 * Comparator that orders RendererComponents so that {@see TokenRendererComponent} instances are always at the end
 */
public class TokenRendererAwareRendererComparator implements Comparator<RendererComponent>
{
    public static final TokenRendererAwareRendererComparator COMPARATOR = new TokenRendererAwareRendererComparator();

    public int compare(final RendererComponent renderer1, final RendererComponent renderer2)
    {
        if (renderer1 instanceof TokenRendererComponent)
        {
            return renderer2 instanceof TokenRendererComponent ? 0 : 1;
        }
        else if (renderer2 instanceof TokenRendererComponent)
        {
            return -1;
        }
        return 0;
    }
}
