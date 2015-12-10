package com.atlassian.jira.issue.fields.renderer.wiki.links;

import com.atlassian.jira.util.JiraKeyUtils;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.links.Link;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.components.AbstractRendererComponent;
import com.atlassian.renderer.v2.components.link.LinkDecorator;
import com.opensymphony.util.TextUtils;
import org.apache.oro.text.perl.Perl5Util;
import org.apache.oro.text.regex.MatchResult;

/**
 * This plugs into the wiki renderer framework to turn references to Jira issue keys into
 * links to the actual issue.
 */
public class JiraIssueLinkRendererComponent extends AbstractRendererComponent
{
    public boolean shouldRender(final RenderMode renderMode)
    {
        return renderMode.renderLinks();
    }

    public String render(final String wiki, final RenderContext context)
    {
        return linkBugKeys(wiki, context);
    }

    // This is not so nice, it is almost a complete replication of the JiraKeyUtils.linkBugKeys
    // code, but when we find the match we need to treat it differently and we require the
    // RenderContext to process it here.
    private String linkBugKeys(String body, final RenderContext context)
    {
        if (!TextUtils.stringSet(body))
        {
            return "";
        }

        final Perl5Util util = new Perl5Util(); //note that we have to create a new match, as MatchResult is not Threadsafe

        final StringBuilder buff = new StringBuilder(body.length());
        final String issueKeyRegex = JiraKeyUtils.getIssueKeyRegex(); // this can be expensive, cache it locally
        while (util.match(issueKeyRegex, body))
        {
            final MatchResult match = util.getMatch();

            buff.append(body.substring(0, match.beginOffset(2)));

            // The issue key is composed of the all sub-pattern match
            // groups after the initial sub pattern match group
            final StringBuilder sb = new StringBuilder();
            final int matchGroups = match.groups();
            for (int i = 2; i < matchGroups; i++)
            {
                sb.append(match.group(i));
            }
            final String key = sb.toString();

            // Check backs from the key to see if it part of the url
            if (JiraKeyUtils.isPartOfUrl(body, match.beginOffset(2)))
            {
                buff.append(key);
            }
            else
            {
                try
                {
                    final Link link = new JiraIssueLink(key);
                    buff.append(context.getRenderedContentStore().addInline(new LinkDecorator(link)));
                }
                catch (final Exception iae)
                {
                    buff.append(key);
                }
            }

            body = body.substring(match.endOffset(matchGroups - 1));
        }

        // append any remaining body (or the whole thing if no matches occurred)
        buff.append(body);
        return buff.toString();
    }
}
