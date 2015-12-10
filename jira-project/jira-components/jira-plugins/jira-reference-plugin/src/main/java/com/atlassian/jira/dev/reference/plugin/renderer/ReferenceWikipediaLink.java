package com.atlassian.jira.dev.reference.plugin.renderer;

import com.atlassian.jira.util.I18nHelper;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.links.BaseLink;
import com.atlassian.renderer.links.GenericLinkParser;
import com.opensymphony.util.TextUtils;

/**
 * Represents a link to a Wikipedia article.
 *
 * @since 4.4
 */
public class ReferenceWikipediaLink extends BaseLink
{
    private static final String WIKIPEDIA_LINK_FORMAT = "http://en.wikipedia.org/wiki/%s";
    private static final String LINK_TITLE_KEY = "module.link.title";

    public ReferenceWikipediaLink(GenericLinkParser parser, RenderContext context, I18nHelper i18nHelper)
    {
        super(parser);

        if (TextUtils.stringSet(parser.getNotLinkBody()) && parser.getNotLinkBody().startsWith("="))
        {
            url = String.format(WIKIPEDIA_LINK_FORMAT, extractArticleTitle(parser));
            linkBody = i18nHelper.getText(LINK_TITLE_KEY, parser.getNotLinkBody().substring(1));
            title = linkBody;
        }
    }

    private String extractArticleTitle(GenericLinkParser parser)
    {
        return parser.getNotLinkBody().substring(1).replaceAll("\\s", "_");
    }

}
