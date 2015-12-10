package com.atlassian.jira.config.util;

import java.io.File;

import com.atlassian.jira.mock.MockApplicationProperties;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestAttachmentPathManager
{
    private final MockJiraHome jiraHome = new MockJiraHome(new File("/jira-local-home").getAbsolutePath(), new File("/jira-shared-home").getAbsolutePath());

    @Test
    public void testDefaultAttachmentPath() throws Exception
    {
        AttachmentPathManager.PropertiesAdaptor attachmentPathManager = new AttachmentPathManager.PropertiesAdaptor(null, jiraHome);
        assertEquals(new File("/jira-shared-home/data/attachments").getAbsolutePath(), attachmentPathManager.getDefaultAttachmentPath());
    }

    @Test
    public void testAttachmentPath() throws Exception
    {
        final MockApplicationProperties applicationProperties = new MockApplicationProperties();
        AttachmentPathManager.PropertiesAdaptor attachmentPathManager = new AttachmentPathManager.PropertiesAdaptor(applicationProperties, jiraHome);
        applicationProperties.setString("jira.path.attachments", "/attachments");
        applicationProperties.setOption("jira.option.allowattachments", true);

        // test with default path
        applicationProperties.setOption("jira.path.attachments.use.default.directory", true);
        assertEquals(new File("/jira-shared-home/data/attachments").getAbsolutePath(), attachmentPathManager.getAttachmentPath());

        // test with custom path
        applicationProperties.setOption("jira.path.attachments.use.default.directory", false);
        assertEquals("/attachments", attachmentPathManager.getAttachmentPath());

        // TODO: Figure out what to return for "disabled".
    }

    @Test
    public void testGetMode() throws Exception
    {
        final MockApplicationProperties applicationProperties = new MockApplicationProperties();
        AttachmentPathManager.PropertiesAdaptor attachmentPathManager = new AttachmentPathManager.PropertiesAdaptor(applicationProperties, jiraHome);
        applicationProperties.setString("jira.path.attachments", "/attachments");

        // test disabled
        applicationProperties.setOption("jira.option.allowattachments", false);
        assertEquals(AttachmentPathManager.Mode.DISABLED, attachmentPathManager.getMode());

        // test default
        applicationProperties.setOption("jira.path.attachments.use.default.directory", true);
        applicationProperties.setOption("jira.option.allowattachments", true);
        assertEquals(AttachmentPathManager.Mode.DEFAULT, attachmentPathManager.getMode());

        // test custom
        applicationProperties.setOption("jira.path.attachments.use.default.directory", false);
        applicationProperties.setOption("jira.option.allowattachments", true);
        assertEquals(AttachmentPathManager.Mode.CUSTOM, attachmentPathManager.getMode());
    }

    @Test
    public void testSetCustomAttachmentPath() throws Exception
    {
        final MockApplicationProperties applicationProperties = new MockApplicationProperties();
        AttachmentPathManager.PropertiesAdaptor attachmentPathManager = new AttachmentPathManager.PropertiesAdaptor(applicationProperties, jiraHome);

        attachmentPathManager.setCustomAttachmentPath("/attachments");
        assertEquals(true, applicationProperties.getOption("jira.option.allowattachments"));
        assertEquals(false, applicationProperties.getOption("jira.path.attachments.use.default.directory"));
        assertEquals("/attachments", applicationProperties.getString("jira.path.attachments"));
        assertEquals(AttachmentPathManager.Mode.CUSTOM, attachmentPathManager.getMode());
        assertEquals(false, attachmentPathManager.getUseDefaultDirectory());
    }

    @Test
    public void testSetUseDefaultDirectory() throws Exception
    {
        final MockApplicationProperties applicationProperties = new MockApplicationProperties();
        AttachmentPathManager.PropertiesAdaptor attachmentPathManager = new AttachmentPathManager.PropertiesAdaptor(applicationProperties, jiraHome);

        attachmentPathManager.setUseDefaultDirectory();
        assertEquals(true, applicationProperties.getOption("jira.option.allowattachments"));
        assertEquals(true, applicationProperties.getOption("jira.path.attachments.use.default.directory"));
        assertEquals(new File("/jira-shared-home/data/attachments").getAbsolutePath(), applicationProperties.getString("jira.path.attachments"));
        assertEquals(AttachmentPathManager.Mode.DEFAULT, attachmentPathManager.getMode());
        assertEquals(true, attachmentPathManager.getUseDefaultDirectory());
    }

    @Test
    public void testDisableAttachments() throws Exception
    {
        final MockApplicationProperties applicationProperties = new MockApplicationProperties();
        AttachmentPathManager.PropertiesAdaptor attachmentPathManager = new AttachmentPathManager.PropertiesAdaptor(applicationProperties, jiraHome);

        attachmentPathManager.disableAttachments();
        assertEquals(false, applicationProperties.getOption("jira.option.allowattachments"));
        assertEquals(AttachmentPathManager.Mode.DISABLED, attachmentPathManager.getMode());        
        assertEquals(false, attachmentPathManager.getUseDefaultDirectory());
    }
}
