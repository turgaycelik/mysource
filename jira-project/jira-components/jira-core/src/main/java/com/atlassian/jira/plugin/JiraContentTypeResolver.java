package com.atlassian.jira.plugin;

import com.atlassian.plugin.servlet.ContentTypeResolver;
import com.atlassian.renderer.util.FileTypeUtil;

/**
 * @since v4.0
 */
public class JiraContentTypeResolver implements ContentTypeResolver
{
    public String getContentType(final String requestUrl)
    {
        return FileTypeUtil.getContentType(requestUrl);
    }
}
