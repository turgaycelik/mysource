package com.atlassian.jira.issue.attachment;

import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.atlassian.jira.util.dbc.Assertions;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.ofbiz.core.entity.GenericEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;

import static com.google.common.collect.Lists.transform;

/**
 * Helper class for handling attachments on the view issue page. This class sorts attachments into two categories: <ul>
 * <li>attachments that have thumbnails</li> <li>attachments that do not have thumbnails</li> </ul>
 * <p/>
 * This is useful because of the different way in which these two types of attachments are displayed on the view issue
 * page. Note that an attachment may be "thumbnailable" but not have a thumbnail in practice, i.e. because there was an
 * error creating the thumbnail. This class cares not about theoretical thumbnailability, but only about actual
 * presence/absence of a thumbnail.
 *
 * @since v5.0
 */
public class AttachmentsCategoriser
{
    public interface Source
    {
        /**
         * @return a list of Attachment in the desired order.
         */
        List<Attachment> getAttachments();
    }

    /**
     * Logger for this AttachmentsCategoriser instance.
     */
    private static final Logger log = LoggerFactory.getLogger(AttachmentsCategoriser.class);

    /**
     * A Source used to get attachments from.
     */
    @Nonnull
    private final Source attachmentsSource;

    /**
     * A ThumbnailManager.
     */
    @Nonnull
    private final ThumbnailManager thumbnailManager;

    /**
     * Lazily loaded attachment list.
     */
    private AttachmentItems attachmentItems = null;

    /**
     * Creates a new AttachmentsCategoriser
     *
     * @param thumbnailManager a ThumbnailManager
     * @param attachmentsSource the Source used to get Attachments from
     */
    public AttachmentsCategoriser(ThumbnailManager thumbnailManager, Source attachmentsSource)
    {
        this.thumbnailManager = Assertions.notNull(thumbnailManager);
        this.attachmentsSource = Assertions.notNull(attachmentsSource);
    }

    /**
     * @return a AttachmentItems containing all the attachments for current issue
     */
    public AttachmentItems items()
    {
        if (attachmentItems == null)
        {
            attachmentItems = new AttachmentItems(transform(attachmentsSource.getAttachments(), new AttachmentItemCreator()));
        }

        return attachmentItems;
    }

    /**
     * @return as AttachmentItems containing all the attachments that have thumbnails
     */
    public AttachmentItems itemsThatHaveThumbs()
    {
        return new AttachmentItems(Iterables.filter(items(), new IfHasThumbnail(true)));
    }

    /**
     * @return as AttachmentItems containing all the attachments that do not have thumbnails
     */
    public AttachmentItems itemsThatDoNotHaveThumbs()
    {
        return new AttachmentItems(Iterables.filter(items(), new IfHasThumbnail(false)));
    }

    static class AttachmentGetter implements Function<AttachmentItem, Attachment>
    {
        @Override
        public Attachment apply(AttachmentItem item)
        {
            return item.attachment;
        }
    }

    static class IfHasThumbnail implements Predicate<AttachmentItem>
    {
        private final boolean hasThumbnail;

        public IfHasThumbnail(boolean hasThumbnail) {this.hasThumbnail = hasThumbnail;}

        @Override
        public boolean apply(AttachmentItem item)
        {
            return item.isThumbnailAvailable() == hasThumbnail;
        }
    }

    public class AttachmentItemCreator implements Function<Attachment, AttachmentItem>
    {
        @Override
        public AttachmentItem apply(Attachment attachment)
        {
            Thumbnail thumbnail = null;
            try
            {
                if (thumbnailManager.isThumbnailable(attachment))
                {
                    thumbnail = thumbnailManager.getThumbnail(attachment);
                }
            }
            catch (GenericEntityException e)
            {
                log.warn("Failed to get thumbnail for {}", attachment);
            }

            return new AttachmentItem(attachment, thumbnail);
        }
    }
}
