package com.atlassian.jira.web.util;

import com.atlassian.core.util.FileSize;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.fugue.Either;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.TemporaryAttachmentsMonitorLocator;
import com.atlassian.jira.issue.attachment.TemporaryAttachment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.AttachmentUtils;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.LimitedOutputStream;
import com.atlassian.jira.util.PathTraversalException;
import com.atlassian.jira.util.PathUtils;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.action.issue.TemporaryAttachmentsMonitor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;
import webwork.config.Configuration;
import webwork.multipart.MultiPartRequestWrapper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;

public class DefaultWebAttachmentManager implements WebAttachmentManager
{
    private static final Logger log = Logger.getLogger(DefaultWebAttachmentManager.class);

    private final FileNameCharacterCheckerUtil fileNameCharacterCheckerUtil;
    private final AttachmentManager attachmentManager;
    private final I18nHelper.BeanFactory beanFactory;
    private final JiraAuthenticationContext authenticationContext;
    private final TemporaryAttachmentsMonitorLocator locator;
    private final AttachmentService service;

    public DefaultWebAttachmentManager(final AttachmentManager attachmentManager, final I18nHelper.BeanFactory beanFactory,
            final JiraAuthenticationContext authenticationContext, TemporaryAttachmentsMonitorLocator locator,
            final AttachmentService service)
    {
        this.attachmentManager = attachmentManager;
        this.beanFactory = beanFactory;
        this.authenticationContext = authenticationContext;
        this.locator = locator;
        this.service = service;
        this.fileNameCharacterCheckerUtil = new FileNameCharacterCheckerUtil();
    }

    public ChangeItemBean createAttachment(final MultiPartRequestWrapper requestWrapper, final User remoteUser, final Issue issue, final String fileParamName, final Map<String, Object> attachmentProperties)
            throws AttachmentException
    {
        return createAttachment(requestWrapper, remoteUser, issue.getGenericValue(), fileParamName, attachmentProperties);
    }

    public ChangeItemBean createAttachment(final MultiPartRequestWrapper requestWrapper, final User remoteUser, final GenericValue issue, final String fileParamName, final Map<String, Object> attachmentProperties)
            throws AttachmentException
    {
        final File file = requestWrapper.getFile(fileParamName);
        if (file == null) //there's not much we can do here.
        {
            log.warn(format("Could not create attachment. No file found in MultiPartRequestWrapper. File param name: %s. Request wrapper filenames: %s.", fileParamName, CollectionBuilder.list(requestWrapper.getFileNames())));
            return null;
        }
        final String filename = requestWrapper.getFilesystemName(fileParamName);
        final String contentType = requestWrapper.getContentType(fileParamName);
        return attachmentManager.createAttachment(file, filename, contentType, remoteUser, issue, attachmentProperties, UtilDateTime.nowTimestamp());
    }

    public TemporaryAttachment createTemporaryAttachment(final MultiPartRequestWrapper requestWrapper, final String fileParamName,
            final Issue issue, final Project project, final String formToken) throws AttachmentException
    {
        if (issue == null && project == null)
        {
            throw new IllegalArgumentException("'issue' and 'project' cannot be null at the same time.");
        }

        //Is the mult-part request in a good state. This will also check the file size. We need to do this
        //first becasue the file will be null if the attachment is oversized.
        validateAttachmentIfExists(requestWrapper, fileParamName, false);

        final File file = requestWrapper.getFile(fileParamName);
        if (file == null) //there's not much we can do here.
        {
            log.warn(format("Could not create attachment. No file found in MultiPartRequestWrapper. File param name: %s. Request wrapper filenames: %s.", fileParamName, CollectionBuilder.list(requestWrapper.getFileNames())));
            return null;
        }

        //check permissions.
        assertCanAttach(issue, project);

        final String filename = requestWrapper.getFilesystemName(fileParamName);
        final String contentType = requestWrapper.getContentType(fileParamName);

        //create the temporary attachment file with the id prefixed to avoid name clashes when multiple people are
        //uploading files.
        try
        {
            final UniqueFile uniqueFile = createUniqueFile(file.getName());
            createTemporaryAttachmentOnDisk(file, uniqueFile.getFile());

            final TemporaryAttachment temporaryAttachment = new TemporaryAttachment(uniqueFile.getId(), uniqueFile.getFile(), filename, contentType, formToken);
            addToMonitor(temporaryAttachment);
            return temporaryAttachment;
        }
        catch (IOException e)
        {
            throw new AttachmentException(getI18n().getText("attachfile.error.io.error", fileParamName, e.getMessage()), e);
        }
        catch (PathTraversalException e)
        {
            throw new AttachmentException(getI18n().getText("attachfile.error.io.error", fileParamName, e.getMessage()), e);
        }
    }

    @Override
    public TemporaryAttachment createTemporaryAttachment(final MultiPartRequestWrapper requestWrapper, final String fileParamName, final Issue issue, final Project project)
            throws AttachmentException
    {
        return createTemporaryAttachment(requestWrapper, fileParamName, issue, project, getFormToken(issue, project));
    }

    @Override
    public TemporaryAttachment createTemporaryAttachment(InputStream stream, String fileName, String contentType,
            long size, Issue issue, Project project, String formToken) throws AttachmentException
    {
        Assertions.notBlank("fileName", fileName);
        Assertions.notBlank("contentType", contentType);
        Assertions.notNull("stream", stream);
        if (issue == null && project == null)
        {
            throw new IllegalArgumentException("'issue' and 'project' cannot be null at the same time.");
        }
        if (size < 0)
        {
            throw new IllegalArgumentException("size must be >= 0.");
        }

        if (size == 0)
        {
            throw new AttachmentException(getI18n().getText("attachfile.error.file.zero", fileName));
        }
        else
        {
            long maxAttachmentSize = getMaxAttachmentSize();
            if (size > maxAttachmentSize)
            {
                throw new AttachmentException(getI18n().getText("attachfile.error.file.large", fileName, FileSize.format(maxAttachmentSize)));
            }
        }
        assertCanAttach(issue, project);
        assertFileNameIsValid(fileName);
        UniqueFile uniqueFile = null;
        LimitedOutputStream limitedOutput = null;
        try
        {
            uniqueFile = createUniqueFile(fileName);
            FileOutputStream fos = new FileOutputStream(uniqueFile.getFile());
            limitedOutput = wrapOutputStream(fos, size);

            IOUtils.copy(stream, limitedOutput);

            //We want the close here. If we get an error flusing to the disk then we really need to know.
            limitedOutput.close();

            //This can only happen when the stream is too small. If the stream is too big we will get a
            //TooBigIOException which is caught below.
            if (limitedOutput.getCurrentLength() != size)
            {
                deleteFileIfExists(uniqueFile.getFile());
                String text;
                if (limitedOutput.getCurrentLength() == 0)
                {
                    text = getI18n().getText("attachfile.error.io.bad.size.zero", fileName);
                }
                else
                {
                    text = getI18n().getText("attachfile.error.io.bad.size", fileName,
                            String.valueOf(limitedOutput.getCurrentLength()), String.valueOf(size));
                }
                throw new AttachmentException(text);
            }

            final TemporaryAttachment temporaryAttachment = new TemporaryAttachment(uniqueFile.getId(), uniqueFile.getFile(), fileName, contentType, formToken);
            addToMonitor(temporaryAttachment);
            return temporaryAttachment;
        }
        catch (IOException e)
        {
            IOUtils.closeQuietly(limitedOutput);
            if(uniqueFile != null)
            {
                deleteFileIfExists(uniqueFile.getFile());
            }

            if (e instanceof LimitedOutputStream.TooBigIOException)
            {
                LimitedOutputStream.TooBigIOException tooBigIOException = (LimitedOutputStream.TooBigIOException) e;
                throw new AttachmentException(getI18n().getText("attachfile.error.file.large", fileName, FileSize.format(tooBigIOException.getNextSize())));
            }
            else
            {
                //JRADEV-5540: This is probably caused by some kind of client i/o error (e.g. disconnect). Not much point of logging it as we send
                // back an error reason anyways.
                log.debug("I/O error occured while attaching file.", e);
                throw new AttachmentException(getI18n().getText("attachfile.error.io.error", fileName, e.getMessage()), e);
            }
        }
        catch (PathTraversalException e)
        {
            throw new AttachmentException(getI18n().getText("attachfile.error.io.error", fileName, e.getMessage()), e);
        }
    }

    @Override
    public TemporaryAttachment createTemporaryAttachment(final InputStream stream, final String fileName, final String contentType, final long size, final Issue issue, final Project project)
            throws AttachmentException
    {
        return createTemporaryAttachment(stream, fileName, contentType, size, issue, project, getFormToken(issue, project));
    }

    public boolean validateAttachmentIfExists(final MultiPartRequestWrapper requestWrapper, final String fileParamName, final boolean required)
            throws AttachmentException
    {
        final File file = requestWrapper.getFile(fileParamName);
        final String filename = requestWrapper.getFilesystemName(fileParamName);
        final String contentType = requestWrapper.getContentType(fileParamName);
        return StringUtils.isNotBlank(contentType) && assertAttachmentIfExists(file, filename, required, requestWrapper.getContentLength());
    }

    private static void deleteFileIfExists(final File file)
    {
        if (file.exists() && !file.delete())
        {
            log.warn("Unable to delete file '" + file + "'.");
        }
    }

    void addToMonitor(TemporaryAttachment temporaryAttachment) throws AttachmentException
    {
        final TemporaryAttachmentsMonitor attachmentsMonitor = locator.get(true);
        if (attachmentsMonitor != null)
        {
            attachmentsMonitor.add(temporaryAttachment);
        }
        else
        {
            deleteFileIfExists(temporaryAttachment.getFile());
            throw new AttachmentException(getI18n().getText("attachfile.error.session.error", temporaryAttachment.getFilename()));
        }
    }

    private void createTemporaryAttachmentOnDisk(File file, File targetFile) throws AttachmentException
    {
        try
        {
            FileUtils.moveFile(file, targetFile);
        }
        catch (IOException e)
        {
            final String message = getI18n().getText("attachfile.error.save.to.store", e);
            log.error(message, e);
            throw new AttachmentException(message);
        }
    }

    LimitedOutputStream wrapOutputStream(OutputStream fos, long size)
    {
        return new LimitedOutputStream(new BufferedOutputStream(fos), size);
    }

    //Some characters are invalid on certain filesystems, JRA-5864, JRA-5595, JRA-6141
    void assertFileNameIsValid(final String filename) throws AttachmentException
    {
        if (StringUtils.isBlank(filename))
        {
            throw new AttachmentException(getI18n().getText("attachfile.error.no.name"));
        }

        final String invalidChar = fileNameCharacterCheckerUtil.assertFileNameDoesNotContainInvalidChars(filename);
        if (invalidChar != null)
        {
            throw new AttachmentException(getI18n().getText("attachfile.error.invalidcharacter", filename, invalidChar));
        }
    }

    //package level protected for tests.
    boolean assertAttachmentIfExists(final File file, final String fileName, final boolean required, final int contentLength)
            throws AttachmentException
    {
        final boolean exists = exists(file, fileName, contentLength);
        if (!exists)
        {
            if (required)
            {
                throw new AttachmentException(getI18n().getText("attachfile.error.filerequired"));
            }
        }
        else
        {
            assertFileNameIsValid(fileName);
        }

        return exists;
    }

    private boolean exists(final File file, final String fileName, final int contentLength) throws AttachmentException
    {
        if (file == null)
        {
            if (fileName != null)
            {
                final long attachmentSize = getMaxAttachmentSize();
                //now that we're only uploading a single file, the contentLength of the request is a pretty good
                //indicator if the attachment was too large. If it's bigger than the max size allowed we
                //return an error specific to this.
                if (contentLength > attachmentSize)
                {
                    throw new AttachmentException(getI18n().getText("attachfile.error.file.large", fileName, FileSize.format(attachmentSize)));
                }
                else
                {
                    throw new AttachmentException(getI18n().getText("attachfile.error.file.zero", fileName));
                }
            }
            else
            {
                return false;
            }
        }
        else if (file.length() == 0)
        {
            throw new AttachmentException(getI18n().getText("attachfile.error.file.zero", fileName));
        }
        return true;
    }

    ///CLOVER:OFF
    private long getUUID()
    {
        return Math.abs(UUID.randomUUID().getLeastSignificantBits());
    }

    UniqueFile createUniqueFile(String fileName) throws IOException, PathTraversalException
    {
        final File tmpDir = AttachmentUtils.getTemporaryAttachmentDirectory();
        long uniqueId;
        File tempAttachmentFile;
        do
        {
            //if the file already exists, choose a new UUID to avoid clashes!
            uniqueId = getUUID();
            tempAttachmentFile = new File(tmpDir, uniqueId + "_" + fileName);
        }
        while (tempAttachmentFile.exists());

        //JRA-29318: This is to ensure the path is in the secure directory. This should not throw any errors
        //because this method should have been called after assertFileNameIsValid method.
        PathUtils.ensurePathInSecureDir(tmpDir.getCanonicalPath(), tempAttachmentFile.getCanonicalPath());

        return new UniqueFile(tempAttachmentFile, uniqueId);
    }

    long getMaxAttachmentSize()
    {
        return Long.parseLong(Configuration.getString(APKeys.JIRA_ATTACHMENT_SIZE));
    }

    I18nHelper getI18n()
    {
        return beanFactory.getInstance(authenticationContext.getLocale());
    }

    String getFormToken(final Issue issue, final Project project)
    {
        final Either<Issue, Project> entity = Either.<Issue, Project>cond(issue == null, project, issue);
        return TemporaryAttachment.getEntityToken(entity);
    }

    ///CLOVER:ON

    void assertCanAttach(Issue issue, Project project) throws AttachmentException
    {
        if (issue == null)
        {
            //creating a new issue so when attaching temp files we don't yet have an issue yet to check permissions
            //against, so we check the project
            JiraServiceContext context = createServiceContext();
            service.canCreateAttachments(context, project);
            throwForFirstError(context.getErrorCollection());
        }
        else
        {
            JiraServiceContext context = createServiceContext();
            service.canCreateTemporaryAttachments(context, issue);
            throwForFirstError(context.getErrorCollection());
        }
    }

    private JiraServiceContext createServiceContext()
    {
        return new JiraServiceContextImpl(authenticationContext.getUser(),
                new SimpleErrorCollection(), authenticationContext.getI18nHelper());
    }

    private static void throwForFirstError(ErrorCollection collection) throws AttachmentException
    {
        if (collection.hasAnyErrors())
        {
            String message = getFirstElement(collection.getErrorMessages());
            if (message == null)
            {
                message = getFirstElement(collection.getErrors().values());
            }

            throw new AttachmentException(message);
        }
    }

    private static <T> T getFirstElement(Collection<? extends T> values)
    {
        if (!values.isEmpty())
        {
            return values.iterator().next();
        }
        else
        {
            return null;
        }
    }

    static class UniqueFile
    {
        private final File file;
        private final long id;

        UniqueFile(File file, long id)
        {
            this.file = file;
            this.id = id;
        }

        public File getFile()
        {
            return file;
        }

        public long getId()
        {
            return id;
        }
    }
}
