package com.atlassian.jira.charts.jfreechart;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import com.atlassian.core.util.FileUtils;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.io.SessionTempFile;
import com.atlassian.jira.io.TempFileFactory;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.servlet.MockHttpServletRequest;
import com.atlassian.jira.mock.servlet.MockHttpServletResponse;
import com.atlassian.jira.mock.servlet.MockHttpSession;
import com.atlassian.jira.mock.servlet.MockServletOutputStream;

import org.jfree.chart.servlet.ServletUtilities;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DisplayChartServletTest
{
    @Mock TempFileFactory tempFileFactory;
    @Mock SessionTempFile sessionTempFile;
    @Mock ChartUtils chartUtils;
    MockHttpServletRequest request;
    MockHttpServletResponse response;
    File tmpFile;
    private File tempDirectory;

    @Before
    public void setUp() throws Exception
    {
        tempDirectory = createTempDirectory();

        ComponentAccessor.initialiseWorker(new MockComponentWorker()
                .addMock(TempFileFactory.class, tempFileFactory)
                .addMock(ChartUtils.class, chartUtils)
        );

        request = new MockHttpServletRequest(new MockHttpSession());
        response = new MockHttpServletResponse(new MockServletOutputStream(new StringWriter()));
        tmpFile = File.createTempFile(ServletUtilities.getTempOneTimeFilePrefix(), null, tempDirectory);

        request.setParameter("filename", tmpFile.getName());
        when(tempFileFactory.getSessionTempFile(tmpFile.getName())).thenReturn(sessionTempFile);
        when(sessionTempFile.getFile()).thenReturn(tmpFile);
        when(chartUtils.getChartDirectory()).thenReturn(tempDirectory);
    }

    @Test
    public void emptyFilename() throws Exception
    {
        DisplayChartServlet servlet = new DisplayChartServlet();
        request.setParameter("filename", "");
        servlet.service(request, response);
        assertThat(response.getStatus(), is(404));
    }

    @Test
    public void illegalFilename() throws Exception
    {
        DisplayChartServlet servlet = new DisplayChartServlet();
        request.setParameter("filename", "myimage.png");
        servlet.service(request, response);
        assertThat(response.getStatus(), is(404));
    }

    @Test
    public void servletUnbindsSessionTempFileAfterStreamingIt() throws Exception
    {
        DisplayChartServlet servlet = new DisplayChartServlet();
        servlet.service(request, response);
        verify(tempFileFactory).getSessionTempFile(tmpFile.getName());
        verify(sessionTempFile).unbind();
    }

    @Test
    public void servletDoesNotCrashIfTempFileNotFound() throws Exception
    {
        when(tempFileFactory.getSessionTempFile(tmpFile.getName())).thenReturn(null);

        DisplayChartServlet servlet = new DisplayChartServlet();
        servlet.service(request, response);
        verify(tempFileFactory).getSessionTempFile(tmpFile.getName());
    }

    @After
    public void tearDown() throws Exception
    {
        //noinspection ResultOfMethodCallIgnored
        tmpFile.delete();
        FileUtils.deleteDir(tempDirectory);
    }

    private File createTempDirectory() throws IOException
    {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        String name = "Test" + System.currentTimeMillis();
        File dir = new File(baseDir, name);
        dir.mkdir();
        return dir;
    }

}
