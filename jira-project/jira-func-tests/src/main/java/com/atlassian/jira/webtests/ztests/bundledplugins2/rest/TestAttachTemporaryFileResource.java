package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.testkit.client.restclient.Attachment;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.google.common.collect.Sets;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.log4j.Logger;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * This is just a smoke test to make sure all the pieces work together. The unit test for the resource covers all the
 * logic and edge cases.
 *
 * @since v4.4
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestAttachTemporaryFileResource extends FuncTestCase
{
    private static final Logger log = Logger.getLogger(TestAttachTemporaryFileResource.class);

    private static final String REST_PATH = "rest/internal/1.0/AttachTemporaryFile";
    private static final String ATTACH_PATH = "secure/AttachFile.jspa";

    private static final String PARAM_FILENAME = "filename";
    private static final String PARAM_ISSUE_ID = "issueId";
    private static final String PARAM_ID = "id";
    private static final String PARAM_SIZE = "size";
    private static final String PARAM_CONVERT_ID = "filetoconvert";
    private static final String PARAM_DECORATOR = "decorator";
    private static final String PARAM_USERNAME = "os_username";
    private static final String PARAM_PASSWORD = "os_password";
    private static final String PARAM_COOKIE = "os_cookie";
    private static final String PARAM_XSRF = "atl_token";

    private static final String JSON_NAME = "name";
    private static final String JSON_ID = "id";
    private static final String JSON_ERROR_MSG = "errorMessage";

    private static final String HEADER_SERAPH_RESPONSE = "X-Seraph-LoginReason";

    private static final String COOKIE_XSRF = "atlassian.xsrf.token";

    private static final String GOOD_LOGIN = "OK";
    private static final String NONE = "none";

    private static final long ISSUE_ID = 10000L;
    private static final String ISSUE_KEY = "HSP-1";
    public static final String JSON_TOKEN = "token";

    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestAttachTemporaryFileResource.xml");
    }

    public void testNoSizeRequest() throws Exception
    {
        HttpClient client = createClientForUser("admin");

        //No content length or size.
        PostMethod method = new PostMethod(getRestUrlWithToken(client, PARAM_FILENAME, "text.txt", PARAM_ISSUE_ID, 1));
        method.setRequestEntity(new RandomBytesEntity().setContentLength(-1).setActualLength(1));
        try
        {
            client.executeMethod(method);
            assertBadResponse(HttpStatus.SC_BAD_REQUEST, method);
        }
        finally
        {
            method.releaseConnection();
        }
    }

    public void testTooBigRequest() throws Exception
    {
        HttpClient client = createClientForUser("admin");

        PostMethod method = new PostMethod(getRestUrlWithToken(client, PARAM_FILENAME, "test.data", PARAM_ISSUE_ID, ISSUE_ID, PARAM_SIZE, 10));
        method.setRequestEntity(new RandomBytesEntity().setLength(20));

        try
        {
            client.executeMethod(method);
            assertBadResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, method);
        }
        finally
        {
            method.releaseConnection();
        }
    }

    public void testTooSmallRequest() throws Exception
    {
        HttpClient client = createClientForUser("admin");

        PostMethod method = new PostMethod(getRestUrlWithToken(client, PARAM_FILENAME, "test.data", PARAM_ISSUE_ID, ISSUE_ID, PARAM_SIZE, 40));
        method.setRequestEntity(new RandomBytesEntity().setLength(20));

        try
        {
            client.executeMethod(method);
            assertBadResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, method);
        }
        finally
        {
            method.releaseConnection();
        }
    }

    public void testGoodRequest() throws Exception
    {
        HttpClient client = createClientForUser("admin");

        String expectedFile = "test.data";
        PostMethod method = new PostMethod(getRestUrlWithToken(client, PARAM_FILENAME, expectedFile, PARAM_ISSUE_ID, ISSUE_ID));
        RandomBytesEntity entity = new RandomBytesEntity().setLength(150);
        method.setRequestEntity(entity);

        String id;
        try
        {
            int response = client.executeMethod(method);
            assertEquals(HttpStatus.SC_CREATED, response);
            JSONObject jsonObject = new JSONObject(method.getResponseBodyAsString());

            HashSet<String> actualKeys = Sets.newHashSet(jsonObject.keys());
            assertEquals(Sets.newHashSet(JSON_NAME, JSON_ID), actualKeys);
            assertEquals(expectedFile, jsonObject.getString(JSON_NAME));

            id = jsonObject.getString(JSON_ID);
        }
        finally
        {
            method.releaseConnection();
        }

        //Actually do the attachment. This is a bit of a back but so be it.
        method = new PostMethod(getUrl(ATTACH_PATH));
        method.setParameter(PARAM_CONVERT_ID, String.valueOf(id));
        method.setParameter(PARAM_ID, String.valueOf(ISSUE_ID));
        method.setParameter(PARAM_DECORATOR, NONE);
        method.setParameter(PARAM_XSRF, getToken(client));

        try
        {
            int response = client.executeMethod(method);
            if (response != HttpStatus.SC_MOVED_TEMPORARILY)
            {
                fail(String.format("Trying to add attachment but got status %s: %s.", HttpStatus.getStatusText(response),
                        method.getResponseBodyAsString()));
            }
        }
        finally
        {
            method.releaseConnection();
        }

        //Which attachment did we just create.
        IssueClient issueClient = new IssueClient(getEnvironmentData());
        Issue issue = issueClient.get(ISSUE_KEY);
        List<Attachment> attachments = issue.fields.attachment;
        assertEquals(1, attachments.size());
        Attachment attachment = attachments.get(0);
        assertEquals(expectedFile, attachment.filename);

        //Make sure the content is correct.
        GetMethod data = new GetMethod(attachment.content);
        try
        {
            assertEquals(HttpStatus.SC_OK, client.executeMethod(data));
            ByteArrayOutputStream expectedStream = new ByteArrayOutputStream();
            entity.writeRequest(expectedStream);
            assertStreams(new ByteArrayInputStream(expectedStream.toByteArray()), data.getResponseBodyAsStream());
        }
        finally
        {
            data.releaseConnection();
        }
    }

    public void testAttachmentsDisabled() throws IOException, JSONException
    {
        administration.attachments().disable();
        HttpClient client = createClientForUser("admin");

        String expectedFile = "test.data";
        PostMethod method = new PostMethod(getRestUrlWithToken(client, PARAM_FILENAME, expectedFile, PARAM_ISSUE_ID, ISSUE_ID));
        RandomBytesEntity entity = new RandomBytesEntity().setLength(150);
        method.setRequestEntity(entity);

        try
        {
            client.executeMethod(method);
            assertBadResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, method);
        }
        finally
        {
            method.releaseConnection();
        }
    }

    public void testNoAttachPermission() throws IOException, JSONException
    {
        administration.attachments().disable();
        HttpClient client = createClientForUser("fred");

        String expectedFile = "test.data";
        PostMethod method = new PostMethod(getRestUrlWithToken(client, PARAM_FILENAME, expectedFile, PARAM_ISSUE_ID, ISSUE_ID));
        RandomBytesEntity entity = new RandomBytesEntity().setLength(150);
        method.setRequestEntity(entity);

        try
        {
            client.executeMethod(method);
            assertBadResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, method);
        }
        finally
        {
            method.releaseConnection();
        }
    }

    public void testAttachmentToBig() throws IOException, JSONException
    {
        administration.attachments().disable();
        final HttpClient client = createClientForUser("fred");

        final String expectedFile = "test.data";
        final PostMethod method = new PostMethod(getRestUrlWithToken(client, PARAM_FILENAME, expectedFile, PARAM_ISSUE_ID, ISSUE_ID, PARAM_SIZE, Integer.MAX_VALUE));
        final RandomBytesEntity entity = new RandomBytesEntity().setLength(150);
        method.setRequestEntity(entity);

        try
        {
            client.executeMethod(method);
            assertBadResponse(HttpStatus.SC_BAD_REQUEST, method,"test.data is too large to attach. Attachment is 2,048.00 MB but the largest allowed attachment is 0.2 kB.");
        }
        finally
        {
            method.releaseConnection();
        }
    }

    public void testBadXsrfAttach() throws IOException, JSONException
    {
        administration.attachments().disable();
        HttpClient client = createClientForUser("fred");

        String expectedFile = "test.data";
        PostMethod method = new PostMethod(getRestUrl(PARAM_FILENAME, expectedFile, PARAM_ISSUE_ID, ISSUE_ID));
        RandomBytesEntity entity = new RandomBytesEntity().setLength(150);
        method.setRequestEntity(entity);

        try
        {
            client.executeMethod(method);
            assertXsrfResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, method, client);
        }
        finally
        {
            method.releaseConnection();
        }
    }

    private void assertStreams(InputStream expected, InputStream actual) throws IOException
    {
        int expectedByte = expected.read(), actualByte = actual.read();
        int count = 0;

        while (expectedByte >= 0 && actualByte >= 0)
        {
            assertEquals(String.format("Byte at position %d differers.%n", count), expectedByte, actualByte);
            expectedByte = expected.read();
            actualByte = actual.read();
            count++;
        }

        if (expectedByte >= 0)
        {
            fail("Expected more input but actual had only " + count + " bytes.");
        }
        else if (actualByte >= 0)
        {
            fail("Expected less input as expected had only " + count + " bytes.");
        }
    }

    private void assertBadResponse(final int expectedResponse, final HttpMethod method, final String message)
    {
        JSONObject result = assertBadCommon(expectedResponse, method);
        assertFalse("token should not be deffined in response",result.has(JSON_TOKEN));
        try
        {
            Assert.assertThat(result.getString(JSON_ERROR_MSG), CoreMatchers.containsString(message));
        }
        catch (JSONException e)
        {
            log.error("Response did not contain JSON.", e);
            fail("Response did not contain JSON.");
        }
    }

    private void assertBadResponse(int expectedResponse, HttpMethod method)
    {
        assertBadResponse(expectedResponse, method,"");
    }

    private JSONObject assertBadCommon(int expectedResponse, HttpMethod method)
    {
        assertEquals(expectedResponse, method.getStatusCode());
        JSONObject jsonObject = null;
        try
        {
            jsonObject = new JSONObject(method.getResponseBodyAsString());
            assertTrue(jsonObject.has(JSON_ERROR_MSG));
        }
        catch (JSONException e)
        {
            log.error("Response did not contain JSON.", e);
            fail("Response did not contain JSON.");
        }
        catch (IOException e)
        {
            log.error("IO error occured while reading the response.", e);
            fail("IO error occured while reading the response.");
        }
        return jsonObject;
    }

    private void assertXsrfResponse(int expectedResponse, HttpMethod method, HttpClient client)
    {
        JSONObject result = assertBadCommon(expectedResponse, method);
        assertTrue(result.has(JSON_TOKEN));
        try
        {
            assertEquals(getToken(client), result.getString(JSON_TOKEN));
        }
        catch (JSONException e)
        {
            throw new RuntimeException(e);
        }
    }

    private HttpClient createClientForUser(final String user) throws IOException
    {
        HttpClient client = new HttpClient();
        client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

        PostMethod method = new PostMethod(getUrl("login.jsp"));
        try
        {
            method.setParameter(PARAM_USERNAME, user);
            method.setParameter(PARAM_PASSWORD, user);
            method.setParameter(PARAM_COOKIE, String.valueOf(true));

            client.executeMethod(method);
            assertEquals(GOOD_LOGIN, method.getResponseHeader(HEADER_SERAPH_RESPONSE).getValue());
        }
        finally
        {
            method.releaseConnection();
        }

        return client;
    }

    private String getToken(HttpClient client)
    {
        Cookie[] cookies = client.getState().getCookies();
        for (Cookie cookie : cookies)
        {
            if (cookie.getName().equals(COOKIE_XSRF))
            {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String getRestUrl(Object... params)
    {
        return getUrl(REST_PATH, params);
    }

    public String getRestUrlWithToken(HttpClient client, Object... params)
    {
        List<Object> objects = new ArrayList<Object>(Arrays.asList(params));
        objects.add(PARAM_XSRF);
        objects.add(getToken(client));
        return getUrl(REST_PATH, objects);
    }

    private String getUrl(final String path, Object... params)
    {
        return getUrl(path, Arrays.asList(params));
    }

    private String getUrl(final String path, Collection<Object> params)
    {
        String baseUrl = getEnvironmentData().getBaseUrl().toExternalForm();
        StringBuilder builder = new StringBuilder(baseUrl);
        if (!baseUrl.endsWith("/"))
        {
            builder.append("/");
        }
        builder.append(path);

        if (params != null && !params.isEmpty())
        {
            Iterator<Object> object = params.iterator();
            int pos = 1;
            builder.append("?").append(encode(object.next())).append("=");

            while (object.hasNext())
            {
                //The key is the even parameters.
                boolean key = (pos & 0x1) == 0;
                if (key)
                {
                    builder.append("&");
                }
                builder.append(encode(object.next()));
                if (key)
                {
                    builder.append("=");
                }
                pos++;
            }
        }

        return builder.toString();
    }

    private String encode(Object obj)
    {
        try
        {
            return URLEncoder.encode(obj.toString(), "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static class RandomBytesEntity implements RequestEntity
    {
        private int contentLength = 10 * 1024;
        private int actualLength = contentLength;
        private String contentType = "application/octet-stream";

        public RandomBytesEntity setLength(int length)
        {
            this.actualLength = this.contentLength = length;
            return this;
        }

        public RandomBytesEntity setActualLength(int actualLength)
        {
            this.actualLength = actualLength;
            return this;
        }

        public RandomBytesEntity setContentLength(int contentLength)
        {
            this.contentLength = contentLength;
            return this;
        }

        public RandomBytesEntity setContentType(String contentType)
        {
            this.contentType = contentType;
            return this;
        }

        @Override
        public boolean isRepeatable()
        {
            return true;
        }

        @Override
        public void writeRequest(OutputStream out) throws IOException
        {
            int left = actualLength;
            int counter = 0;
            byte[] bytes = new byte[4];
            while (left > 0)
            {
                bytes[0] = (byte) counter;
                bytes[1] = (byte) (counter >> 8);
                bytes[2] = (byte) (counter >> 16);
                bytes[3] = (byte) (counter >> 24);

                int write = Math.min(left, bytes.length);
                out.write(bytes, 0, write);

                left -= write;
                counter++;
            }
        }

        @Override
        public long getContentLength()
        {
            return contentLength;
        }

        @Override
        public String getContentType()
        {
            return contentType;
        }
    }
}
