package com.atlassian.jira.issue.managers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.exception.AttachmentNotFoundException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.attachment.AttachmentConstants;
import com.atlassian.jira.issue.attachment.AttachmentMoveException;
import com.atlassian.jira.issue.attachment.AttachmentStore;
import com.atlassian.jira.issue.attachment.CreateAttachmentParamsBean;
import com.atlassian.jira.issue.attachment.TemporaryAttachment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.AttachmentUtils;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.io.InputStreamConsumer;
import com.atlassian.jira.util.mime.MimeManager;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.action.issue.TemporaryAttachmentsMonitor;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.util.concurrent.Effect;
import com.atlassian.util.concurrent.Function;
import com.atlassian.util.concurrent.Promise;

import com.opensymphony.module.propertyset.PropertySet;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;

import webwork.action.ServletActionContext;

import static com.atlassian.core.util.WebRequestUtils.MACOSX;
import static com.atlassian.core.util.WebRequestUtils.WINDOWS;
import static com.atlassian.core.util.WebRequestUtils.getBrowserOperationSystem;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultAttachmentManager implements AttachmentManager
{
    private static final Logger log = Logger.getLogger(DefaultAttachmentManager.class);
    private static final int NOT_THUMBNAILABLE = 0;
    private static final int IS_THUMBNAILABLE = 1;
    private static final int NOT_ZIP = 0;
    private static final int IS_ZIP = 1;

    private final IssueManager issueManager;
    private final OfBizDelegator ofBizDelegator;
    private final MimeManager mimeManager;
    private final ApplicationProperties applicationProperties;
    private final AttachmentPathManager attachmentPathManager;
    private final ComponentLocator componentLocator;
    private final I18nBean.BeanFactory i18nBeanFactory;
    private final UserManager userManager;
    private final AttachmentStore attachmentStore;

    public DefaultAttachmentManager(final IssueManager issueManager, final OfBizDelegator ofBizDelegator,
            final MimeManager mimeManager, final ApplicationProperties applicationProperties,
            final AttachmentPathManager attachmentPathManager, final ComponentLocator componentLocator,
            final I18nHelper.BeanFactory i18nBeanFactory,
            final UserManager userManager, final AttachmentStore attachmentStore)
    {
        this.issueManager = issueManager;
        this.ofBizDelegator = ofBizDelegator;
        this.mimeManager = mimeManager;
        this.applicationProperties = applicationProperties;
        this.attachmentPathManager = attachmentPathManager;
        this.componentLocator = componentLocator;
        this.i18nBeanFactory = i18nBeanFactory;
        this.userManager = userManager;
        this.attachmentStore = attachmentStore;
    }

    @Override
    public Attachment getAttachment(Long id)
    {
        GenericValue attachmentGV;
        try
        {
            attachmentGV = ofBizDelegator.findById(AttachmentConstants.ATTACHMENT_ENTITY_NAME, id);
        }
        catch (DataAccessException e)
        {
            log.error("Unable to find a file attachment with id: " + id);
            throw e;
        }
        if (attachmentGV == null) { throw new AttachmentNotFoundException(id); }
        return new Attachment(issueManager, attachmentGV, OFBizPropertyUtils.getPropertySet(attachmentGV));
    }

    @Override
    public List<Attachment> getAttachments(Issue issue)
    {
        return getStoredAttachments(issue);
    }

    public List<Attachment> getStoredAttachments(Issue issue)
    {
        try
        {
            GenericValue issueGV = issue.getGenericValue();
            Collection<GenericValue> attachmentGvs = issueGV.getRelatedOrderBy("ChildFileAttachment", EasyList.build("filename ASC", "created DESC"));
            List<Attachment> attachments = new ArrayList<Attachment>(attachmentGvs.size());
            for (GenericValue attachmentGV : attachmentGvs)
            {
                attachments.add(new Attachment(issueManager, attachmentGV, OFBizPropertyUtils.getPropertySet(attachmentGV)));
            }
            return attachments;
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    private List<Attachment> getStoredAttachments(Issue issue, Comparator<? super Attachment> comparator)
    {
        List<Attachment> attachments = getStoredAttachments(issue);
        Collections.sort(attachments, comparator);
        return attachments;
    }

    @Override
    public List<Attachment> getAttachments(Issue issue, Comparator<? super Attachment> comparator)
    {
        return getStoredAttachments(issue, comparator);
    }

    @Override
    public Attachment createAttachmentCopySourceFile(final File file, final String filename, final String contentType, final String attachmentAuthor, final Issue issue, final Map<String, Object> attachmentProperties, final Date createdTime)
            throws AttachmentException
    {
        return createAttachment(file, filename, contentType, userManager.getUserByName(attachmentAuthor), issue, null, null, attachmentProperties, createdTime, true);
    }

    @Override
    public Attachment createAttachment(GenericValue issue, User author, String mimetype, String filename, Long filesize, Map<String, Object> attachmentProperties, Date createdTime)
    {
        return insertAttachmentWithAuthorKey(issue.getLong("id"), ApplicationUsers.getKeyFor(author), mimetype, filename, filesize, null, null, attachmentProperties, createdTime);
    }

    private Attachment insertAttachmentWithAuthorKey(Long issueId, String authorKey, String mimetype, String filename, Long filesize, Boolean zip, Boolean thumbnailable, Map<String, Object> attachmentProperties, Date createdTime)
    {
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("issue", issueId);
        fields.put("author", authorKey);
        fields.put("mimetype", mimetype);
        fields.put("filename", filename);
        fields.put("filesize", filesize);
        if (zip != null)
        {
            fields.put("zip", zip ? IS_ZIP : NOT_ZIP);
        }
        if (thumbnailable != null)
        {
            fields.put("thumbnailable", thumbnailable ? IS_THUMBNAILABLE : NOT_THUMBNAILABLE);
        }
        fields.put("created", createdTime);

        GenericValue attachmentGV = ofBizDelegator.createValue(AttachmentConstants.ATTACHMENT_ENTITY_NAME, fields);

        if (attachmentProperties != null)
        {
            PropertySet propSet = createAttachmentPropertySet(attachmentGV, attachmentProperties);
            return new Attachment(issueManager, attachmentGV, propSet);
        }
        else
        {
            return new Attachment(issueManager, attachmentGV);
        }
    }

    @Override
    public void deleteAttachment(final Attachment attachment) throws RemoveException
    {
        attachmentStore.deleteAttachment(attachment).done(new Effect<Void>()
        {
            @Override
            public void apply(final Void aVoid)
            {
                ofBizDelegator.removeAll(CollectionBuilder.list(attachment.getGenericValue()));
            }
        }).claim();
    }

    @Override
    public void deleteAttachmentDirectory(Issue issue) throws RemoveException
    {
        deleteAttachmentDirectory(issue.getGenericValue());
    }

    IssueFactory getIssueFactory()
    {
        return componentLocator.getComponent(IssueFactory.class);
    }

    private void deleteAttachmentDirectory(GenericValue issue) throws RemoveException
    {
        if (issue != null && attachmentsAllowedAndDirectoryIsSet())
        {
            File attachmentDir = attachmentStore.getAttachmentDirectory(getIssueFactory().getIssue(issue));
            if (!attachmentDir.isDirectory())
            { throw new RemoveException("Attachment path '" + attachmentDir + "' is not a directory"); }
            if (!attachmentDir.canWrite())
            { throw new RemoveException("Can't write to attachment directory '" + attachmentDir + "'"); }

            // Remove the /thumbs/ subdirectory if required.
            final File thumbnailDirectory = new File(attachmentDir, AttachmentUtils.THUMBS_SUBDIR);
            if (thumbnailDirectory.exists())
            {
                // We want to delete it.
                if (thumbnailDirectory.listFiles().length == 0)
                {
                    boolean deleted = thumbnailDirectory.delete();
                    if (!deleted)
                    {
                        log.error("Unable to delete the issue attachment thumbnail directory '" + thumbnailDirectory + "'.");
                    }
                }
                else
                {
                    for (File file : thumbnailDirectory.listFiles())
                    {
                        System.out.println("file = " + file);
                    }
                    log.error("Unable to delete the issue attachment thumbnail directory '" + thumbnailDirectory + "' because it is not empty.");
                }
            }

            if (attachmentDir.listFiles().length == 0)
            {
                if (!attachmentDir.delete())
                {
                    log.error("Unable to delete the issue attachment directory '" + attachmentDir + "'.");
                }
            }
            else
            {
                log.error("Unable to delete the issue attachment directory '" + attachmentDir + "' because it is not empty.");
            }
        }
    }

    private boolean attachmentsAllowedAndDirectoryIsSet()
    {
        String attachmentDir = attachmentPathManager.getAttachmentPath();
        return applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS) && StringUtils.isNotBlank(attachmentDir);
    }

    @Override
    public boolean attachmentsEnabled()
    {
        boolean allowAttachments = applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS);
        boolean attachmentPathSet = StringUtils.isNotBlank(attachmentPathManager.getAttachmentPath());
        return allowAttachments && attachmentPathSet;
    }

    @Override
    public boolean isScreenshotAppletEnabled()
    {
        return applicationProperties.getOption(APKeys.JIRA_SCREENSHOTAPPLET_ENABLED);
    }

    protected boolean isScreenshotAppletEnabledForLinux()
    {
        return applicationProperties.getOption(APKeys.JIRA_SCREENSHOTAPPLET_LINUX_ENABLED);
    }

    @Override
    public boolean isScreenshotAppletSupportedByOS()
    {
        if (isScreenshotAppletEnabledForLinux())
        {
            // means all OS are supported so just return true.
            return true;
        }

        // Linux is still flakey
        int browserOS = getUsersOS();
        return (browserOS == WINDOWS || browserOS == MACOSX);
    }

    @Override
    public List<ChangeItemBean> convertTemporaryAttachments(final User user, final Issue issue, final List<Long> selectedAttachments, final TemporaryAttachmentsMonitor temporaryAttachmentsMonitor)
            throws AttachmentException
    {
        notNull("issue", issue);
        notNull("selectedAttachments", selectedAttachments);
        notNull("temporaryAttachmentsMonitor", temporaryAttachmentsMonitor);

        final List<ChangeItemBean> ret = new ArrayList<ChangeItemBean>();
        for (final Long selectedAttachment : selectedAttachments)
        {
            final TemporaryAttachment tempAttachment = temporaryAttachmentsMonitor.getById(selectedAttachment);
            final ChangeItemBean cib = createAttachment(tempAttachment.getFile(), tempAttachment.getFilename(), tempAttachment.getContentType(), user, issue, Collections.<String, Object>emptyMap(), UtilDateTime.nowTimestamp());
            if (cib != null)
            {
                ret.add(cib);
            }
        }

        return ret;
    }

    int getUsersOS()
    {
        HttpServletRequest servletRequest = ExecutingHttpRequest.get();
        if (servletRequest == null)
        {
            servletRequest = ServletActionContext.getRequest();

        }
        return getBrowserOperationSystem(servletRequest);

    }

    @Override
    public ChangeItemBean createAttachment(File file, String filename, String contentType, User remoteUser, Issue issue, Map<String, Object> attachmentProperties, Date createdTime)
            throws AttachmentException
    {
        return createAttachment(file, filename, contentType, remoteUser, issue, null, null, attachmentProperties, createdTime);
    }

    @Override
    public ChangeItemBean createAttachment(File file, String filename, String contentType, User remoteUser, Issue issue, Boolean zip, Boolean thumbnailable, Map<String, Object> attachmentProperties, Date createdTime)
            throws AttachmentException
    {
        return createAttachmentBean(file, filename, contentType, ApplicationUsers.from(remoteUser), issue, zip, thumbnailable, attachmentProperties, createdTime, false);
    }

    /**
     * @param contentType The desired contentType.  This may be modified if a better alternative is suggested by {@link
     * MimeManager#getSanitisedMimeType(String, String)}
     * @param attachmentProperties String -> Object property map
     */
    @Override
    public ChangeItemBean createAttachment(File file, String filename, String contentType, User author, GenericValue issue, Map<String, Object> attachmentProperties, Date createdTime)
            throws AttachmentException
    {
        return createAttachment(file, filename, contentType, author, issue, null, null, attachmentProperties, createdTime);
    }

    /**
     * @param contentType The desired contentType.  This may be modified if a better alternative is suggested by {@link
     * MimeManager#getSanitisedMimeType(String, String)}
     * @param attachmentProperties String -> Object property map
     */
    @Override
    public ChangeItemBean createAttachment(File file, String filename, String contentType, User author, GenericValue issue, Boolean zip, Boolean thumbnailable, Map<String, Object> attachmentProperties, Date createdTime)
            throws AttachmentException
    {
        return createAttachment(file, filename, contentType, author, getIssueFactory().getIssue(issue), zip, thumbnailable, attachmentProperties, createdTime);
    }

    private ChangeItemBean createAttachmentBean(File file, String filename, String contentType, ApplicationUser author, Issue issue, Boolean zip, Boolean thumbnailable, Map<String, Object> attachmentProperties, Date createdTime, boolean copySourceFile)
            throws AttachmentException
    {
        Attachment attachment = createAttachment(file, filename, contentType, author, issue, zip, thumbnailable, attachmentProperties, createdTime, copySourceFile);
        if (attachment == null)
        {
            return null;
        }
        return new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Attachment", null, null, attachment.getId().toString(), filename);
    }

    private Attachment createAttachment(File file, String filename, String contentType, ApplicationUser author, Issue issue, Boolean zip, Boolean thumbnailable, Map<String, Object> attachmentProperties, Date createdTime, boolean copySourceFile)
            throws AttachmentException
    {
        if (file == null)
        {
            log.warn("Cannot create attachment without a file (filename=" + filename + ").");
            return null;
        }
        else if (filename == null)
        {
            // Perhaps we should just use the temporary filename instead of losing the attachment? These are all hacks anyway. We need to properly support multipart/{related,inline}
            log.warn("Cannot create attachment without a filename - inline content? See http://jira.atlassian.com/browse/JRA-10825 (file=" + file.getName() + ").");
            return null;
        }

        //get sanitised version of the mimeType.
        contentType = mimeManager.getSanitisedMimeType(contentType, filename);

        Attachment attachment = insertAttachmentWithAuthorKey(issue.getId(), ApplicationUsers.getKeyFor(author), contentType, filename, file.length(), zip, thumbnailable, attachmentProperties, createdTime);

        // create attachment on disk - copy or just move source file
        if (copySourceFile)
        {
            createAttachmentOnDiskCopySourceFile(attachment, file);
        }
        else
        {
            createAttachmentOnDisk(attachment, file, ApplicationUsers.toDirectoryUser(author));
        }

        return attachment;
    }

    @Override
    public ChangeItemBean createAttachment(File file, String filename, String contentType, User remoteUser, GenericValue issue)
            throws AttachmentException
    {
        return createAttachment(file, filename, contentType, remoteUser, issue, Collections.<String, Object>emptyMap(), UtilDateTime.nowTimestamp());
    }

    @Override
    public ChangeItemBean createAttachment(File file, String filename, String contentType, User remoteUser, Issue issue)
            throws AttachmentException
    {
        return createAttachment(file, filename, contentType, remoteUser, issue, Collections.<String, Object>emptyMap(), UtilDateTime.nowTimestamp());
    }

    @Override
    public ChangeItemBean createAttachment(CreateAttachmentParamsBean bean) throws AttachmentException
    {
        return createAttachmentBean(bean.getFile(), bean.getFilename(), bean.getContentType(), bean.getAuthor(), bean.getIssue(), bean.getZip(), bean.getThumbnailable(), bean.getAttachmentProperties(), bean.getCreatedTime(), bean.getCopySourceFile());
    }

    @Override
    public List<ChangeItemBean> convertTemporaryAttachments(ApplicationUser user, Issue issue, List<Long> selectedAttachments, TemporaryAttachmentsMonitor temporaryAttachmentsMonitor)
            throws AttachmentException
    {
        return convertTemporaryAttachments(ApplicationUsers.toDirectoryUser(user), issue, selectedAttachments, temporaryAttachmentsMonitor);
    }

    void createAttachmentOnDisk(Attachment attachment, final File file, User user) throws AttachmentException
    {
        try
        {
            attachmentStore.putAttachment(attachment, new BufferedInputStream(new FileInputStream(file))).claim();
            // This will throw some kind of AttachmentRuntimeException
        }
        catch (Exception e)
        {
            final String message =
                    i18nBeanFactory.getInstance(user).getText("attachfile.error.save.to.store", e);

            log.error(message, e);
            throw new AttachmentException(message, e);
        }
        finally
        {
            file.delete();
        }
    }

    void createAttachmentOnDiskCopySourceFile(Attachment attachment, File file) throws AttachmentException
    {
        try
        {
            InputStream is = new BufferedInputStream(new FileInputStream(file));
            Promise<Attachment> copyResult = attachmentStore.putAttachment(attachment, is);
            copyResult.claim();
        }
        catch (Exception e)
        {
            throw new AttachmentException(e);
        }
    }

    /**
     * Create attachment properties in the database.
     *
     * @param attachment the attachment whose property set we are meant to create.
     * @param attachmentProperties Map of String -> Object pairs
     * @return Returned map of {@link PropertySet}s.
     */
    PropertySet createAttachmentPropertySet(GenericValue attachment, Map<String, Object> attachmentProperties)
    {
        PropertySet propSet = OFBizPropertyUtils.getPropertySet(attachment);
        for (Map.Entry<String, Object> entry : attachmentProperties.entrySet())
        {
            propSet.setAsActualType(entry.getKey(), entry.getValue());
        }
        return propSet;
    }

    @Override
    public Attachment setThumbnailable(Attachment attachment, boolean thumbnailable)
    {
        GenericValue attachmentGV;
        try
        {
            attachmentGV = ofBizDelegator.findById(AttachmentConstants.ATTACHMENT_ENTITY_NAME, attachment.getId());
            attachmentGV.put("thumbnailable", thumbnailable ? IS_THUMBNAILABLE : NOT_THUMBNAILABLE);
            attachmentGV.store();
        }
        catch (DataAccessException e)
        {
            log.error("Unable to find a file attachment with id: " + attachment.getId());
            throw e;
        }
        catch (GenericEntityException e)
        {
            log.error("Unable to find a file attachment with id: " + attachment.getId());
            throw new DataAccessException(e);
        }
        return new Attachment(issueManager, attachmentGV, attachment.getProperties());
    }

    @Override
    public Attachment setZip(Attachment attachment, boolean zip)
    {
        GenericValue attachmentGV;
        try
        {
            attachmentGV = ofBizDelegator.findById(AttachmentConstants.ATTACHMENT_ENTITY_NAME, attachment.getId());
            attachmentGV.put("zip", zip ? IS_ZIP : NOT_ZIP);
            attachmentGV.store();
        }
        catch (DataAccessException e)
        {
            log.error("Unable to find a file attachment with id: " + attachment.getId());
            throw e;
        }
        catch (GenericEntityException e)
        {
            log.error("Unable to find a file attachment with id: " + attachment.getId());
            throw new DataAccessException(e);
        }
        return new Attachment(issueManager, attachmentGV, attachment.getProperties());
    }

    @Override
    public <T> T streamAttachmentContent(@Nonnull final Attachment attachment, final InputStreamConsumer<T> consumer)
    {
        return attachmentStore.getAttachment(attachment, new Function<InputStream, T>()
        {
            @Override
            public T get(final InputStream input)
            {
                try
                {
                    return consumer.withInputStream(input);
                }
                catch (IOException e)
                {
                    throw new DataAccessException(e);
                }
            }
        }).claim();
    }

    /**
     * Create a new directory for this issue, and move all the attachments from the old directory to the new directory.
     * <p/>
     * NB - this will fail if the old directory and new directory are on different filesystems as {@link File#renameTo}
     * fails across filesystems.
     *
     * @param oldIssue the issue we're moving attachments from
     * @param newIssueKey the new destination issue key
     */
    public void moveAttachments(final Issue oldIssue, final String newIssueKey)
    {
        // JRA-15475: reload the oldIssue here to ensure we get the latest state before trying to move attachments.  When
        // moving attachments it's important to get the source location right otherwise the attachment may 'disappear'.
        // The source location could be wrong if another move happens moving the same oldIssue as this one
        // which changes the source oldIssue's key and thus the source file path.
        // Note: This isn't really a 100% fix since another move could still enter after the oldIssue has been loaded from the DB here and before
        // we move the attachments, but it's a lot less likely to happen.
        // Also note that there is now validation at the beginning of the doExecute() method to fail the move operation
        // if the Issue was already moved by then.
        final List<Attachment> attachments = getAttachments(oldIssue);
        for (final Attachment attachment : attachments)
        {
            try
            {
                attachmentStore.move(attachment, newIssueKey).claim();
            }
            catch (AttachmentMoveException e)
            {
                log.warn(e.getMessage());
            }
        }
    }
}
