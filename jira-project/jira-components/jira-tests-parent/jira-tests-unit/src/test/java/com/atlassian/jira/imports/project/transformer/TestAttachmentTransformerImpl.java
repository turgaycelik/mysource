package com.atlassian.jira.imports.project.transformer;

import java.io.File;
import java.util.Date;

import com.atlassian.jira.external.beans.ExternalAttachment;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.user.util.UserUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

/**
 * @since v3.13
 */
public class TestAttachmentTransformerImpl
{
    @Test
    public void testTransform()
    {
        final UserUtil userUtil = mock(UserUtil.class);
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(userUtil, null);
        projectImportMapper.getIssueMapper().mapValue("2", "102");

        Date attachDate = new Date();
        ExternalAttachment externalAttachment = new ExternalAttachment();
        externalAttachment.setId("1");
        externalAttachment.setIssueId("2");
        externalAttachment.setAttachedDate(attachDate);
        externalAttachment.setAttacher("admin");
        externalAttachment.setAttachedFile(new File("/tmp"));
        externalAttachment.setFileName("test.txt");

        AttachmentTransformerImpl attachmentTransformer = new AttachmentTransformerImpl();
        final ExternalAttachment transformedAttachment = attachmentTransformer.transform(projectImportMapper, externalAttachment);
        assertNull(transformedAttachment.getId());
        assertEquals("102", transformedAttachment.getIssueId());
        assertEquals(attachDate, transformedAttachment.getAttachedDate());
        assertEquals(externalAttachment.getAttacher(), transformedAttachment.getAttacher());
        assertEquals(externalAttachment.getAttachedFile(), transformedAttachment.getAttachedFile());
        assertEquals(externalAttachment.getFileName(), transformedAttachment.getFileName());
    }

}
