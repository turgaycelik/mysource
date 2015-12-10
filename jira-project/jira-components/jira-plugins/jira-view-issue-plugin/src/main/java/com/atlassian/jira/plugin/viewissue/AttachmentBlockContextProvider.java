package com.atlassian.jira.plugin.viewissue;

import com.atlassian.core.util.FileSize;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.attachment.FileNameBasedVersionedAttachmentsList;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.attachment.AttachmentCreationDateComparator;
import com.atlassian.jira.issue.attachment.AttachmentFileNameCreationDateComparator;
import com.atlassian.jira.issue.attachment.AttachmentItem;
import com.atlassian.jira.issue.attachment.AttachmentItems;
import com.atlassian.jira.issue.attachment.AttachmentZipKit;
import com.atlassian.jira.issue.attachment.AttachmentsCategoriser;
import com.atlassian.jira.issue.attachment.MimetypesFileTypeMap;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.atlassian.jira.issue.thumbnail.ThumbnailedImage;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.AttachmentUtils;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.http.JiraUrl;
import com.atlassian.jira.web.bean.NonZipExpandableExtensions;
import com.atlassian.jira.web.util.FileIconBean;
import com.atlassian.jira.web.util.FileIconUtil;
import com.atlassian.plugin.PluginParseException;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import static com.atlassian.jira.datetime.DateTimeStyle.COMPLETE;

/**
 * Provides context for the Attachments block on the View Issue page.
 *
 * @since v5.0
 */
public class AttachmentBlockContextProvider implements CacheableContextProvider
{
    private static final Set WELL_KNOWN_MIMETYPES = new LinkedHashSet();

    private final AttachmentManager attachmentManager;
    private final JiraAuthenticationContext authenticationContext;
    private final ThumbnailManager thumbnailManager;
    private final UserManager userManager;
    private final NonZipExpandableExtensions nonZipExpandableExtensions;
    private final FileIconUtil fileIconUtil;
    private final DateTimeFormatter dateTimeFormatter;
    private final AttachmentZipKit attachmentZipKit;
    private final AttachmentBlockContextHelper helper;

    static {
        WELL_KNOWN_MIMETYPES.add("application/pdf");
        WELL_KNOWN_MIMETYPES.add("application/msword");
    }

    public AttachmentBlockContextProvider(AttachmentManager attachmentManager, JiraAuthenticationContext authenticationContext,
            ThumbnailManager thumbnailManager, UserManager userManager, FileIconUtil fileIconUtil, DateTimeFormatter dateTimeFormatter,
            AttachmentBlockContextHelper helper)
    {
        this.attachmentManager = attachmentManager;
        this.authenticationContext = authenticationContext;
        this.thumbnailManager = thumbnailManager;
        this.userManager = userManager;
        this.nonZipExpandableExtensions = ComponentAccessor.getComponent(NonZipExpandableExtensions.class);
        this.fileIconUtil = fileIconUtil;
        this.dateTimeFormatter = dateTimeFormatter != null ? dateTimeFormatter.forLoggedInUser().withStyle(COMPLETE) : null;
        attachmentZipKit = new AttachmentZipKit();
        this.helper = helper;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final MapBuilder<String, Object> paramsBuilder = MapBuilder.newBuilder(context);

        final Issue issue = (Issue) context.get("issue");
        ApplicationUser user = authenticationContext.getUser();

        final boolean zipEnabled = helper.getZipSupport();

        AttachmentsCategoriser attachments = new AttachmentsCategoriser(thumbnailManager, new AttachmentsCategoriser.Source()
        {
            @Override
            public List<Attachment> getAttachments()
            {
                return attachmentManager.getAttachments(issue, attachmentComparator());
            }
        });

        paramsBuilder.add("iconGenerator", new IconGenerator());
        paramsBuilder.add("fileSizeFormatter", new FileSize());
        paramsBuilder.add("hasAttachments", !attachments.items().isEmpty());
        paramsBuilder.add("openSquareBracket", JiraUrlCodec.encode("["));
        paramsBuilder.add("closeSquareBracket", JiraUrlCodec.encode("]"));
        paramsBuilder.add("imageAttachments", convertToSimpleAttachments(issue, attachments.itemsThatHaveThumbs(), zipEnabled, user));
        paramsBuilder.add("fileAttachments", convertToSimpleAttachments(issue, attachments.itemsThatDoNotHaveThumbs(), zipEnabled, user));
        paramsBuilder.add("maximumNumberOfZipEntriesToShow", helper.getMaximumNumberOfZipEntriesToShow());
        paramsBuilder.add("fullBaseUrl", JiraUrl.constructBaseUrl(getRequest(context)));

        return paramsBuilder.toMap();
    }

    /**
     * @return a Comparator&lt;Attachment&gt; according to the user's selection.
     */
    protected Comparator<Attachment> attachmentComparator()
    {
        final String attachmentSortBy = helper.getAttachmentSortBy();
        final String attachmentOrder = helper.getAttachmentOrder();

        Comparator<Attachment> attachmentComparator;
        if (AttachmentBlockContextHelper.SORTBY_DATE_TIME.equals(attachmentSortBy))
        {
            attachmentComparator = new AttachmentCreationDateComparator();
        }
        else
        {
            attachmentComparator = new AttachmentFileNameCreationDateComparator(authenticationContext.getLocale());
        }

        if (AttachmentBlockContextHelper.ORDER_DESC.equals(attachmentOrder))
        {
            attachmentComparator = Collections.reverseOrder(attachmentComparator);
        }

        return attachmentComparator;
    }

    private List<SimpleAttachment> convertToSimpleAttachments(Issue issue, AttachmentItems items, boolean zipEnabled, ApplicationUser user)
    {
        final FileNameBasedVersionedAttachmentsList attachmentsList = new FileNameBasedVersionedAttachmentsList(items.attachments());
        final CollectionBuilder<SimpleAttachment> builder = CollectionBuilder.newBuilder();

        for (AttachmentItem item : items)
        {
            boolean latestVersion = attachmentsList.isLatestVersion(item.attachment());
            boolean shouldExpandAsZip = false;
            if (zipEnabled)
            {
                shouldExpandAsZip = shouldExpandAsZip(issue, item.attachment());
            }
            boolean canDelete = helper.canDeleteAttachment(issue, item.attachment(), user);
            builder.add(new SimpleAttachment(item.attachment(), latestVersion, shouldExpandAsZip, thumbnailManager.toThumbnailedImage(item.thumbnail()), canDelete));
        }

        return builder.asList();
    }

    /**
     * Determines whether the specified attachment should be expanded as a zip file. Files are expanded if zip support
     * is on, the file extension is not one of the extensions specified by {@link com.atlassian.jira.web.bean.NonZipExpandableExtensions}
     * and if the file represents a valid zip file.
     *
     * For performance reasons we only sniff "application/??" mime-types and exclude some common mime-types known not to be zips.
     *
     * TODO: All this logic should really live in the manager.
     *
     * @param attachment The attachment in play.
     * @return true if the the specified attachment should be expanded as a zip file; otherwise, false is returned.
     */
    private boolean shouldExpandAsZip(Issue issue, Attachment attachment)
    {
        // The attachment object may know this already, as once we have worked it out we store it in the database.
        if (attachment.isZip() != null)
        {
            return attachment.isZip().booleanValue();
        }

        final String mimetype = attachment.getMimetype();
        boolean isZip = false;
        if (mimetype.startsWith("application"))
        {
            final String attachmentExtension = FilenameUtils.getExtension(attachment.getFilename());
            if (nonZipExpandableExtensions.contains(attachmentExtension) || WELL_KNOWN_MIMETYPES.contains(mimetype))
            {
                isZip = false;
            }
            else
            {
                final File attachmentFile = AttachmentUtils.getAttachmentFile(issue, attachment);
                isZip = attachmentZipKit.isZip(attachmentFile);
            }
        }
        // Now we have worked out this is a zip file save that knowledge.
        attachmentManager.setZip(attachment, isZip);
        return isZip;
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        final Issue issue = (Issue) context.get("issue");
        final User user = authenticationContext.getLoggedInUser();

        return issue.getId() + "/" + (user == null ? "" : user.getName());
    }

    public class SimpleAttachment
    {
        private final Attachment attachment;
        private final boolean isLatest;
        private final boolean exapandAsZip;
        private final ThumbnailedImage thumbnail;
        private boolean canDelete;
        private AttachmentZipKit.AttachmentZipEntries attachmentZipEntries;

        public SimpleAttachment(Attachment attachment, boolean latest, boolean exapandAsZip, ThumbnailedImage thumbnail, boolean canDelete)
        {
            this.attachment = attachment;
            isLatest = latest;
            this.exapandAsZip = exapandAsZip;
            this.thumbnail = thumbnail;
            this.canDelete = canDelete;
        }

        public boolean isLatest()
        {
            return isLatest;
        }

        public Long getId()
        {
            return attachment.getId();
        }

        public String getMimetype()
        {
            return MimetypesFileTypeMap.getContentType(getFilename());
        }

        public String getFilename()
        {
            return attachment.getFilename();
        }

        public String getFilenameUrlEncoded()
        {
            return JiraUrlCodec.encode(attachment.getFilename(), true);
        }

        public String getCreatedFormatted()
        {
            return dateTimeFormatter.format(attachment.getCreated());
        }

        public String getCreatedIso8601()
        {
            return dateTimeFormatter.withStyle(DateTimeStyle.ISO_8601_DATE_TIME).format(attachment.getCreated());
        }

        public String getFilesize()
        {
            return FileSize.format(attachment.getFilesize());
        }

        public String getAuthor()
        {
            return attachment.getAuthorKey();
        }

        public String getDisplayAuthor()
        {
            final ApplicationUser user = userManager.getUserByKeyEvenWhenUnknown(attachment.getAuthorKey());
            return user != null ? user.getDisplayName() : null;

        }

        public boolean isExpandAsZip()
        {
            return exapandAsZip;
        }

        public ThumbnailedImage getThumbnail()
        {
            return thumbnail;
        }

        public boolean isCanDelete()
        {
            return canDelete;
        }

        /**
         * <p>Returns a list of zip entries for the specified attachment. The number of entries returned is limited to
         * the value of MAX_ZIP_ENTRIES.</p> <p/> <p>It is assumed that this attachment represents a valid zip file.</p>
         *
         * @return A {@link java.util.List} of {@link com.atlassian.jira.issue.attachment.AttachmentZipKit.AttachmentZipEntry}
         *         for the specified attachment. Limited to {@link APKeys#JIRA_ATTACHMENT_NUMBER_OF_ZIP_ENTRIES_TO_SHOW}.
         */
        public AttachmentZipKit.AttachmentZipEntries getZipEntries()
        {
            if (attachmentZipEntries == null)
            {
                try
                {
                    File attachmentFile = AttachmentUtils.getAttachmentFile(attachment);
                    attachmentZipEntries = attachmentZipKit.listEntries(attachmentFile, helper.getMaximumNumberOfZipEntriesToShow(), AttachmentZipKit.FileCriteria.ONLY_FILES);
                    return attachmentZipEntries;
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
            return attachmentZipEntries;
        }

    }

    public class IconGenerator
    {
        public SimpleIcon getIcon(SimpleAttachment attachment)
        {
            final FileIconBean.FileIcon fileIcon = fileIconUtil.getFileIcon(attachment.getFilename(), attachment.getMimetype());
            return new SimpleIcon(fileIcon == null ? "file.gif" : fileIcon.getIcon(), fileIcon == null ? "File" : fileIcon.getAltText());

        }

        public SimpleIcon getIcon(AttachmentZipKit.AttachmentZipEntry zipEntry)
        {
            final FileIconBean.FileIcon fileIcon = fileIconUtil.getFileIcon(zipEntry.getName(), null);
            return new SimpleIcon(fileIcon == null ? "file.gif" : fileIcon.getIcon(), fileIcon == null ? "File" : fileIcon.getAltText());

        }

        public class SimpleIcon
        {
            private final String icon;
            private final String altText;

            public SimpleIcon(String icon, String altText)
            {
                this.icon = icon;
                this.altText = altText;
            }

            public String getIcon()
            {
                return icon;
            }

            public String getAltText()
            {
                return altText;
            }
        }
    }

    private static HttpServletRequest getRequest(Map<String, Object> context)
    {
        return ((JiraHelper) context.get(JiraWebInterfaceManager.CONTEXT_KEY_HELPER)).getRequest();
    }

}
