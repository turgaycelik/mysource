package com.atlassian.jira.charts.jfreechart;

import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.io.SessionTempFile;
import com.atlassian.jira.io.TempFileFactory;
import com.atlassian.jira.util.PathTraversalException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.servlet.DisplayChart;
import org.jfree.chart.servlet.ServletUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.atlassian.jira.util.PathUtils.ensurePathInSecureDir;

/**
 * Display chart servlet that delegates processing to {@link DisplayChart}, after a few additional checks have been done
 * on the <code>filename</code> request parameter. If there is an attempted path traversal, it is logged and a 404 is
 * returned.
 *
 * @see com.atlassian.jira.util.PathUtils#ensurePathInSecureDir(String, String)
 * @since v4.4.5
 */
public class DisplayChartServlet implements Servlet
{
    /**
     * Logger for DisplayChartServlet.
     */
    public static final Logger log = LoggerFactory.getLogger(DisplayChartServlet.class);

    /**
     * The servlet that we delegat to.
     */
    private final Servlet displayChart = new DisplayChart();

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        displayChart.init(config);
    }

    @Override
    public void destroy()
    {
        displayChart.destroy();
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException
    {
        String filename = request.getParameter("filename");
        final ChartUtils chartUtils = ComponentAccessor.getComponent(ChartUtils.class);
        File tmpDir = chartUtils.getChartDirectory();
        try
        {
            validateFileName(filename);
            // the path that the filename will resolve to in JChart
            final File path = new File(tmpDir, filename);

            ensurePathInSecureDir(tmpDir.getAbsolutePath(), path.getAbsolutePath());
            if (path.exists())
            {
                try
                {
                    ServletUtilities.sendTempFile(path, (HttpServletResponse) response);
                    return;
                }
                finally
                {
                    FileUtils.deleteQuietly(path);
                    if (((HttpServletRequest) request).getSession(false) != null)
                    {
                        unmarkAsTemporaryFile(filename);
                    }
                }
            }

            log.warn("File not found, returning 404 (filename='{}').", filename);
        }
        catch (IllegalArgumentException e)
        {
            log.warn(e.getMessage() + ", returning 404 (filename='{}').", filename);
        }
        catch (PathTraversalException e)
        {
            log.warn("Possible path traversal attempt, returning 404 (filename='{}').", filename);
        }
        catch (IOException e)
        {
            log.error("Error checking path, returning 404 (filename='{}').", filename);
        }

        // fallback: return 404
        ((HttpServletResponse) response).sendError(404);
    }

    private void validateFileName(final String filename)
    {
        if (StringUtils.isEmpty(filename))
        {
            throw new IllegalArgumentException("The file name is of the wrong format");
        }
        if (!filename.startsWith(ServletUtilities.getTempOneTimeFilePrefix()))
        {
            throw new IllegalArgumentException("The file name does not start with '" + ServletUtilities.getTempOneTimeFilePrefix() + "'");
        }
    }

    @Override
    public ServletConfig getServletConfig()
    {
        return displayChart.getServletConfig();
    }

    @Override
    public String getServletInfo()
    {
        return displayChart.getServletInfo();
    }

    /**
     * Loads the SessionTempFile corresponding to {@code filename} and unbinds it from the session if the underlying
     * File has been deleted.
     *
     * @param filename a file within {@code java.io.tmpdir}
     */
    private void unmarkAsTemporaryFile(final String filename)
    {
        TempFileFactory tempFileFactory = ComponentAccessor.getComponent(TempFileFactory.class);
        if (tempFileFactory == null) {
            return;
        }

        SessionTempFile sessionTempFile = tempFileFactory.getSessionTempFile(filename);
        if (sessionTempFile != null && !sessionTempFile.getFile().exists())
        {
            // if the file has been deleted then unbind it
            sessionTempFile.unbind();
        }
    }
}
