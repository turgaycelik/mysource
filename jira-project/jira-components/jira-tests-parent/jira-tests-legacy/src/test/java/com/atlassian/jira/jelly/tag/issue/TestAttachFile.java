package com.atlassian.jira.jelly.tag.issue;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueImpl;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.jelly.AbstractJellyTestCase;
import com.atlassian.jira.jelly.service.JellyServiceException;
import com.atlassian.jira.jelly.tag.util.JellyTagUtils;
import electric.xml.Document;
import electric.xml.Element;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

public class TestAttachFile extends AbstractJellyTestCase
{
    private AttachmentManager attachmentManager;
    private String attachmentPath;
    private GenericValue issue;
    private Issue issueObject;
    private File fileToAttach;
    private File attachmentDirectory;

    public TestAttachFile(String s)
    {
        super(s);
        if (attachmentPath == null)
        {
            SecureRandom random = new SecureRandom();
            attachmentPath = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "TestAttachFile" + random.nextInt() + System.getProperty("file.separator");
        }
    }

    protected TestAttachFile(String s, String scriptFilename)
    {
        super(s, scriptFilename);
    }

    protected String getRelativePath()
    {
        return "tag" + FS + "issue" + FS;
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        attachmentManager = ComponentAccessor.getAttachmentManager();
        ComponentAccessor.getAttachmentPathManager().setCustomAttachmentPath(attachmentPath);

        UtilsForTests.getTestEntity("ProjectKey", EasyMap.build("projectKey", "TST", "projectId", new Long(1)));
        GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "Project", "id", new Long(1), "key", "TST", "originalkey", "TST"));
        issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "A summary", "project", project.getLong("id"), "number", 1L));
        issueObject = IssueImpl.getIssueObject(issue);

        attachmentDirectory = new File(attachmentPath);
        if (!attachmentDirectory.exists())
        {
            attachmentDirectory.mkdirs();
        }

        fileToAttach = new File(attachmentPath + "attachMe");
        if (!fileToAttach.exists())
        {
            try
            {
                BufferedWriter out = new BufferedWriter(new FileWriter(fileToAttach));
                out.write("fileToAttach content");
                out.close();
            }
            catch (IOException e)
            {
                // TODO: Should we swallow this, or should we be throwing it?
            }
        }
    }

    protected void tearDown() throws Exception
    {
        fileToAttach.delete();
        rmdir(attachmentDirectory);
        super.tearDown();
    }

    public void testAttachFileWithInvalidAttributes() throws JellyServiceException
    {
        try
        {
            attachFileUsingJelly(null, null, null, null);
            fail("Should have thrown a JellyServiceException");
        }
        catch (JellyServiceException e)
        {
            assertTrue(e.getMessage().indexOf("<jira:AttachFile> You must define an attribute called 'key' for this tag.") != -1);
        }

        try
        {
            attachFileUsingJelly("invalidKey", null, null, null);
            fail("Should have thrown a JellyServiceException");
        }
        catch (JellyServiceException e)
        {
            assertTrue(e.getMessage().indexOf("<jira:AttachFile> You must define an attribute called 'filepath' for this tag.") != -1);
        }

        try
        {
            attachFileUsingJelly("invalidKey", "invalidFile", null, null);
            fail("Should have thrown a JellyServiceException");
        }
        catch (JellyServiceException e)
        {
            assertTrue(e.getMessage().indexOf("<jira:AttachFile> Unable to make temporary copy of file.") != -1);
        }

        try
        {
            attachFileUsingJelly(issueObject.getKey(), fileToAttach.getAbsolutePath(), "invalidOption", null);
            fail("Should have thrown a JellyServiceException");
        }
        catch (JellyServiceException e)
        {
            assertTrue(e.getMessage().indexOf("<jira:AttachFile> Invalid value for the 'option' attribute. Must be one of the following: [skip, override, add]") != -1);
        }

        try
        {
            attachFileUsingJelly("TST-2", fileToAttach.getAbsolutePath(), "add", null);
            fail("Should have thrown a JellyServiceException");
        }
        catch (JellyServiceException e)
        {
            assertTrue(e.getMessage().indexOf("<jira:AttachFile> Unable to find an issue with key 'TST-2' not adding the attachment.") != -1);
        }

        try
        {
            attachFileUsingJelly(issueObject.getKey(), fileToAttach.getAbsolutePath(), null, "invalidCreated");
            fail("Should have thrown a JellyServiceException");
        }
        catch (JellyServiceException e)
        {
            assertTrue(e.getMessage().indexOf("<jira:AttachFile> Timestamp format must be yyyy-mm-dd hh:mm:ss") != -1);
        }
    }

    public void testAttachFileWithNoOption() throws JellyServiceException
    {
        //Check to see the issue has no attachments to start with
        List attachments = attachmentManager.getAttachments(issueObject);
        assertTrue(attachments.isEmpty());

        //no options default to 'add' (which just attaches as new files
        assertAndAttachFile(null, null, 1);
        assertAndAttachFile(null, "2007-01-31 01:23:45.00", 2);
        assertAndAttachFile("", null, 3);
    }

    public void testAttachFileWithOptionAdd() throws JellyServiceException
    {
        //Check to see the issue has no attachments to start with
        List attachments = attachmentManager.getAttachments(issueObject);
        assertTrue(attachments.isEmpty());

        //attach 3 files with the same name
        assertAndAttachFile("add", null, 1);
        assertAndAttachFile("add", "2007-02-28 09:30:00.00", 2);
        assertAndAttachFile("add", null, 3);
    }

    public void testAttachFileWithOptionOverride() throws JellyServiceException
    {
        //Check to see the issue has no attachments to start with
        List attachments = attachmentManager.getAttachments(issueObject);
        assertTrue(attachments.isEmpty());

        //each file over-rides the previous (new created date for each)
        assertAndAttachFile("override", null, 1);
        assertAndAttachFile("override", "2007-03-31 12:45:13.00", 1);
        assertAndAttachFile("override", null, 1);
    }

    public void testAttachFileWithOptionSkip() throws JellyServiceException
    {
        //Check to see the issue has no attachments to start with
        List attachments = attachmentManager.getAttachments(issueObject);
        assertTrue(attachments.isEmpty());

        //only the first is attached, rest are ignored as duplicates (same created date as first)
        assertAndAttachFile("skip", "2007-11-30 13:13:13.00", 1);
        assertAndAttachFile("skip", "2007-11-30 13:13:13.00", null, 1);
        assertAndAttachFile("skip", "2007-11-30 13:13:13.00", "2007-03-04 15:13:11.00", 1);
    }

    //--------------------------------------------------------------------------------------------------- Helper Methods
    public static boolean rmdir(File dir)
    {
        if (dir.isDirectory())
        {
            File[] children = dir.listFiles();
            for (int i = 0; i < children.length; i++)
            {
                rmdir(children[i]);
            }
        }
        return dir.delete();
    }

    private void assertAndAttachFile(String option, String created, int expectedNumOfAttachments) throws JellyServiceException
    {
        assertAndAttachFile(option, created, created, expectedNumOfAttachments);
    }

    private void assertAndAttachFile(String option, String expectedCreated, String created, int expectedNumOfAttachments) throws JellyServiceException
    {
        Timestamp createdTimeEstimate = null;
        if (expectedCreated == null)
        {
            createdTimeEstimate = UtilDateTime.nowTimestamp();
        }
        Document document = attachFileUsingJelly(issueObject.getKey(), fileToAttach.getAbsolutePath(), option, created);
        Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        //Check to see if attachment was created.
        List attachments = attachmentManager.getAttachments(issueObject);
        assertEquals(expectedNumOfAttachments, attachments.size());

        Attachment attachedFile = getLastAttachment(attachments);
        assertEquals(fileToAttach.getName(), attachedFile.getFilename());
        assertEquals(fileToAttach.length(), attachedFile.getFilesize().longValue());
        assertNull(attachedFile.getAuthorKey());
        if (expectedCreated != null)
        {
            //check that the created timestamp is exactly the same as the expected
            Timestamp expectedCreatedTimeStamp = JellyTagUtils.parseDate(expectedCreated);
            assertEquals(expectedCreatedTimeStamp, attachedFile.getCreated());
        }
        else
        {
            //check that the created date is roughly close to the time of attachment
            long diff = attachedFile.getCreated().getTime() - createdTimeEstimate.getTime();
            assertTrue("Expected and actual created time difference was too large: '" + diff + "'", 0 <= diff && diff <= 5000);
        }
        assertEquals(issueObject, attachedFile.getIssue());
        assertEquals(issueObject, attachedFile.getIssueObject());
    }

    private Attachment getLastAttachment(List attachments)
    {
        long lastId = -1;
        Attachment lastAttachment = null;
        for (Iterator iterator = attachments.iterator(); iterator.hasNext();)
        {
            Attachment attachment = (Attachment) iterator.next();
            if (lastId <= attachment.getId().longValue())
            {
                lastId = attachment.getId().longValue();
                lastAttachment = attachment;
            }
        }
        return lastAttachment;
    }

    private Document attachFileUsingJelly(String issueKey, String filePath, String option, String created) throws JellyServiceException
    {
        StringBuilder script = new StringBuilder("<jira:AttachFile");
        if (issueKey != null)
        {
            script.append(" key=\"").append(issueKey).append("\"");
        }
        if (filePath != null)
        {
            script.append(" filepath=\"").append(filePath).append("\"");
        }
        if (option != null)
        {
            script.append(" option=\"").append(option).append("\"");
        }
        if (created != null)
        {
            script.append(" created=\"").append(created).append("\"");
        }
        script.append("/>");
        return runScriptBody(script.toString());
    }
}
