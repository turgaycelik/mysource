package com.atlassian.jira.issue.thumbnail;

import java.awt.*;
import java.awt.color.CMMException;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.JAI;
import javax.media.jai.OpImage;
import javax.media.jai.RenderedOp;

import com.atlassian.core.util.thumbnail.Thumber;
import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.ThumbnailConfiguration;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.attachment.AttachmentStore;
import com.atlassian.jira.issue.attachment.ThumbnailAccessor;
import com.atlassian.jira.util.io.InputStreamConsumer;
import com.atlassian.jira.util.log.OneShotLogger;
import com.atlassian.jira.util.mime.MimeManager;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.util.concurrent.Function;
import com.atlassian.util.concurrent.Promise;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.sun.media.jai.codec.SeekableStream;

import org.apache.log4j.Logger;

import static com.atlassian.core.util.thumbnail.Thumbnail.MimeType.PNG;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.touch;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang3.Validate.validState;

/**
 * Some of this code was taken in 4.4 from our friends in Confluence
 */
public class DefaultThumbnailManager implements ThumbnailManager
{
    @VisibleForTesting
    static final String TEMP_FILE_SUFFIX = ".tmp";

    @VisibleForTesting
    interface ThumberFactory
    {
        Thumber getInstance();
    }

    @VisibleForTesting
    interface ThumbnailedImageFactory
    {
        ThumbnailedImage getInstance(VelocityRequestContext velocityRequestContext, Thumbnail thumbnail);
    }

    private static final Logger LOG = Logger.getLogger(DefaultThumbnailManager.class);
    private static final OneShotLogger JAI_MESSAGE_LOG = new OneShotLogger(LOG);

    private static final ThumberFactory THUMBER_FACTORY = new ThumberFactory()
    {
        @Override
        public Thumber getInstance()
        {
            // Thumbers are NOT threadsafe and therefore need to be instantiated every time they're
            // used, to avoid problems like: https://jdog.atlassian.com/browse/JRADEV-9677
            return new Thumber(MIME_TYPE);
        }
    };

    /**
     * Exists to make the outer class more testable. Maybe this is a
     * sign that this factory ought to be an INTERNAL Pico component.
     */
    private static class DefaultThumbnailedImageFactory implements ThumbnailedImageFactory
    {
        private final ApplicationProperties applicationProperties;

        private DefaultThumbnailedImageFactory(final ApplicationProperties applicationProperties)
        {
            this.applicationProperties = applicationProperties;
        }

        @Override
        public ThumbnailedImage getInstance(final VelocityRequestContext velocityRequestContext, final Thumbnail thumbnail)
        {
            return new AtlassianCoreThumbnail(applicationProperties, velocityRequestContext, thumbnail);
        }
    }

    private static File getTempFile(final File thumbnailFile)
    {
        // Mixing the thread ID into the temp filename reduces the chances of inter- and intra-node clashes
        try
        {
            return File.createTempFile(thumbnailFile.getName(), TEMP_FILE_SUFFIX, thumbnailFile.getParentFile());
        }
        catch (final IOException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private final AttachmentManager attachmentManager;
    private final AttachmentStore attachmentStore;
    private final ThumbnailAccessor thumbnailAccessor;
    private final JAIImageRenderer jaiImageRenderer;
    private final MimeManager mimeManager;
    private final Predicate<Dimensions> rasterBasedRenderingThreshold;
    private final StreamingImageRenderer streamingImageRenderer;
    private final ThumberFactory thumberFactory;
    private final ThumbnailConfiguration thumbnailConfiguration;
    private final ThumbnailedImageFactory thumbnailedImageFactory;
    private final VelocityRequestContextFactory velocityRequestContextFactory;

    // Constructor invoked reflectively by Pico
    @SuppressWarnings("unused")
    public DefaultThumbnailManager(final ThumbnailConfiguration thumbnailConfiguration,
            final AttachmentManager attachmentManager, final MimeManager mimeManager,
            final ApplicationProperties applicationProperties,
            final VelocityRequestContextFactory velocityRequestContextFactory, final AttachmentStore attachmentStore,
            final ThumbnailAccessor thumbnailAccessor)
    {
        this(thumbnailConfiguration, attachmentManager, mimeManager, velocityRequestContextFactory, attachmentStore,
                thumbnailAccessor, new AdaptiveThresholdPredicate(), THUMBER_FACTORY,
                new DefaultThumbnailedImageFactory(applicationProperties),
                new StreamingImageRenderer(), new JAIImageRenderer());
    }

    // Constructor invoked reflectively from unit test via Mockito's @InjectMocks annotation
    @VisibleForTesting
    DefaultThumbnailManager(final ThumbnailConfiguration thumbnailConfiguration,
            final AttachmentManager attachmentManager, final MimeManager mimeManager,
            final VelocityRequestContextFactory velocityRequestContextFactory, final AttachmentStore attachmentStore,
            final ThumbnailAccessor thumbnailAccessor,
            final Predicate<Dimensions> rasterBasedRenderingThreshold, final ThumberFactory thumberFactory,
            final ThumbnailedImageFactory thumbnailedImageFactory, final StreamingImageRenderer streamingImageRenderer,
            final JAIImageRenderer jaiImageRenderer)
    {
        this.attachmentManager = attachmentManager;
        this.attachmentStore = attachmentStore;
        this.thumbnailAccessor = thumbnailAccessor;
        this.jaiImageRenderer = jaiImageRenderer;
        this.mimeManager = mimeManager;
        this.rasterBasedRenderingThreshold = rasterBasedRenderingThreshold;
        this.streamingImageRenderer = streamingImageRenderer;
        this.thumberFactory = thumberFactory;
        this.thumbnailConfiguration = thumbnailConfiguration;
        this.thumbnailedImageFactory = thumbnailedImageFactory;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }

    private Collection<Thumbnail> getThumbnails(final Collection<Attachment> attachments, final Issue issue)
    {
        final List<Thumbnail> thumbnails = new ArrayList<Thumbnail>();
        for (final Attachment attachment : attachments)
        {
            if (isThumbnailable(attachment))
            {
                thumbnails.add(doGetThumbnail(issue, attachment));
            }
        }
        return thumbnails;
    }

    @Override
    public Collection<Thumbnail> getThumbnails(final Issue issue, final User user)
    {
        return getThumbnails(attachmentManager.getAttachments(issue), issue);
    }

    @Override
    public boolean isThumbnailable(final Issue issue, final Attachment attachment)
    {
        // The attachment object may know this already, as once we have worked it out we store it in the database.
        if (attachment.isThumbnailable() != null)
        {
            return attachment.isThumbnailable();
        }

        // if toolkit is unavailable, we can't thumbnail the image.
        if (!checkToolkit())
        {
            return false;
        }

        // Check that file is of a valid image mime type
        // All thumbnails are saved in JPEG format on disk.
        boolean thumbnailable = false;
        Promise<Boolean> isSupportedImage = attachmentStore.getAttachment(attachment, new Function<InputStream, Boolean>()
        {
            @Override
            public Boolean get(final InputStream input)
            {
                return getThumber().isFileSupportedImage(input);
            }
        }).recover(new com.google.common.base.Function<Throwable, Boolean>()
        {
            @Override
            public Boolean apply(final Throwable t)
            {
                return false;
            }
        });

        if (isSupportedImage.claim())
        {
            final String mimeType = mimeManager.getSuggestedMimeType(attachment.getFilename());
            for (final String thumbnailMimeType : Thumber.THUMBNAIL_MIME_TYPES)
            {
                if (thumbnailMimeType.equalsIgnoreCase(mimeType))
                {
                    thumbnailable = true;
                }
            }
        }
        attachmentManager.setThumbnailable(attachment, thumbnailable);

        return thumbnailable;
    }

    @Override
    public boolean isThumbnailable(final Attachment attachment)
    {
        return isThumbnailable(attachment.getIssueObject(), attachment);
    }

    @Override
    public Thumbnail getThumbnail(final Attachment attachment)
    {
        return getThumbnail(attachment.getIssueObject(), attachment);
    }

    @Override
    public Thumbnail getThumbnail(final Issue issue, final Attachment attachment)
    {
        if (!isThumbnailable(issue, attachment))
        {
            return null;
        }
        return doGetThumbnail(issue, attachment);
    }

    @Override
    public boolean checkToolkit()
    {
        return getThumber().checkToolkit();
    }

    @Override
    public ThumbnailedImage toThumbnailedImage(@Nullable final Thumbnail thumbnail)
    {
        if (thumbnail == null)
        {
            return null;
        }
        final VelocityRequestContext velocityContext = velocityRequestContextFactory.getJiraVelocityRequestContext();
        return thumbnailedImageFactory.getInstance(velocityContext, thumbnail);
    }

    @Override
    public <T> T streamThumbnailContent(final Attachment attachment, final InputStreamConsumer<T> consumer)
            throws IOException
    {
        final File thumbnailFile = thumbnailAccessor.getThumbnailFile(attachment);
        InputStream inputStream = new FileInputStream(thumbnailFile);
        try
        {
            inputStream = new BufferedInputStream(inputStream);
            return consumer.withInputStream(inputStream);
        }
        finally
        {
            closeQuietly(inputStream);
        }
    }

    private Thumber getThumber()
    {
        return thumberFactory.getInstance();
    }

    private Thumbnail doGetThumbnail(final Issue issue, final Attachment attachment)
    {
        validState(isThumbnailable(issue, attachment),
                "Unable to create thumbnail image of attachment with id %d", attachment.getId());
        final File thumbnailFile = issue == null ?
                thumbnailAccessor.getThumbnailFile(attachment) : thumbnailAccessor.getThumbnailFile(issue, attachment);

        final int maxWidth = thumbnailConfiguration.getMaxWidth();
        final int maxHeight = thumbnailConfiguration.getMaxHeight();

        final Thumbnail existingThumbnail = readThumbnail(attachment, thumbnailFile);
        if (existingThumbnail != null)
        {
            return existingThumbnail;
        }        

        /*
            It's unlikely two threads will try to create the same thumbnail at once; it's sufficiently cluster-safe
            for them each to write to a temp file in the destination directory and rename it when finished.
         */
        final File tempThumbnailFile = getTempFile(thumbnailFile);
        final Thumbnail thumbnail = createThumbnail(attachment, tempThumbnailFile, maxWidth, maxHeight);
        if (thumbnail instanceof BrokenThumbnail)
        {
            // We couldn't generate the thumbnail - no need to rename or delete any files
            return thumbnail;
        }
        if (thumbnailFile.exists())
        {
            // Another thread got there first
            deleteQuietly(tempThumbnailFile);
        }
        else
        {
            // We won the race to create the thumbnail; did we also win the race to rename the temp file?
            final boolean renamed = tempThumbnailFile.renameTo(thumbnailFile);
            validState(renamed || thumbnailFile.exists(), "Could not rename '%s' to '%s'",
                    tempThumbnailFile.getAbsolutePath(), thumbnailFile.getAbsolutePath());
        }
        return new Thumbnail(thumbnail.getHeight(), thumbnail.getWidth(), thumbnailFile.getName(), attachment.getId(),
                thumbnail.getMimeType());
    }

    private Thumbnail createThumbnail(final Attachment attachment, final File thumbnailFile, final int maxWidth,
            final int maxHeight)
    {
        try
        {
            // check that the thumbnail file can be created, otherwise ImageIO will get NullPointerExceptions
            // see also http://bugs.sun.com/view_bug.do?bug_id=5034864
            touch(thumbnailFile);
            deleteQuietly(thumbnailFile);

            // If they don't fit within a sensible image size, we use a streamable thumbnail.
            // The image quality is not as good, but the memory impact is limited.
            final Dimensions originalImageDimensions = imageDimensions(attachment);
            if (!rasterBasedRenderingThreshold.apply(originalImageDimensions))
            {
                LOG.debug("Image dimensions exceed the threshold for raster based image manipulation. Using stream based renderer.");
                try
                {
                    return generateWithStreamRenderer(attachment, thumbnailFile, maxWidth, maxHeight);
                }
                catch (CMMException cme)
                {
                    // We don't use the JAI based fallback renderer if the image is too large.
                    LOG.warn("Attachment image is very large and contains color information that JIRA can't handle. Attachment ID is : '" + attachment.getId() + "'");
                    return new BrokenThumbnail(attachment.getId());
                }
            }

            // Render using the raster based renderers...
            try
            {
                return withStreamConsumer(attachment, new InputStreamConsumer<Thumbnail>()
                {
                    @Override
                    public Thumbnail withInputStream(final InputStream in) throws MalformedURLException
                    {
                        final Thumbnail thumbnail = getThumber().retrieveOrCreateThumbNail(in,
                                attachment.getFilename(), thumbnailFile, maxWidth, maxHeight, attachment.getId());
                        return fixup(thumbnail, thumbnailFile);
                    }
                });
            }
            catch (final CMMException ce)
            {
                // The JAI based renderer is only used for images that have embedded color profile information
                // that causes the Thumber to fail. We still want to use Thumber in general, as it's faster.
                LOG.debug("Failed to create thumbnail, delegating to JAI based thumbnail renderer: CMMException " +
                        ce.getLocalizedMessage()); // No need to dump the stack trace, we expect this to happen.
                return generateWithInMemoryJAIRenderer(attachment, thumbnailFile, maxWidth, maxHeight);
            }
        }
        catch (Exception e)
        {
            LOG.warn("Error writing to thumbnail file: " + thumbnailFile, e);
            return new BrokenThumbnail(attachment.getId());
        }
    }

    private Thumbnail readThumbnail(@Nonnull final Attachment attachment, @Nonnull final File thumbnailFile)
    {
        if (thumbnailFile.exists())
        {
            LOG.debug("Thumbnail file '" + thumbnailFile + "' already exists. Returning existing thumbnail.");
            try
            {
                final BufferedImage image = ImageIO.read(thumbnailFile);
                if (image != null)
                {
                    return new Thumbnail(image.getHeight(), image.getWidth(), thumbnailFile.getName(), attachment.getId(), PNG);
                }
                LOG.warn("Unable to read image data from existing thumbnail file '" + thumbnailFile + "'. Deleting this thumbnail.");
                deleteQuietly(thumbnailFile);
            }
            catch (IOException ioe)
            {
                LOG.warn("Unable to render existing thumbnail file: " + thumbnailFile, ioe);
                return new BrokenThumbnail(attachment.getId());
            }
        }
        return null;
    }

    private Thumbnail fixup(@Nullable final Thumbnail thumbnail, final File thumbnailFile)
    {
        if (thumbnail == null)
        {
            return null;
        }

        // The core thumber code has a behaviour that causes it to name the thumbnail differently
        // depending on whether the file exists or not, so we ensure it's always the file name we expect.
        return new Thumbnail(thumbnail.getHeight(), thumbnail.getWidth(), thumbnailFile.getName(),
                thumbnail.getAttachmentId(), thumbnail.getMimeType());
    }

    /**
     * Generate the thumbnail for the given {@link Attachment} using the raster based (in memory) renderer.
     *
     * @param attachment The attachment containing the image data
     * @param thumbnailFile The file to use for the thumbnail
     * @param maxWidth Maximum width of the thumbnail
     * @param maxHeight Maximum height of the thumbnail
     * @return Thumbnail that does not exceed the given maxWidth or maxHeight
     */
    private Thumbnail generateWithInMemoryJAIRenderer(final Attachment attachment,
            final File thumbnailFile,
            final int maxWidth,
            final int maxHeight) throws IOException
    {
        return withStreamConsumer(attachment, new InputStreamConsumer<Thumbnail>()
        {
            @Override
            public Thumbnail withInputStream(InputStream is) throws IOException
            {
                JAI_MESSAGE_LOG.warn("The first time we call the JAI library it may fail to find the native library implementation."
                        + " The following output is harmless but unpreventable and hence this precending log message.");
                final Dimensions d = jaiImageRenderer.renderThumbnail(is, thumbnailFile, maxWidth, maxHeight);
                return new Thumbnail(d.getHeight(), d.getWidth(), thumbnailFile.getName(), attachment.getId(), PNG);
            }
        });
    }

    /**
     * Generate the thumbnail for the given {@link Attachment} using the stream based renderer.
     *
     * @param attachment The attachment containing the image data
     * @param thumbnailFile The file to use for the thumbnail
     * @param maxWidth Maximum width of the thumbnail
     * @param maxHeight Maximum height of the thumbnail
     * @return Thumbnail that does not exceed the given maxWidth or maxHeight
     */
    private Thumbnail generateWithStreamRenderer(
            final Attachment attachment, final File thumbnailFile, final int maxWidth, final int maxHeight)
            throws IOException
    {
        return withStreamConsumer(attachment, new InputStreamConsumer<Thumbnail>()
        {
            @Override
            public Thumbnail withInputStream(final InputStream in) throws IOException
            {
                final Dimensions d = streamingImageRenderer.renderThumbnail(in, thumbnailFile, maxWidth, maxHeight);
                return new Thumbnail(d.getHeight(), d.getWidth(), thumbnailFile.getName(), attachment.getId(), PNG);
            }
        });
    }

    /**
     * Determine the dimensions (width/height) of the original image.
     *
     * @param attachment the attachment in play
     * @return true if we attempt to render the image, false otherwise.
     */
    private Dimensions imageDimensions(final Attachment attachment) throws IOException
    {
        return withStreamConsumer(attachment, new InputStreamConsumer<Dimensions>()
        {
            @Override
            public Dimensions withInputStream(final InputStream is) throws IOException
            {
                return new ImageDimensionsHelper().dimensionsForImage(ImageIO.createImageInputStream(is));
            }
        });
    }

    /**
     * Call the {@link InputStreamConsumer} with the attachment data input stream ensuring that the input stream gets
     * closed properly afterwards.
     *
     * @param attachment The attachment containing the image data
     * @param sc The InputStreamConsumer that consumes the attachment data {@link InputStream}
     * @return what the sc returns
     */
    private <T> T withStreamConsumer(final Attachment attachment, final InputStreamConsumer<T> sc) throws IOException
    {
        return attachmentManager.streamAttachmentContent(attachment, sc);
    }

    /**
     * Image renderer based on the JAI (Java Advanced Imaging) API. Used as a fallback renderer if
     * the original image contains colour profile information that causes a CMMException (CONF-21418).
     */
    @VisibleForTesting
    static class JAIImageRenderer
    {
        public Dimensions renderThumbnail(
                final InputStream inputStream, final File thumbnailFile, final int maxWidth, final int maxHeight)
            throws IOException
        {
            OutputStream thumbnailOutputStream = null;
            try
            {
                thumbnailOutputStream = new BufferedOutputStream(new FileOutputStream(thumbnailFile));
                return scale(inputStream, thumbnailOutputStream, maxWidth, maxHeight);
            }
            catch (FileNotFoundException e)
            {
                throw new RuntimeException(e);
            }
            finally
            {
                closeQuietly(thumbnailOutputStream);
            }
        }

        private Dimensions scale(
                final InputStream inputStream, final OutputStream thumbnail, final int maxWidth, final int maxHeight)
        {
            RenderedImage image = loadImage(inputStream);
            final Thumber.WidthHeightHelper wh = new Thumber().determineScaleSize(maxWidth, maxHeight, image.getWidth(), image.getHeight());
            final double scale = (wh.getWidth() / (double) image.getWidth());
            final ParameterBlock pb = new ParameterBlock();
            pb.addSource(image);
            pb.add(scale); // x scale factor
            pb.add(scale); // y scale factor
            pb.add(0.0F); // x translate
            pb.add(0.0F); // y translate
            pb.add(image);
            final RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            image = JAI.create("SubsampleAverage", pb, qualityHints);
            JAI.create("encode", image, thumbnail, "PNG");
            return new Dimensions(image.getWidth(), image.getHeight());
        }

        /**
         * Load the image using the given {@code inputStream}.
         * <p/>
         * This method doesn't close the InputStream!
         *
         * @param inputStream Original image
         * @return RenderedImage
         */
        private RenderedImage loadImage(final InputStream inputStream)
        {
            final SeekableStream s = SeekableStream.wrapInputStream(inputStream, true);
            final RenderedOp img = JAI.create("stream", s);
            ((OpImage) img.getRendering()).setTileCache(null); // We don't want to cache image tiles in memory.
            return img;
        }
    }

    /**
     * Helper class that uses an {@link ImageReader} to determine the width and height of an image.
     * <p/>
     * Doesn't rasterize the whole image and works well even with very large images (e.g. 15,000 x 15,000 px).
     */
    private static class ImageDimensionsHelper
    {
        public Dimensions dimensionsForImage(final ImageInputStream inputStream) throws IOException
        {
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(inputStream);
            if (!readers.hasNext())
            {
                throw new IOException("There is no ImageReader available for the given ImageInputStream");
            }
            // Use the first one available, it will instantiate ImageReader which needs to be disposed
            final ImageReader reader = readers.next();
            try {
                reader.setInput(inputStream);
                return new Dimensions(reader.getWidth(0), reader.getHeight(0));
            }
            finally {
                reader.dispose();
            }
        }
    }
}
