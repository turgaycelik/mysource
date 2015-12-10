package com.atlassian.jira.issue.thumbnail;

import java.awt.color.CMMException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.atlassian.core.util.thumbnail.Thumber;
import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.util.ThumbnailConfiguration;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.attachment.AttachmentStore;
import com.atlassian.jira.issue.attachment.ThumbnailAccessor;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.io.InputStreamConsumer;
import com.atlassian.jira.util.mime.MimeManager;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.util.concurrent.Promise;
import com.atlassian.util.concurrent.Promises;

import com.google.common.base.Predicate;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static com.atlassian.core.util.thumbnail.Thumbnail.MimeType.PNG;
import static java.io.File.createTempFile;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.iterateFiles;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.apache.commons.lang3.Validate.validState;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestDefaultThumbnailManager
{
    private static final int MAX_HEIGHT = 60;
    private static final int MAX_WIDTH = 50;

    private static final long ATTACHMENT_ID = 666;

    private static final String FILE_NAME = "anyName";
    private static final String THUMBNAIL_IMAGE = "thumb.gif";
    private static final String UNSUPPORTED_MIME_TYPE = "foo/bar";

    private static final IOFileFilter THUMBNAIL_FILTER = new IOFileFilter()
    {
        @Override
        public boolean accept(final File file)
        {
            return file.getName().startsWith("thumbnail");
        }

        @Override
        public boolean accept(final File dir, final String name)
        {
            return false;
        }
    };

    private static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"));

    private static void assertThumbnail(final int height, final int width, final String thumbnailFileName,
            final Thumbnail.MimeType mimeType, final Thumbnail thumbnail)
    {
        assertNotNull(thumbnail);
        assertThat(thumbnail.getAttachmentId(), is(ATTACHMENT_ID));
        assertThat(thumbnail.getFilename(), is(thumbnailFileName));
        assertThat(thumbnail.getHeight(), is(height));
        assertThat(thumbnail.getMimeType(), is(mimeType));
        assertThat(thumbnail.getWidth(), is(width));
    }

    @InjectMocks private DefaultThumbnailManager thumbnailManager;

    @Mock private Attachment mockAttachment;
    @Mock private AttachmentManager mockAttachmentManager;
    @Mock private AttachmentStore mockAttachmentStore;
    @Mock private ThumbnailAccessor mockThumbnailAccessor;
    @Mock private DefaultThumbnailManager.JAIImageRenderer mockJaiImageRenderer;
    @Mock private DefaultThumbnailManager.ThumberFactory mockThumberFactory;
    @Mock private DefaultThumbnailManager.ThumbnailedImageFactory mockThumbnailedImageFactory;
    @Mock private File mockFile;
    @Mock private Issue mockIssue;
    @Mock private MimeManager mockMimeManager;
    @Mock private Predicate<Dimensions> mockRasterBasedRenderingThreshold;
    @Mock private StreamingImageRenderer mockStreamingImageRenderer;
    @Mock private Thumber mockThumber;
    @Mock private Thumbnail mockThumbnail;
    @Mock private ThumbnailConfiguration mockThumbnailConfiguration;
    @Mock private ThumbnailedImage mockThumbnailedImage;
    @Mock private User mockUser;
    @Mock private VelocityRequestContext mockVelocityRequestContext;
    @Mock private VelocityRequestContextFactory mockVelocityRequestContextFactory;

    @Before
    public void setUp()
    {
        when(mockAttachment.getFilename()).thenReturn(FILE_NAME);
        when(mockAttachment.getId()).thenReturn(ATTACHMENT_ID);
        when(mockThumberFactory.getInstance()).thenReturn(mockThumber);
        when(mockThumbnailConfiguration.getMaxHeight()).thenReturn(MAX_HEIGHT);
        when(mockThumbnailConfiguration.getMaxWidth()).thenReturn(MAX_WIDTH);
        when(mockThumbnailedImageFactory.getInstance(mockVelocityRequestContext, mockThumbnail))
                .thenReturn(mockThumbnailedImage);
        when(mockVelocityRequestContextFactory.getJiraVelocityRequestContext()).thenReturn(mockVelocityRequestContext);
    }

    @After
    public void tearDown()
    {
        deleteTemporaryThumbnailFiles();
    }

    private void deleteTemporaryThumbnailFiles()
    {
        final Iterator thumbnails = iterateFiles(TEMP_DIR, THUMBNAIL_FILTER, null);
        while (thumbnails.hasNext())
        {
            deleteQuietly((File) thumbnails.next());
        }
    }

    @Test
    public void attachmentKnownNotToBeThumbnailableShouldBeReportedAsSuch()
    {
        assertThumbnailableAlreadyKnown(false);
    }

    @Test
    public void attachmentKnownToBeThumbnailableShouldBeReportedAsSuch()
    {
        assertThumbnailableAlreadyKnown(true);
    }

    private void assertThumbnailableAlreadyKnown(final boolean thumbnailable)
    {
        // Set up
        when(mockAttachment.isThumbnailable()).thenReturn(thumbnailable);

        // Invoke and check
        assertThumbnailable(thumbnailable);
    }

    private void assertThumbnailable(final boolean expectedValue)
    {
        // Invoke
        final boolean actualThumbnailable = thumbnailManager.isThumbnailable(mockIssue, mockAttachment);

        // Check
        assertThat(actualThumbnailable, is(expectedValue));
    }

    @Test
    public void shouldNotBeAbleToThumbnailImageIfToolkitIsUnavailable()
    {
        // Set up
        when(mockAttachment.isThumbnailable()).thenReturn(null);
        when(mockThumber.checkToolkit()).thenReturn(false);

        // Invoke and check
        assertThumbnailable(false);
    }

    @Test
    public void unsupportedFileTypesShouldNotBeThumbnailable()
    {
        // Set up
        when(mockAttachment.isThumbnailable()).thenReturn(null);
        when(mockThumber.checkToolkit()).thenReturn(true);
        Promise<Boolean> notThumbnailable = Promises.promise(false);
        when(mockAttachmentStore.getAttachment(eq(mockAttachment), any(Function.class))).thenReturn(notThumbnailable);

        // Invoke and check
        assertThumbnailable(false);
        verifyThumbnailableUpdatedTo(false);
    }

    private void verifyThumbnailableUpdatedTo(final boolean valueExpectedToBeStored)
    {
        verify(mockAttachmentManager, only()).setThumbnailable(mockAttachment, valueExpectedToBeStored);
    }

    @Test
    public void fileWithMimeTypeNotSupportedByThumberShouldNotBeThumbnailable()
    {
        assertFileWithMimeTypeIsThumbnailable(UNSUPPORTED_MIME_TYPE, false);
    }

    @Test
    public void fileWithMimeTypeSupportedByThumberShouldBeThumbnailable()
    {
        final String supportedMimeTypeUpper = Thumber.getThumbnailMimeTypes().get(0).toUpperCase();
        assertFileWithMimeTypeIsThumbnailable(supportedMimeTypeUpper, true);
    }

    private void assertFileWithMimeTypeIsThumbnailable(final String mimeType, final boolean expectedValue)
    {
        // Set up
        when(mockAttachment.isThumbnailable()).thenReturn(null);
        when(mockThumber.checkToolkit()).thenReturn(true);
        Promise<Boolean> thumbnailable = Promises.promise(true);
        when(mockAttachmentStore.getAttachment(eq(mockAttachment), any(Function.class))).thenReturn(thumbnailable);
        when(mockThumber.isFileSupportedImage(mockFile)).thenReturn(true);
        setUpSuggestedMimeType(mimeType);

        // Invoke and check
        assertThumbnailable(expectedValue);
        verifyThumbnailableUpdatedTo(expectedValue);
    }

    private void setUpSuggestedMimeType(final String mimeType)
    {
        when(mockMimeManager.getSuggestedMimeType(FILE_NAME)).thenReturn(mimeType);
    }

    @Test
    public void gettingThumbnailsForIssueShouldReturnEmptyListIfNoneAreThumbnailable()
    {
        // Set up
        when(mockAttachment.isThumbnailable()).thenReturn(false);
        when(mockAttachmentManager.getAttachments(mockIssue)).thenReturn(Collections.singletonList(mockAttachment));

        // Invoke
        final Collection<Thumbnail> thumbnails = thumbnailManager.getThumbnails(mockIssue, mockUser);

        // Check
        assertThat(thumbnails, org.hamcrest.Matchers.<Thumbnail>emptyIterable());
    }

    @Test
    public void thumbnailShouldBeNullForNonThumbnailableAttachment()
    {
        // Set up
        when(mockAttachment.isThumbnailable()).thenReturn(false);

        // Invoke
        final Thumbnail thumbnail = thumbnailManager.getThumbnail(mockAttachment);

        // Check
        assertThat(thumbnail, is(nullValue()));
    }

    @Test
    public void nullThumbnailShouldHaveNullImage()
    {
        // Invoke
        final ThumbnailedImage thumbnailedImage = thumbnailManager.toThumbnailedImage(null);

        // Check
        assertThat(thumbnailedImage, is(nullValue()));
    }

    @Test
    public void nonNullThumbnailShouldHaveAnImage()
    {
        // Invoke
        final ThumbnailedImage thumbnailedImage = thumbnailManager.toThumbnailedImage(mockThumbnail);

        // Check
        assertThat(thumbnailedImage, sameInstance(mockThumbnailedImage));
    }

    @Test
    public void streamingThumbnailShouldProvideCorrectContent() throws IOException
    {
        // Set up
        final String content = "this is some content";
        final File thumbnailFile = createTempFile("thumbnail", null);
        try
        {
            writeStringToFile(thumbnailFile, content);
            when(mockThumbnailAccessor.getThumbnailFile(mockAttachment)).thenReturn(thumbnailFile);

            // Invoke
            final String actualContents = thumbnailManager.streamThumbnailContent(mockAttachment, new StringReader());

            // Check
            assertThat(actualContents, is(content));
        }
        finally
        {
            deleteQuietly(thumbnailFile);
        }
    }

    @Test
    public void shouldBeAbleToGetThumbnailThatExists()
    {
        // Set up
        when(mockAttachment.isThumbnailable()).thenReturn(true);
        final File thumbnailFile = getFile(THUMBNAIL_IMAGE);
        when(mockThumbnailAccessor.getThumbnailFile(mockIssue, mockAttachment)).thenReturn(thumbnailFile);

        // Invoke
        final Thumbnail thumbnail = thumbnailManager.getThumbnail(mockIssue, mockAttachment);

        // Check
        assertThumbnail(306, 227, THUMBNAIL_IMAGE, PNG, thumbnail);
    }

    @Test
    public void shouldBeAbleToGenerateThumbnailThatDoesNotExistUsingRasterBasedRendering() throws Exception
    {
        // Set up
        final int height = 300;
        final int width = 400;
        final File attachmentFile = getFile("my-attachment.jpg");
        final File thumbnailFile = File.createTempFile("thumbnail", ".png");
        deleteQuietly(thumbnailFile);
        final String attachmentFileName = attachmentFile.getName();
        when(mockAttachment.getFilename()).thenReturn(attachmentFileName);
        when(mockAttachment.isThumbnailable()).thenReturn(true);
        when(mockThumbnailAccessor.getThumbnailFile(mockIssue, mockAttachment)).thenReturn(thumbnailFile);
        when(mockAttachmentStore.getAttachmentFile(mockAttachment)).thenReturn(attachmentFile);
        final Dimensions mockDimensions = mock(Dimensions.class);
        when(mockAttachmentManager.streamAttachmentContent(eq(mockAttachment), any(InputStreamConsumer.class))).thenReturn(mockDimensions).then(new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable
            {
                InputStreamConsumer<?> f = (InputStreamConsumer<?>) invocation.getArguments()[1];
                InputStream is = new ByteArrayInputStream(new byte[0]);
                f.withInputStream(is);
                return mockThumbnail;
            }
        });
        when(mockRasterBasedRenderingThreshold.apply(any(Dimensions.class))).thenReturn(true);

        when(mockThumber.retrieveOrCreateThumbNail(any(InputStream.class), eq(attachmentFileName),
                argThat(new TempFileMatcher(thumbnailFile)), eq(MAX_WIDTH), eq(MAX_HEIGHT), eq(ATTACHMENT_ID)))
                .thenAnswer(new Answer<Thumbnail>()
                            {
                                @Override
                                public Thumbnail answer(final InvocationOnMock invocation) throws Throwable
                                {
                                    final File tempThumbnail = (File) invocation.getArguments()[2];
                                    // A side-effect that we depend upon
                                    validState(tempThumbnail.createNewFile());
                                    return mockThumbnail;
                                }
                            }
                );
        final Thumbnail.MimeType mimeType = Thumbnail.MimeType.values()[0];
        setUpMockThumbnail(height, width, mimeType);

        // Invoke
        final Thumbnail thumbnail = thumbnailManager.getThumbnail(mockIssue, mockAttachment);

        // Check
        assertThumbnail(height, width, thumbnailFile.getName(), mimeType, thumbnail);
    }

    @Test
    public void shouldUseJaiRenderingWhenRasterBasedRenderingThrowsCMMException() throws Exception
    {
        // Set up
        final int height = 300;
        final int width = 400;
        when(mockThumbnail.getHeight()).thenReturn(height);
        when(mockThumbnail.getWidth()).thenReturn(width);
        when(mockThumbnail.getMimeType()).thenReturn(PNG);
        final File attachmentFile = getFile("my-attachment.jpg");
        final File thumbnailFile = File.createTempFile("thumbnail", ".png");
        deleteQuietly(thumbnailFile);
        final String attachmentFileName = attachmentFile.getName();
        final Dimensions mockDimensions = mock(Dimensions.class);
        when(mockAttachment.getFilename()).thenReturn(attachmentFileName);
        when(mockAttachment.isThumbnailable()).thenReturn(true);
        when(mockThumbnailAccessor.getThumbnailFile(mockIssue, mockAttachment)).thenReturn(thumbnailFile);
        when(mockAttachmentStore.getAttachmentFile(mockAttachment)).thenReturn(attachmentFile);
        when(mockAttachmentManager.streamAttachmentContent(eq(mockAttachment), any(InputStreamConsumer.class))).thenReturn(mockDimensions).then(new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable
            {
                InputStreamConsumer<?> f = (InputStreamConsumer<?>) invocation.getArguments()[1];
                InputStream is = new ByteArrayInputStream(new byte[0]);
                f.withInputStream(is);
                return mockThumbnail;
            }
        });
        when(mockRasterBasedRenderingThreshold.apply(any(Dimensions.class))).thenReturn(true);
        when(mockThumber.retrieveOrCreateThumbNail(any(InputStream.class), eq(attachmentFileName),
                argThat(new TempFileMatcher(thumbnailFile)), eq(MAX_WIDTH), eq(MAX_HEIGHT), eq(ATTACHMENT_ID)))
                    .thenThrow(new CMMException("Some error"));
        when(mockDimensions.getHeight()).thenReturn(height);
        when(mockDimensions.getWidth()).thenReturn(width);
        when(mockJaiImageRenderer.renderThumbnail(
                any(InputStream.class), argThat(new TempFileMatcher(thumbnailFile)), eq(MAX_WIDTH), eq(MAX_HEIGHT)))
                .thenAnswer(new Answer<Dimensions>()
                {
                    @Override
                    public Dimensions answer(final InvocationOnMock invocation) throws Throwable
                    {
                        final File tempThumbnail = (File) invocation.getArguments()[1];
                        // A side-effect that we depend upon
                        validState(tempThumbnail.createNewFile());
                        return mockDimensions;
                    }
                });

        // Invoke
        final Thumbnail thumbnail = thumbnailManager.getThumbnail(mockIssue, mockAttachment);

        // Check
        assertThumbnail(height, width, thumbnailFile.getName(), PNG, thumbnail);
    }

    @Test
    public void shouldBeAbleToGenerateThumbnailThatDoesNotExistUsingStreamBasedRendering() throws Exception
    {
        // Set up
        final File attachmentFile = getFile("my-attachment.jpg");
        final File thumbnailFile = File.createTempFile("thumbnail", ".png");
        final Dimensions mockDimensions = mock(Dimensions.class);
        final int height = 300;
        final int width = 400;
        when(mockThumbnail.getHeight()).thenReturn(height);
        when(mockThumbnail.getWidth()).thenReturn(width);
        when(mockThumbnail.getMimeType()).thenReturn(PNG);
        when(mockAttachment.isThumbnailable()).thenReturn(true);
        when(mockThumbnailAccessor.getThumbnailFile(mockIssue, mockAttachment)).thenReturn(thumbnailFile);
        when(mockAttachmentStore.getAttachmentFile(mockAttachment)).thenReturn(attachmentFile);
        when(mockAttachmentManager.streamAttachmentContent(eq(mockAttachment), any(InputStreamConsumer.class))).thenReturn(mockDimensions).then(new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable
            {
                InputStreamConsumer<?> f = (InputStreamConsumer<?>) invocation.getArguments()[1];
                InputStream is = new ByteArrayInputStream(new byte[0]);
                f.withInputStream(is);
                return mockThumbnail;
            }
        });
        when(mockRasterBasedRenderingThreshold.apply(any(Dimensions.class))).thenReturn(false);
        when(mockStreamingImageRenderer.renderThumbnail(
                any(InputStream.class), argThat(new TempFileMatcher(thumbnailFile)), eq(MAX_WIDTH), eq(MAX_HEIGHT)))
                .thenAnswer(new Answer<Dimensions>()
                {
                    @Override
                    public Dimensions answer(final InvocationOnMock invocation) throws Throwable
                    {
                        final File tempThumbnail = (File) invocation.getArguments()[1];
                        // A side-effect that we depend upon
                        validState(tempThumbnail.createNewFile());
                        return mockDimensions;
                    }
                });
        when(mockDimensions.getHeight()).thenReturn(height);
        when(mockDimensions.getWidth()).thenReturn(width);

        // Invoke
        final Thumbnail thumbnail = thumbnailManager.getThumbnail(mockIssue, mockAttachment);

        // Check
        assertThumbnail(height, width, thumbnailFile.getName(), PNG, thumbnail);
    }

    @Test
    public void shouldGetBrokenThumbnailWhenStreamBasedRenderingThrowsCMMException() throws Exception
    {
        assertBrokenThumbnailWhenStreamRenderingThrows(new CMMException("some error"));
    }

    @Test
    public void shouldGetBrokenThumbnailWhenStreamBasedRenderingThrowsIOException() throws Exception
    {
        assertBrokenThumbnailWhenStreamRenderingThrows(new IOException("some error"));
    }

    private void assertBrokenThumbnailWhenStreamRenderingThrows(final Throwable throwable) throws Exception
    {
        // Set up
        final File attachmentFile = getFile("my-attachment.jpg");
        final File thumbnailFile = File.createTempFile("thumbnail", ".png");
        final Thumbnail mockBrokenThumbnail = mock(BrokenThumbnail.class);
        when(mockAttachment.isThumbnailable()).thenReturn(true);
        when(mockThumbnailAccessor.getThumbnailFile(mockIssue, mockAttachment)).thenReturn(thumbnailFile);
        when(mockAttachmentStore.getAttachmentFile(mockAttachment)).thenReturn(attachmentFile);
        final Dimensions mockDimensions = mock(Dimensions.class);
        when(mockAttachmentManager.streamAttachmentContent(eq(mockAttachment), any(InputStreamConsumer.class))).thenReturn(mockDimensions).then(new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable
            {
                InputStreamConsumer<?> f = (InputStreamConsumer<?>) invocation.getArguments()[1];
                InputStream is = new ByteArrayInputStream(new byte[0]);
                f.withInputStream(is);
                return mockBrokenThumbnail;
            }
        });
        when(mockRasterBasedRenderingThreshold.apply(any(Dimensions.class))).thenReturn(false);
        when(mockStreamingImageRenderer.renderThumbnail(
                any(InputStream.class), argThat(new TempFileMatcher(thumbnailFile)), eq(MAX_WIDTH), eq(MAX_HEIGHT)))
                .thenThrow(throwable);

        // Invoke
        final Thumbnail thumbnail = thumbnailManager.getThumbnail(mockIssue, mockAttachment);

        // Check
        assertBrokenThumbnail(thumbnail);
    }

    private void assertBrokenThumbnail(final Thumbnail thumbnail)
    {
        assertNotNull(thumbnail);
        assertThat(thumbnail, is(instanceOf(BrokenThumbnail.class)));
        assertThat(thumbnail.getAttachmentId(), is(ATTACHMENT_ID));
    }

    private void setUpMockThumbnail(final int height, final int width, final Thumbnail.MimeType mimeType)
    {
        when(mockThumbnail.getAttachmentId()).thenReturn(ATTACHMENT_ID);
        when(mockThumbnail.getHeight()).thenReturn(height);
        when(mockThumbnail.getMimeType()).thenReturn(mimeType);
        when(mockThumbnail.getWidth()).thenReturn(width);
    }

    private File getFile(final String name)
    {
        final URL fileUrl = getClass().getResource(name);
        if (fileUrl == null)
        {
            throw new IllegalArgumentException("Can't find '" + name + "' in this package");
        }
        try
        {
            return new File(fileUrl.toURI());
        }
        catch (URISyntaxException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private static class StringReader implements InputStreamConsumer<String>
    {
        @Override
        public String withInputStream(final InputStream input) throws IOException
        {
            return IOUtils.toString(input);
        }
    }

    private static class TempFileMatcher extends TypeSafeMatcher<File>
    {
        private final File thumbnailFile;

        public TempFileMatcher(final File thumbnailFile)
        {
            this.thumbnailFile = thumbnailFile;
        }

        @Override
        protected final boolean matchesSafely(final File file)
        {
            return file.getName().startsWith(thumbnailFile.getName())
                    && file.getName().endsWith(DefaultThumbnailManager.TEMP_FILE_SUFFIX);
        }

        @Override
        public final void describeTo(final Description description)
        {
            description.appendText("filename should start with ").appendValue(thumbnailFile.getName())
                    .appendText(" and end with ").appendValue(DefaultThumbnailManager.TEMP_FILE_SUFFIX);
        }
    }
}
