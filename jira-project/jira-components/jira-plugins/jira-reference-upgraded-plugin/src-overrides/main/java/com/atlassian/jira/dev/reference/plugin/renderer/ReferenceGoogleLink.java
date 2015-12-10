package com.atlassian.jira.dev.reference.plugin.renderer;

import com.atlassian.jira.util.I18nHelper;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.links.BaseLink;
import com.atlassian.renderer.links.GenericLinkParser;
import com.opensymphony.util.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Represents a link to a user's profile.
 */
public class ReferenceGoogleLink extends BaseLink
{
    private static final String GOOGLE_LINK_FORMAT = "http://www.google.com.au/search?q=%s";
    private static final String LINK_TITLE_KEY = "module.link.title";

    public ReferenceGoogleLink(GenericLinkParser parser, RenderContext context, I18nHelper i18nHelper)
    {
        super(parser);

        if (TextUtils.stringSet(parser.getNotLinkBody()) && parser.getNotLinkBody().startsWith("="))
        {
            url = String.format(GOOGLE_LINK_FORMAT, extractQuery(parser));
            linkBody = i18nHelper.getText(LINK_TITLE_KEY, parser.getNotLinkBody().substring(1));
            title = linkBody;
        }
    }

    private String extractQuery(GenericLinkParser parser)
    {
        try
        {
            return URLEncoder.encode(parser.getNotLinkBody().substring(1), "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

}
