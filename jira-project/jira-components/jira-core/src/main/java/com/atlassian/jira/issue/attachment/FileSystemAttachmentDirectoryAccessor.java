package com.atlassian.jira.issue.attachment;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.fugue.Option;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueKey;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.util.AttachmentException;

import org.apache.log4j.Logger;

/**
 * Implementation of the AttachmentDirectoryAccessor that deals with filesystem-based attachment storage.
 *
 * @since v6.3
 */
public class FileSystemAttachmentDirectoryAccessor implements AttachmentDirectoryAccessor
{
    private static final Logger log = Logger.getLogger(FileSystemAttachmentDirectoryAccessor.class);
    private static final String TMP_ATTACHMENTS = "tmp_attachments";

    protected final ProjectManager projectManager;
    private final AttachmentPathManager attachmentPathManager;

    public FileSystemAttachmentDirectoryAccessor(final ProjectManager projectManager, final AttachmentPathManager attachmentPathManager)
    {
        this.projectManager = projectManager;
        this.attachmentPathManager = attachmentPathManager;
    }

    /**
     * Returns the physical directory of the thumbnails for the given issue, creating if necessary.
     *
     * @param issue the issue whose thumbnail directory you want
     * @return The issue's thumbnail directory.
     */
    @Nonnull
    @Override
    public File getThumbnailDirectory(@Nonnull final Issue issue)
    {
        final File thumbDir = new File(getAttachmentDirectory(issue), AttachmentManager.THUMBS_SUBDIR);
        if (!thumbDir.exists() && !thumbDir.mkdirs())
        {
            log.warn("Unable to make thumbnail directory " + thumbDir.getAbsolutePath());
        }
        return thumbDir;
    }

    /**
     * Returns the physical directory of the attachments for the given issue. This will create it if necessary.
     *
     * @param issue the issue whose attachment directory you want (required)
     * @return The issue's attachment directory.
     */
    @Override
    public File getAttachmentDirectory(@Nonnull final Issue issue)
    {
        return getAttachmentDirectory(issue, true);
    }

    @Override
    @Nullable
    public File getAttachmentDirectory(@Nonnull final String issueKey)
    {
        final IssueKey ik = IssueKey.from(issueKey);
        final Project project = projectManager.getProjectObjByKey(ik.getProjectKey());
        if (project != null)
        {
            return getAttachmentDirectory(getAttachmentDirName(), project.getOriginalKey(),
                    IssueKey.format(project.getOriginalKey(), ik.getIssueNumber()));
        }
        return null;
    }

    /**
     * Returns the physical directory of the attachments for the given issue. This will create it if necessary.
     *
     * @param issue the issue whose attachment directory you want
     * @param createDirectory If true, and the directory does not currently exist, then the directory is created.
     * @return The issue's attachment directory.
     */
    @Override
    public File getAttachmentDirectory(@Nonnull final Issue issue, final boolean createDirectory)
    {
        final Project project = issue.getProjectObject();
        final File directory = getAttachmentDirectory(getAttachmentDirName(), project.getKey(), issue.getKey());
        if (createDirectory)
        {
            //noinspection ResultOfMethodCallIgnored
            directory.mkdirs();
        }
        return directory;
    }

    @Override
    public File getTemporaryAttachmentDirectory()
    {
        final File cachesDirectory = ComponentAccessor.getComponent(JiraHome.class).getCachesDirectory();
        final File tempDirectory = new File(cachesDirectory, TMP_ATTACHMENTS);
        if (!tempDirectory.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            tempDirectory.mkdirs();
        }
        return tempDirectory;
    }

    /**
     * Get the attachment directory for the given attachment base directory, project key, and issue key.
     * <p/>
     * The idea is to encapsulate all of the path-joinery magic to make future refactoring easier if we ever decide to
     * move away from attachment-base/project-key/issue-ket
     *
     * @param attachmentDirectory base of attachments
     * @param projectKey the project key the issue belongs to
     * @param issueKey the issue key for the issue
     * @return the directory attachments for this issue live in
     */
    @Override
    public File getAttachmentDirectory(final String attachmentDirectory, final String projectKey, final String issueKey)
    {
        final Project project = projectManager.getProjectObjByKey(projectKey);
        if (project == null)
        {
            //looking for attachments in standard directory when project does not exist e.g. we are importing project and validating attachments
            final File projectDirectory = new File(attachmentDirectory, projectKey);
            return new File(projectDirectory, issueKey);
        }
        else
        {
            //handle renamed projects by always putting attachments in original directory
            return FileAttachments.getAttachmentDirectoryForIssue(new File(attachmentDirectory), project.getOriginalKey(), issueKey);
        }
    }

    /**
     * Checks that the Attachment directory of the given issue is right to go - writable, accessible etc. Will create it
     * if necessary.
     *
     * @param issue the issue whose attachment directory to check.
     * @throws com.atlassian.jira.web.util.AttachmentException if the directory is not writable or missing and cannot be created.
     */
    @Override
    public void checkValidAttachmentDirectory(Issue issue) throws AttachmentException
    {
        // check that we can write to the attachment directory
        try
        {
            File directory = getAttachmentDirectory(issue);

            if (!directory.canWrite())
            {
                throw new AttachmentStorageUnavailableException(directory.getAbsolutePath());
            }
            checkValidTemporaryAttachmentDirectory();
        }
        catch (Exception e)
        {
            throw new AttachmentStorageUnavailableException(e);
        }
    }

    @Override
    public void checkValidTemporaryAttachmentDirectory() throws AttachmentException
    {
        final File tempDirectory = getTemporaryAttachmentDirectory();
        if (!tempDirectory.canWrite())
        {
            throw new TemporaryAttachmentStorageUnavailableException(tempDirectory.getAbsolutePath());
        }
    }

    private String getAttachmentDirName()
    {
        return attachmentPathManager.getAttachmentPath();
    }

    @Override
    public File getAttachmentRootPath()
    {
        return new File(getAttachmentDirName());
    }

    /**
     * Checks that the attachment root directory and the temporary attachment directories exist and are writeable.
     */
    @Override
    public Option<ErrorCollection> errors()
    {
        ErrorCollection errors = checkDirectoryHealth(getAttachmentRootPath());
        errors.addErrorCollection(checkDirectoryHealth(getTemporaryAttachmentDirectory()));
        if (errors.hasAnyErrors())
        {
            return Option.some(errors);
        }
        else
        {
            return Option.none();
        }
    }

    private ErrorCollection checkDirectoryHealth(File path)
    {
        ErrorCollection errors = new SimpleErrorCollection();
        if (path != null)
        {
            if (!path.exists() || !path.isDirectory() || !path.canWrite())
            {
                errors.addErrorMessage("attachment path [" + path + "] invalid");
            }
        }
        return errors;
    }
}
