package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.navigation.issue.FileAttachmentsList;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Attachment;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This doesn't use the REST client or webTester to attach files because it wants to do a REST style multipart/form-data
 * request, so it uses http client.
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueAttachmentsResource extends RestFuncTest
{
    private HttpClient client;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
        administration.attachments().enable();
        client = new HttpClient();
    }

    public void testAttachSingleAttachment() throws Exception
    {
        String key = navigation.issue().createIssue("monkey", "Bug", "Issue with attachment");
        PostMethod method = addAttachments(key, true, new MyAttachment("testfile.txt", "text/plain", "Hello world!!"));
        assertEquals(200, method.getStatusCode());
        InputStream jsonStream = method.getResponseBodyAsStream();
        ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
        List<Attachment> attachmentList = mapper.readValue(jsonStream, new TypeReference<List<Attachment>>(){});
        assertEquals(1, attachmentList.size());
        Attachment attachment = attachmentList.get(0);
        assertEquals("10000", attachment.id);
        assertEquals("testfile.txt", attachment.filename);
        assertEquals(getBaseUrl() + "/rest/api/2/attachment/10000", attachment.self);
        assertEquals(getBaseUrl() + "/rest/api/2/user?username=admin", attachment.author.self);
        assertEquals(ADMIN_USERNAME, attachment.author.name);
        assertEquals(ADMIN_FULLNAME, attachment.author.displayName);

        List<FileAttachmentsList.FileAttachmentItem> attachments = navigation.issue().attachments(key).list().get();
        assertEquals(1, attachments.size());
        FileAttachmentsList.FileAttachmentItem item = attachments.get(0);
        assertEquals("testfile.txt", item.getName());
        assertEquals("Hello world!!", navigation.issue().attachments(key).manage().downloadAttachmentAsString(item.getId(),
                item.getName()));
    }

    public void testAttachMultipleAttachments() throws Exception
    {
        String key = navigation.issue().createIssue("monkey", "Bug", "Issue with attachment");
        PostMethod method = addAttachments(key, true, new MyAttachment("testfile1.txt", "text/plain", "Hello first world!!"), new MyAttachment("testfile2.txt", "text/plain", "Hello second world!!"));
        assertEquals(200, method.getStatusCode());
        InputStream jsonStream = method.getResponseBodyAsStream();
        ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
        List<Attachment> attachmentList = mapper.readValue(jsonStream, new TypeReference<List<Attachment>>(){});
        assertEquals(2, attachmentList.size());
        Attachment attachment = attachmentList.get(0);
        assertEquals("10000", attachment.id);
        assertEquals("testfile1.txt", attachment.filename);
        assertEquals(getBaseUrl() + "/rest/api/2/attachment/10000", attachment.self);
        assertEquals(getBaseUrl() + "/rest/api/2/user?username=admin", attachment.author.self);
        assertEquals(ADMIN_USERNAME, attachment.author.name);
        assertEquals(ADMIN_FULLNAME, attachment.author.displayName);

        attachment = attachmentList.get(1);
        assertEquals("10001", attachment.id);
        assertEquals("testfile2.txt", attachment.filename);
        assertEquals(getBaseUrl() + "/rest/api/2/attachment/10001", attachment.self);
        assertEquals(getBaseUrl() + "/rest/api/2/user?username=admin", attachment.author.self);
        assertEquals(ADMIN_USERNAME, attachment.author.name);
        assertEquals(ADMIN_FULLNAME, attachment.author.displayName);

        List<FileAttachmentsList.FileAttachmentItem> attachments = navigation.issue().attachments(key).list().get();

        assertEquals(2, attachments.size());
        FileAttachmentsList.FileAttachmentItem item1 = attachments.get(0);
        assertEquals("testfile1.txt", item1.getName());
        assertEquals("Hello first world!!", navigation.issue().attachments(key).manage().downloadAttachmentAsString(item1.getId(),
                item1.getName()));
        FileAttachmentsList.FileAttachmentItem item2 = attachments.get(1);
        assertEquals("testfile2.txt", item2.getName());
        assertEquals("Hello second world!!", navigation.issue().attachments(key).manage().downloadAttachmentAsString(item2.getId(),
                item2.getName()));
    }

    public void testAttachTooBig() throws Exception
    {
        administration.attachments().enable("500");
        byte[] toobig = new byte[1000];
        for (int i = 0; i < 1000; i++)
        {
            toobig[i] = 'a';
        }
        String key = navigation.issue().createIssue("monkey", "Bug", "Issue with attachment");
        PostMethod method = addAttachments(key, true, new MyAttachment("toobig.txt", "text/plain", toobig));
        assertEquals(404, method.getStatusCode());

        List<FileAttachmentsList.FileAttachmentItem> attachments = navigation.issue().attachments(key).list().get();
        assertEquals(0, attachments.size());
    }

    public void testAttachmentsDisabled() throws Exception
    {
        administration.attachments().disable();
        String key = navigation.issue().createIssue("monkey", "Bug", "Issue with attachment");
        PostMethod method = addAttachments(key, true, new MyAttachment("testfile.txt", "text/plain", "Hello world!!"));
        assertEquals(403, method.getStatusCode());

        List<FileAttachmentsList.FileAttachmentItem> attachments = navigation.issue().attachments(key).list().get();
        assertEquals(0, attachments.size());
    }

    public void testXsrf() throws Exception
    {
        String key = navigation.issue().createIssue("monkey", "Bug", "Issue with attachment");
        PostMethod method = addAttachments(key, false, new MyAttachment("testfile.txt", "text/plain", "Hello world!!"));
        assertEquals(404, method.getStatusCode());

        List<FileAttachmentsList.FileAttachmentItem> attachments = navigation.issue().attachments(key).list().get();
        assertEquals(0, attachments.size());
    }

    public void testNoIssue() throws Exception
    {
        PostMethod method = addAttachments("BLAH-123", true, new MyAttachment("testfile.txt", "text/plain", "Hello world!!"));
        assertEquals(404, method.getStatusCode());
    }

    public void testNoPermission() throws Exception
    {
        String key = navigation.issue().createIssue("monkey", "Bug", "Issue with attachment");
        navigation.logout();
        PostMethod method = addAttachments(key, true, new MyAttachment("testfile.txt", "text/ plain", "Hello world!!"));
        assertEquals(401, method.getStatusCode());

        navigation.login(ADMIN_USERNAME);
        List<FileAttachmentsList.FileAttachmentItem> attachments = navigation.issue().attachments(key).list().get();
        assertEquals(0, attachments.size());
    }

    private PostMethod addAttachments(String issueKey, boolean addXsrf, MyAttachment... attachments) throws Exception
    {
        String url = getBaseUrlPlus("rest/api/2/issue", issueKey, "attachments");
        PostMethod method = new PostMethod(url);
        List<FilePart> fileParts = new ArrayList<FilePart>();
        for (MyAttachment attachment: attachments)
        {
            ByteArrayPartSource partSource = new ByteArrayPartSource(attachment.filename, attachment.contents);
            FilePart filePart = new FilePart("file", partSource, attachment.contentType, "UTF-8");
            fileParts.add(filePart);
        }
        MultipartRequestEntity entity = new MultipartRequestEntity(fileParts.toArray(new FilePart[fileParts.size()]),
                new HttpMethodParams());
        method.setRequestEntity(entity);
        if (addXsrf)
        {
            method.addRequestHeader("X-Atlassian-Token", "nocheck");
        }
        // Add the current cookie to the client, so we're logged in as the same user as the client
        String sessionId = getTester().getTestContext().getWebClient().getCookieValue("JSESSIONID");
        if (sessionId != null)
        {
            client.getState().addCookie(new Cookie(getDomain(), "JSESSIONID", sessionId, "/", null, false));
        }
        if (StringUtils.isNotBlank(environmentData.getTenant()))
        {
            method.addRequestHeader("X-Atlassian-Tenant", environmentData.getTenant());
        }
        client.executeMethod(method);
        return method;
    }

    private String getDomain()
    {
        String domain = getEnvironmentData().getProperty("jira.host");
        if (domain == null)
        {
            return "localhost";
        }
        return domain;
    }

    private static class MyAttachment
    {
        public String filename;
        public String contentType;
        public byte[] contents;

        private MyAttachment(String filename, String contentType, byte[] contents)
        {
            this.filename = filename;
            this.contentType = contentType;
            this.contents = contents;
        }

        private MyAttachment(String filename, String contentType, String contents)
        {
            this.filename = filename;
            this.contentType = contentType;
            this.contents = contents.getBytes();
        }
    }
}
