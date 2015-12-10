package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import junit.framework.Assert;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URL;

@WebTest ({ Category.FUNC_TEST, Category.HTTP })
public class TestUserAgent extends FuncTestCase
{
    // JRA-19910
    // This test is for when a http request with no user agent is sent (for example badly written bots)
    // We are doing the http request manually because httpunit does not allow us to remove the User-Agent header
    // WARNING that if this test fails the page dump is NOT the page that was actually loaded because we are
    // working outside httpunit.
    public void testUserAgentMissing() throws Exception
    {
        administration.restoreBlankInstance();

        URL baseURL = getEnvironmentData().getBaseUrl();

        Socket testSocket = new Socket(baseURL.getHost(), baseURL.getPort());
        OutputStream outputStream = null;
        OutputStreamWriter writer = null;
        InputStream inputStream = null;
        InputStreamReader streamReader = null;

        try
        {
            outputStream = testSocket.getOutputStream();
            writer = new OutputStreamWriter(outputStream, "UTF-8");
            writer.write("GET "+baseURL.getPath() +"/secure/Dashboard.jspa HTTP/1.0\r\n\r\n");
            writer.flush();

            inputStream = testSocket.getInputStream();
            streamReader = new InputStreamReader(inputStream, "UTF-8");

            final String response = IOUtils.toString(streamReader);
            Assert.assertFalse("Should not have found the 500 page.", response.contains("NullPointerException"));
        }
        finally
        {
            if (writer != null)
            {
                IOUtils.closeQuietly(writer);
            }
            else
            {
                IOUtils.closeQuietly(outputStream);
            }

            if (streamReader != null)
            {
                IOUtils.closeQuietly(streamReader);
            }
            else
            {
                IOUtils.closeQuietly(inputStream);
            }

            try
            {
                testSocket.close();
            }
            catch (IOException e)
            {
                // Ignored
            }
        }

    }
}