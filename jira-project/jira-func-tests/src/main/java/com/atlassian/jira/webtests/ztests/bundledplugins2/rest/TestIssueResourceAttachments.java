package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.testkit.client.restclient.Attachment;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.AttachmentRendered;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;

/**
 * Func test case for issue resource attachments functionality.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceAttachments extends RestFuncTest
{
    private static final String ISSUE_KEY = "MKY-1";
    private IssueClient issueClient;

    public void testAttachmentsExpanded() throws Exception
    {
        Issue issue = issueClient.get(ISSUE_KEY);
        assertEquals(ISSUE_KEY, issue.key);
        assertEquals(3, issue.fields.attachment.size());

        // check only attachment 1:
        // {
        //     self: "http://localhost:8090/jira/rest/api/2/attachment/10000",
        //     filename: "attachment.txt",
        //     author": {
        //       self: "http://localhost:8090/jira/rest/api/2/user/admin",
        //       name: "admin",
        //       fullName: "Administrator"
        //     },
        //     created: "2010-06-09T15:59:34.602+1000",
        //     size: 19,
        //     mimeType: "text/plain",
        //     content: "http://localhost:8090/jira/secure/attachment/10000/attachment.txt"
        // }
        for (Attachment attachment1 : issue.fields.attachment)
        {
            if (attachment1.self.endsWith("/10000"))
            {
                assertEquals(getBaseUrl() + "/rest/api/2/attachment/10000", attachment1.self);
                assertEquals("attachment.txt", attachment1.filename);
                assertEquals(getBaseUrl() + "/rest/api/2/user?username=admin", attachment1.author.self);
                assertEquals(ADMIN_USERNAME, attachment1.author.name);
                assertEquals(ADMIN_FULLNAME, attachment1.author.displayName);
                assertEqualDateStrings("2010-06-09T15:59:34.602+1000", attachment1.created);
                assertEquals(19, attachment1.size);
                assertEquals("text/plain", attachment1.mimeType);
                assertEquals(getBaseUrl() + "/secure/attachment/10000/attachment.txt", attachment1.content);
                return;
            }
        }

        fail("attachment 10000 is missing");
    }

    public void testAttachmentsRendered()
    {
        Issue issue = issueClient.get(ISSUE_KEY, Issue.Expand.renderedFields);
        assertEquals(ISSUE_KEY, issue.key);
        assertEquals(3, issue.fields.attachment.size());

        int attachCount = 0;
        for (AttachmentRendered attachment1 : issue.renderedFields.attachment)
        {
            if (attachment1.self.endsWith("/10000"))
            {
                assertEquals(getBaseUrl() + "/rest/api/2/attachment/10000", attachment1.self);
                assertEquals("attachment.txt", attachment1.filename);
                assertEquals(getBaseUrl() + "/rest/api/2/user?username=admin", attachment1.author.self);
                assertEquals(ADMIN_USERNAME, attachment1.author.name);
                assertEquals(ADMIN_FULLNAME, attachment1.author.displayName);
                assertEqualDateStrings("09/Jun/10 3:59 PM", attachment1.created);
                assertEquals("0.0 kB", attachment1.size);
                assertEquals("text/plain", attachment1.mimeType);
                assertEquals(getBaseUrl() + "/secure/attachment/10000/attachment.txt", attachment1.content);
                attachCount++;
            }
            else if (attachment1.self.endsWith("/10010"))
            {
                assertEquals("123 kB", attachment1.size);
                assertEqualDateStrings("28/Jul/11 12:12 PM", attachment1.created);
                attachCount++;
            }
            else if (attachment1.self.endsWith("/10001"))
            {
                assertEquals("0.0 kB", attachment1.size);
                assertEqualDateStrings("09/Jun/10 3:59 PM", attachment1.created);
                attachCount++;
            }
        }

        if (attachCount != 3)
        {
            fail("attachments collection didn't match");
        }
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
        administration.restoreData("TestIssueResourceAttachments.xml");
    }
}
