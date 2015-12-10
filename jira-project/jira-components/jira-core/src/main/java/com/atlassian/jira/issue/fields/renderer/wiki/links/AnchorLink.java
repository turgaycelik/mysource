package com.atlassian.jira.issue.fields.renderer.wiki.links;

import com.atlassian.renderer.links.BaseLink;
import com.atlassian.renderer.links.GenericLinkParser;
import com.atlassian.renderer.v2.components.HtmlEscaper;

/** Creates a simple anchor link. */
public class AnchorLink extends BaseLink
{
    public AnchorLink(GenericLinkParser parser)
    {
        super(parser);

        if (("#" + parser.getAnchor()).equals(linkBody))
        {
            linkBody = parser.getAnchor();
        }

        //JRA-15812: Return a link that escapes the URL before it is returned.
        this.url = "#" + HtmlEscaper.escapeAll(parser.getAnchor(), true);
        this.relativeUrl = false;
    }
}
