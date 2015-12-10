package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Attachment;
import com.atlassian.jira.testkit.client.restclient.AttachmentClient;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.Response;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Func test for the attachment resource.
 *
 * @since v4.2
 */
@WebTest({ Category.FUNC_TEST, Category.REST })
public class TestAttachmentResource extends RestFuncTest
{
    private AttachmentClient attachmentClient;
    private IssueClient issueClient;

    public void testViewAttachment() throws Exception
    {
        // first try to open the issue that contains the attachments
        Issue mky1 = issueClient.get("MKY-1");
        assertThat(mky1.fields.attachment.size(), equalTo(3));

        // {
        //   self: http://localhost:8090/jira/rest/api/2/attachment/10000
        //   filename: attachment.txt
        //   author: {
        //     self: http://localhost:8090/jira/rest/api/2/user?username=admin
        //     name: admin
        //     displayName: Administrator
        //   }
        //   created: 2010-06-09T15:59:34.602+1000
        //   size: 19
        //   mimeType: text/plain
        //   properties: {
        //     magickey7: 1311805918487
        //     magickey6: "Picture of an big long elephant"
        //     magickey5: "Picture of an elephant"
        //     magickey4: 3.14
        //     magickey3: 1212121
        //     magickey2: 11
        //     magickey1: true
        //   }
        //   content: http://localhost:8090/jira/secure/attachment/10000/attachment.txt
        // }

        // now go and verify one of those attachments
        Attachment attachment1 = attachmentClient.get("10000");
        assertEquals(getBaseUrl() + "/rest/api/2/attachment/10000", attachment1.self);
        assertEquals("attachment.txt", attachment1.filename);
        assertEquals(getBaseUrl() + "/rest/api/2/user?username=admin", attachment1.author.self);
        assertEquals(ADMIN_USERNAME, attachment1.author.name);
        assertEquals(ADMIN_FULLNAME, attachment1.author.displayName);
        assertEqualDateStrings("2010-06-09T15:59:34.602+1000", attachment1.created);
        assertEquals(19L, attachment1.size);
        assertEquals("text/plain", attachment1.mimeType);
        assertEquals(getBaseUrl() + "/secure/attachment/10000/attachment.txt", attachment1.content);
        assertTrue((Boolean) attachment1.properties.get("magickey1"));
        assertEquals(Integer.valueOf(11), attachment1.properties.get("magickey2"));
        assertEquals(Integer.valueOf(1212121), attachment1.properties.get("magickey3"));
        assertEquals(Double.valueOf(3.14), attachment1.properties.get("magickey4"));
        assertEquals("Picture of an elephant", attachment1.properties.get("magickey5"));
        assertEquals("Picture of an big long elephant", attachment1.properties.get("magickey6"));
        assertEquals("2011-07-28T08:31:58.487+1000", attachment1.properties.get("magickey7"));
    }

    public void testViewAttachmentNotFound() throws Exception
    {
        // {"errorMessages":["The attachment with id '123' does not exist"],"errors":[]}
        Response response123 = attachmentClient.getResponse("123");
        assertEquals(404, response123.statusCode);
        assertEquals(1, response123.entity.errorMessages.size());
        assertTrue(response123.entity.errorMessages.contains("The attachment with id '123' does not exist"));

        // {"errorMessages":["The attachment with id 'abc' does not exist"],"errors":[]}
        Response responseAbc = attachmentClient.getResponse("abc");
        assertEquals(404, responseAbc.statusCode);
        assertEquals(1, responseAbc.entity.errorMessages.size());
        assertTrue(responseAbc.entity.errorMessages.contains("The attachment with id 'abc' does not exist"));
    }

    public void testViewAttachmentNotAuthorised() throws Exception
    {
        Response response = attachmentClient.anonymous().getResponse("10000");
        assertEquals(403, response.statusCode);
        assertEquals(1, response.entity.errorMessages.size());
        assertTrue(response.entity.errorMessages.contains("You do not have permission to view attachment with id: 10000"));
    }

    public void testRemoveAttachment() throws Exception
    {
        // first try to open the issue that contains the attachments
        Issue mky1 = issueClient.get("MKY-1");
        assertThat(mky1.fields.attachment.size(), equalTo(3));

        // Get the attachment first
        Attachment attachment1 = attachmentClient.get("10000");
        assertEquals("attachment.txt", attachment1.filename);
        // Delete it
        Response response = attachmentClient.deleteResponse("10000");
        assertEquals(204, response.statusCode);

        // Try and get the attachment again, should be 404
        response = attachmentClient.getResponse("10000");
        assertEquals(404, response.statusCode);
        mky1 = issueClient.get("MKY-1");
        assertThat(mky1.fields.attachment.size(), equalTo(2));
    }

    public void testDeleteAttachmentNotFound() throws Exception
    {
        // {"errorMessages":["The attachment with id '123' does not exist"],"errors":[]}
        Response response123 = attachmentClient.deleteResponse("123");
        assertEquals(404, response123.statusCode);
        assertEquals(1, response123.entity.errorMessages.size());
        assertTrue(response123.entity.errorMessages.contains("The attachment with id '123' does not exist"));

        // {"errorMessages":["The attachment with id 'abc' does not exist"],"errors":[]}
        Response responseAbc = attachmentClient.deleteResponse("abc");
        assertEquals(404, responseAbc.statusCode);
        assertEquals(1, responseAbc.entity.errorMessages.size());
        assertTrue(responseAbc.entity.errorMessages.contains("The attachment with id 'abc' does not exist"));
    }

    public void testGetMeta() throws Exception
    {
        Map responseMeta = attachmentClient.getMeta();
        assertEquals(true, responseMeta.get("enabled"));
        assertEquals(10485760, responseMeta.get("uploadLimit"));
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
        attachmentClient = new AttachmentClient(getEnvironmentData());
        administration.restoreData("TestIssueResourceAttachments.xml");
    }
}
