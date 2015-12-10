package com.atlassian.jira.web.servlet;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.URLCodec;
import com.atlassian.plugin.servlet.AbstractFileServerServlet;
import com.atlassian.plugin.servlet.DownloadStrategy;
import com.atlassian.renderer.util.FileTypeUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.atlassian.jira.util.collect.CollectionBuilder.newBuilder;

/**
 * This is used to serve up download references in JIRA plugins.
 */
public class FileServerServlet extends AbstractFileServerServlet
{
    //~ Methods --------------------------------------------------------------------------------------------------------

    public String getDecodedPathInfo(final HttpServletRequest httpServletRequest)
    {
        return urlDecode(httpServletRequest.getPathInfo());
    }

    protected String urlDecode(final String url)
    {
        try
        {
            return (url == null) ? null : URLCodec.decode(url, getCharacterEncoding());
        }
        catch (final Exception e)
        {
            return url;
        }
    }

    protected String getContentType(final String location)
    {
        return FileTypeUtil.getContentType(location);
    }

    private String getCharacterEncoding()
    {
        return ComponentAccessor.getApplicationProperties().getEncoding();
    }

    @Override
    protected List<DownloadStrategy> getDownloadStrategies()
    {
        // Do not cache managed Components here, because Servlets live forever, and we would end up holding onto stale objects.
        return newBuilder(ComponentAccessor.getComponentOfType(DownloadStrategy.class)).asList();
    }
}
