package com.atlassian.jira.issue.fields.renderer.wiki.links;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.util.URLCodec;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.links.BaseLink;
import com.atlassian.renderer.links.GenericLinkParser;
import com.opensymphony.util.TextUtils;

import java.io.UnsupportedEncodingException;

/**
 * Represents a link to a user's profile.
 */
public class JiraUserProfileLink extends BaseLink
{
    private User user;

    public JiraUserProfileLink(GenericLinkParser parser, RenderContext context)
    {
        super(parser);

        if (TextUtils.stringSet(parser.getNotLinkBody()) && parser.getNotLinkBody().startsWith("~"))
        {
            try
            {
                String username = parser.getNotLinkBody().substring(1);
                this.user = UserUtils.getUser(username);

                if (user != null && user.getDisplayName() != null)
                { this.linkBody = user.getDisplayName(); }
                else
                { this.linkBody = username; }

                this.url = buildProfileUrl(context, username);
            }
            catch (Exception e)
            {
                // do nothing, can't find the user that's fine.
            }
        }
    }

    @Override
    public String getLinkAttributes()
    {
        if (user != null)
        {
            return super.getLinkAttributes() + " class=\"user-hover\" rel=\"" + TextUtils.htmlEncode(user.getName()) + "\"";
        }
        return super.getLinkAttributes();
    }

    private String buildProfileUrl(RenderContext context, String username) throws UnsupportedEncodingException
    {
        String encodedUsername = URLCodec.encode(username, context.getCharacterEncoding());
        return context.getBaseUrl() + "/secure/ViewProfile.jspa?name=" + encodedUsername;
    }

    public User getUser()
    {
        return user;
    }
}
