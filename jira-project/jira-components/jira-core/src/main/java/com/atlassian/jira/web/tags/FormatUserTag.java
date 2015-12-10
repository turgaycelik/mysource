package com.atlassian.jira.web.tags;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.userformat.UserFormats;
import com.atlassian.jira.plugin.userformat.UserFormatter;
import com.atlassian.jira.user.ApplicationUser;
import org.apache.log4j.Logger;
import webwork.view.taglib.WebWorkBodyTagSupport;

import javax.servlet.jsp.JspException;
import java.util.Collections;
import java.util.Map;

/**
 * Formats a user given the username (or user object, a type and id) using {@link UserFormats}.
 *
 * @since v3.13
 */
public class FormatUserTag extends WebWorkBodyTagSupport
{
    private static final Logger log = Logger.getLogger(FormatUserTag.class);

    private String user;
    private String userKey;
    private String userName;

    private String type;
    private String id;

    private Boolean escape;

    public void setUser(String user)
    {
        this.user = user;
    }

    public void setUserKey(String userKey)
    {
        this.userKey = userKey;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setEscape(Boolean escape)
    {
        this.escape = escape;
    }

    private void checkUserArgs()
    {
        int count = 0;
        if (user != null)
        {
            ++count;
        }
        if (userKey != null)
        {
            ++count;
        }
        if (userName != null)
        {
            ++count;
        }
        if (count != 1)
        {
            throw new IllegalArgumentException("Exactly one 'user', 'userKey', or 'userName' attribute is required, but " +
                    count + " of them were provided.");
        }
    }

    private UserFormatter formatter()
    {
        return ComponentAccessor.getComponentOfType(UserFormats.class).formatter(findString(type));
    }

    private Map<String, Object> params()
    {
        return Collections.<String, Object>singletonMap("escape", escape);
    }

    private String formatUsername(String userName)
    {
        final String idVal = findString(id);
        return formatter().formatUsername(userName, idVal, params());
    }

    private String formatUserkey(String userKey)
    {
        final String idVal = findString(id);
        return formatter().formatUserkey(userKey, idVal, params());
    }

    public int doEndTag() throws JspException
    {
        String lookup = null;
        boolean isKey = false;

        try
        {
            checkUserArgs();

            if (userKey != null)
            {
                lookup = findString(userKey);
                isKey = true;
            }
            else if (userName != null)
            {
                lookup = findString(userName);
            }
            else
            {
                final Object value = findValue(user);
                if (value instanceof String)
                {
                    // Passed as string.  This used to mean the username, but now it is confusing and ambiguous,
                    // so they need to be explicit about which one they are providing for us to know what to do.
                    lookup = (String)value;
                    throw new IllegalArgumentException("Ambiguous 'user' argument.  Use 'userName' or 'userKey' as appropriate, instead.");
                }
                else if (value instanceof User)
                {
                    lookup = ((User)value).getName();
                }
                else if (value instanceof ApplicationUser)
                {
                    lookup = ((ApplicationUser)value).getKey();
                    isKey = true;
                }
                else
                {
                    lookup = user;
                }
            }

            final String formattedUser = isKey ? formatUserkey(lookup) : formatUsername(lookup);
            if (formattedUser != null)
            {
                pageContext.getOut().write(formattedUser);
            }
        }
        catch (Exception e)
        {
            log.error("Unexpected error occurred formatting user '" + lookup + "'", e);
            throw new JspException(e);
        }
        return EVAL_PAGE;
    }

}
