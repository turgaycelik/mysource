package com.atlassian.jira.lookandfeel.upload;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.lookandfeel.ImageScaler;
import com.atlassian.jira.lookandfeel.image.ImageDescriptor;
import com.atlassian.jira.lookandfeel.image.MockImageDescriptor;
import com.atlassian.jira.util.I18nHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith (MockitoJUnitRunner.class)
public class LogoUploaderTest
{

    @Mock
    private ApplicationProperties applicationProperties;
    @Mock
    private JiraHome jiraHome;
    @Mock
    private ImageScaler imageScaler;
    @Mock
    private I18nHelper i18nHelper;
    @Mock
    private UploadService uploadService;

    private LogoUploader logoUploader;

    @Before
    public void setupClass()
    {
        logoUploader = new LogoUploader(applicationProperties, jiraHome, imageScaler, i18nHelper, uploadService);
    }

    @Test
    public void testValidate_NullImageName() throws IOException
    {
        final ImageDescriptor imageDescriptor = new MockImageDescriptor(null, null);
        assertFalse(logoUploader.validate(imageDescriptor));
        verify(i18nHelper).getText("jira.lookandfeel.upload.error", imageDescriptor.getImageDescriptorType(), imageDescriptor.getImageName());
    }

    @Test
    public void testValidate_EmptyImageName()
    {
        final ImageDescriptor imageDescriptor = new MockImageDescriptor("", null);
        assertFalse(logoUploader.validate(imageDescriptor));
        verify(i18nHelper).getText("jira.lookandfeel.upload.error", imageDescriptor.getImageDescriptorType(), null);
    }

    @Test
    public void testValidate_NullContentType()
    {
        final ImageDescriptor imageDescriptor = new MockImageDescriptor("test", null);
        assertFalse(logoUploader.validate(imageDescriptor));
        verify(i18nHelper).getText("jira.lookandfeel.upload.error", imageDescriptor.getImageDescriptorType(), imageDescriptor.getImageName());
        verify(i18nHelper).getText("jira.lookandfeel.upload.mimetype.unsupported", imageDescriptor.getContentType());
    }

    @Test
    public void testValidate_EmptyContentType()
    {
        final ImageDescriptor imageDescriptor = new MockImageDescriptor("test", "");
        assertFalse(logoUploader.validate(imageDescriptor));
        verify(i18nHelper).getText("jira.lookandfeel.upload.error", imageDescriptor.getImageDescriptorType(), imageDescriptor.getImageName());
        verify(i18nHelper).getText("jira.lookandfeel.upload.mimetype.unsupported", imageDescriptor.getContentType());
    }

    @Test
    public void testValidate_InvalidContentType()
    {
        final ImageDescriptor imageDescriptor = new MockImageDescriptor("test", "charset-UTF-8");
        assertFalse(logoUploader.validate(imageDescriptor));
        verify(i18nHelper).getText("jira.lookandfeel.upload.error", imageDescriptor.getImageDescriptorType(), imageDescriptor.getImageName());
        verify(i18nHelper).getText("jira.lookandfeel.upload.mimetype.unsupported", imageDescriptor.getContentType());
    }

    @Test
    public void testValidate_ValidSimpleContentType()
    {
        final ImageDescriptor imageDescriptor = new MockImageDescriptor("test", "image/png");
        assertTrue(logoUploader.validate(imageDescriptor));
    }

    @Test
    public void testValidate_ValidSimpleContentTypeUpperCase()
    {
        final ImageDescriptor imageDescriptor = new MockImageDescriptor("test", "IMAGE/PNG");
        assertTrue(logoUploader.validate(imageDescriptor));
    }

    @Test
    public void testValidate_ValidMultipleContentType()
    {
        final ImageDescriptor imageDescriptor = new MockImageDescriptor("test", "image/png;charset-UTF-8");
        assertTrue(logoUploader.validate(imageDescriptor));
    }

    @Test
    public void testValidate_ValidMultipleContentType_2()
    {
        final ImageDescriptor imageDescriptor = new MockImageDescriptor("test", "charset-UTF-8;image/png");
        assertTrue(logoUploader.validate(imageDescriptor));
    }

    @Test
    public void testValidate_NoInputStream()
    {
        final ImageDescriptor imageDescriptor = new MockImageDescriptor("test", "image/png;charset-UTF-8", null);
        assertFalse(logoUploader.validate(imageDescriptor));
        verify(i18nHelper).getText("jira.lookandfeel.upload.error", imageDescriptor.getImageDescriptorType(), imageDescriptor.getImageName());
    }
}
