package com.atlassian.sal.jira.message;

import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.sal.api.message.DefaultHelpPath;
import com.atlassian.sal.api.message.HelpPath;
import com.atlassian.sal.api.message.HelpPathResolver;

/**
 * JIRA Help path resolver
 *
 * @since v4.3
 */
public class JiraHelpPathResolver implements HelpPathResolver
{
    /**
     * {@inheritDoc}
     */
    public HelpPath getHelpPath(String key)
    {
        if (key == null)
        {
            return null;
        }
        HelpUtil helpUtil = HelpUtil.getInstance();
        HelpUtil.HelpPath path = helpUtil.getHelpPath(key);

        boolean local = path.isLocal() != null ? path.isLocal() : false;
        return new DefaultHelpPath(path.getKey(), path.getUrl(), path.getTitle(), path.getAlt(), local);
    }
}

