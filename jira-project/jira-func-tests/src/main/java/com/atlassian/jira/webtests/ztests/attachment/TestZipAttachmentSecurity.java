package com.atlassian.jira.webtests.ztests.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.UriBuilder;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.rules.CopyAttachmentsRule;

import com.google.common.io.LineReader;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;

/**
 * Responsible for verifying that the server doesn't shut down if we attach files like:
 *  42.zip, a large compressed file, etc.
 */
@WebTest ({ Category.FUNC_TEST, Category.ATTACHMENTS, Category.SECURITY })
public class TestZipAttachmentSecurity extends FuncTestCase
{

    private final HttpClient client = new HttpClient();

    protected CopyAttachmentsRule copyAttachmentsRule;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestZipAttachmentSecurity/TestZipAttachmentSecurity.xml");

        backdoor.attachments().enable();
        copyAttachmentsRule = new CopyAttachmentsRule(this);
        copyAttachmentsRule.before();
        copyAttachmentsRule.copyAttachmentsFrom("TestZipAttachmentSecurity/attachments");

        client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("admin", "admin"));
        client.getParams().setAuthenticationPreemptive(true);
    }

    @Override
    protected void tearDownTest()
    {
        copyAttachmentsRule.after();
        super.tearDownTest();
    }

    public void testAttachmentUnzippingWithChrome() throws URISyntaxException, IOException
    {
        final GetMethod request = new GetMethod(unzipFileUri().toString());
        request.setRequestHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64)");
        int status = client.executeMethod(request);
        assertEquals(HttpStatus.SC_OK, status);

        final InputStream is = request.getResponseBodyAsStream();
        try {
            final LineReader lineReader = new LineReader(new InputStreamReader(is));
            assertEquals("1: This is a text file with 1000 lines.", lineReader.readLine());
        }
        finally
        {
            IOUtils.closeQuietly(is);
        }
    }

    public void testAttachmentUnzuppingWithOldIE() throws IOException, URISyntaxException
    {
        final GetMethod request = new GetMethod(unzipFileUri().toString());
        request.setRequestHeader("User-Agent", "Mozilla/5.0 (compatible; MSIE 7.01; Windows NT 5.0)");
        int status = client.executeMethod(request);
        assertEquals(HttpStatus.SC_OK, status);

        final InputStream is = request.getResponseBodyAsStream();
        try
        {
            final LineReader lineReader = new LineReader(new InputStreamReader(is));
            assertEquals("1: This is a text file with 1000 lines.", lineReader.readLine());
        }
        finally
        {
            IOUtils.closeQuietly(is);
        }
    }

    private URI unzipFileUri() throws URISyntaxException
    {
        return UriBuilder
                .fromUri(environmentData.getBaseUrl().toURI())
                .path("secure/attachmentzip/unzip/10010/10000%5B0%5D/file.txt")
                .build();
    }

}
